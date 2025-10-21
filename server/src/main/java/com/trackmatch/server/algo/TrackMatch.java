package com.trackmatch.server.algo;

import com.github.psambit9791.jdsp.filter.Butterworth;

import java.util.*;

import com.trackmatch.server.models.Peak;
import org.jtransforms.fft.DoubleFFT_1D;

public class TrackMatch {

    private static final int[] FREQ_BANDS = new int[] {10, 20, 40, 80, 160, 511};
    private static final int INPUT_SAMPLING_FREQ = 44100;
    private static final int DOWNSAMPLE_FACTOR = 4;
    private static final int SAMPLING_FREQ = INPUT_SAMPLING_FREQ / DOWNSAMPLE_FACTOR; // 44100 HZ -> 11025 HZ
    private static final int CHUNK_SIZE = 1024; // 1024 samples are in one frame, so (44100/1024) = 43 frames / s
    private static final int HOP_SIZE = CHUNK_SIZE / 32;
    private static final int FFT_SIZE = CHUNK_SIZE;

    private static final Butterworth HP_FLT = new Butterworth(INPUT_SAMPLING_FREQ);
    private static final Butterworth LP_FLT = new Butterworth(INPUT_SAMPLING_FREQ);

    public TrackMatch() { }

    private double[][] computeSpectogram(byte[] buffer) {

        int totalBytes = buffer.length;
        int totalSamples = totalBytes / 2;
        double[] allInputSamples = new double[totalSamples];

        for (int i = 0; i < totalSamples; i++) {
            int index = i * 2;
            int hi = buffer[index + 1];
            int lo = buffer[index] & 0xFF;
            short signedSample = (short) ((hi << 8) | lo);
            allInputSamples[i] = (double) signedSample / 32768.0; // normalize to -1.0 to 1.0
        }

        // apply high-pass butterworth filter to filter out noise
        double[] filteredHighPass = HP_FLT.highPassFilter(allInputSamples, 4, 300);
        allInputSamples = null;

        // apply low-pass butterworth filter to filter out high frequencies (anti-aliasing)
        double[] filteredSamples = LP_FLT.lowPassFilter(filteredHighPass, 8, 5000);
        filteredHighPass = null;

        // downsample audio (averaging)
        int new_length = filteredSamples.length / DOWNSAMPLE_FACTOR;
        double[] downsampled = new double[new_length];

        for (int k = 0; k < new_length; k++) {
            double sum = 0;
            for (int curr = 0; curr < DOWNSAMPLE_FACTOR; curr++) {
                int idx = k * DOWNSAMPLE_FACTOR + curr;
                if (idx < filteredSamples.length) {
                    sum += filteredSamples[idx];
                }
            }
            downsampled[k] = sum / DOWNSAMPLE_FACTOR;
        }
        filteredSamples = null;

        int numFrames = (downsampled.length - CHUNK_SIZE) / HOP_SIZE + 1;
        List<double[]> validFrames = new ArrayList<>();

        for (int frame = 0; frame < numFrames; frame++) {

            double[] currentSamples = new double[FFT_SIZE];
            int start = frame * HOP_SIZE;
            System.arraycopy(downsampled, start, currentSamples, 0, CHUNK_SIZE);

            // apply the hamming-window
            for (int n = 0; n < FFT_SIZE; n++) {
                double w = 0.54 - 0.46 * Math.cos((2 * Math.PI * n) / (FFT_SIZE - 1));
                currentSamples[n] *= w;
            }
            // apply FFT and transform to frequency-domain
            DoubleFFT_1D fft = new DoubleFFT_1D(FFT_SIZE);
            fft.realForward(currentSamples);
            validFrames.add(currentSamples);
        }
        downsampled = null;
        return validFrames.toArray(new double[0][]);
    }

    private static int getBandIndex(int freq) {
        int i = 0;
        while (i < FREQ_BANDS.length && FREQ_BANDS[i] < freq) i++;
        return Math.min(i, FREQ_BANDS.length - 1);
    }

    private List<Peak> extractPeaks(double[][] spectrogram) {

        int numFrames = spectrogram.length;
        double[][] magnitudes = new double[numFrames][FREQ_BANDS.length];
        int[][] freq_bins = new int[numFrames][FREQ_BANDS.length];
        List<Peak> final_peaks = new ArrayList<>();

        // iterate over all frames
        for (int frame = 0; frame < numFrames; frame++) {
            // initialize all bands to negative infinity
            for (int band = 0; band < FREQ_BANDS.length; band++) {
                magnitudes[frame][band] = Double.NEGATIVE_INFINITY;
            }
            // iterate over all bins
            for (int freqBin = 1; freqBin < FFT_SIZE / 2; freqBin++) {

                int index = getBandIndex(freqBin);
                double re = spectrogram[frame][2*freqBin];
                double im = spectrogram[frame][2*freqBin+1];
                double magnitude = Math.log(Math.sqrt(re * re + im * im) + 1); // betrachte chunk, für jeden chunk nehmen wir innerhalb eines frequenz-intervalls (band) die frequenz mit der höchsten magnitude

                if (magnitude > magnitudes[frame][index]) {
                    magnitudes[frame][index] = magnitude;
                    freq_bins[frame][index] = freqBin;
                }
            }
            // for every frame, filter those peaks which are above treshold t
            double peak_avg = Arrays.stream(magnitudes[frame]).average().orElse(0) * 1.1;
            for (int i = 0; i < magnitudes[frame].length; i++) {
                double mag = magnitudes[frame][i];
                int freqBin = freq_bins[frame][i];
                if (mag > peak_avg) {
                    Peak peak = new Peak(frame, freqBin, mag);
                    final_peaks.add(peak);
                }
            }
        }
        return final_peaks;
    }

    private double computeAbsoluteTime(int frameIndex) {
        double secondsPerFrame = (double) HOP_SIZE / (double) SAMPLING_FREQ;
        return frameIndex * secondsPerFrame * 1000.0;
    }

    private long getHash(Peak peak, int t_anchor_ms, Peak anchor) {
        int t_peak_ms = (int) Math.round(computeAbsoluteTime(peak.frameIndex()));
        long deltaMs = (long) (t_peak_ms - t_anchor_ms);

        int anchor_freq = anchor.freqBin();
        int peak_freq = peak.freqBin();

        final int FREQ_MASK = 0b111111111;
        final long DELTA_MASK = 0b11111111111111L;

        long masked_anchor_freq = anchor_freq & FREQ_MASK;
        long masked_peak_freq = peak_freq & FREQ_MASK;
        long masked_deltaMs = deltaMs & DELTA_MASK;

        return (masked_anchor_freq << 23) | (masked_peak_freq << 14) | (masked_deltaMs);
    }

    public HashMap<Long, Integer> generateFingerprint(byte[] buffer) {

        double[][] spectrogram = computeSpectogram(buffer);
        List<Peak> all_peaks = extractPeaks(spectrogram);
        spectrogram = null;
        HashMap<Long, Integer> fingerprint = new HashMap<>();

        int targetZoneSize = 5;
        int numPoints = all_peaks.size();

        // iterate over all frames
        for (int i = 0; i < numPoints; i++) {

            Peak anchor = all_peaks.get(i);
            for (int j = i + 1; j < Math.min(i + 1 + targetZoneSize, numPoints); j++) {

                Peak peak = all_peaks.get(j);
                int t_anchor_ms = (int) Math.round(computeAbsoluteTime(anchor.frameIndex()));
                long hash = getHash(peak, t_anchor_ms, anchor);
                fingerprint.put(hash, t_anchor_ms);
            }
        }
        return fingerprint;
    }
}
