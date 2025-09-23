package com.myproject;

import com.fftplot.SpectrogramPlot;

import javax.sound.sampled.*;
import java.io.*;

import org.jtransforms.fft.DoubleFFT_1D;

public class TrackMatch {
    
    private static AudioFormat getFormat(float sampleRate) {
        int sampleSize = 16; // Higher precision audio
        int channels = 1; // Mono
        boolean signed = true;
        boolean bigEndian = true;
        return new AudioFormat(sampleRate, sampleSize, channels, signed, bigEndian);
    }

    private static TargetDataLine getTargetDataLineInstance() {
        
        float sampleRate = 44100;
        final AudioFormat format = getFormat(sampleRate);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            return null;
        }
        try {
            final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            return line;
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ByteArrayOutputStream RecordWithMic(TargetDataLine line, int time) {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        long t = System.currentTimeMillis();
        long end = t + time;

        line.start();
        while (System.currentTimeMillis() < end) {
                byte[] temp = new byte[1024];
                int count = line.read(temp, 0, temp.length);
                if (count > 0) {
                    buffer.write(temp, 0, count);
                }
            }
            System.out.flush();

        return buffer;
    }

    private static double[][] applyFFT(ByteArrayOutputStream buffer) {

        // convert byte array to integer array
        byte audio[] = buffer.toByteArray();
        int totalSamples = audio.length / 2;
        int chunkSize = 1024; // 2000 samples are one frame 
        int sampledChunkSize = totalSamples / chunkSize;

        double[][] result = new double[sampledChunkSize][];
        
        for (int i = 0; i < sampledChunkSize; i++) {
            // if last chunk is too small, just cut it off
            int remaining = totalSamples - i * chunkSize;
            int currentChunkSize = Math.min(chunkSize, remaining);

            double[] samples = new double[currentChunkSize];
            for (int j = 0; j < currentChunkSize; j++) {
                int index = i * chunkSize * 2 + j * 2;
                int hi = audio[index + 1];
                int lo = audio[index] & 0xFF;
                double sample = ((hi << 8) | lo) / 32768;
                samples[j] = sample;
            }
            // downsample audio
            int downsample_factor = 4; // 44100 -> 11025 
            int new_length = samples.length / downsample_factor;
            double[] downsampled = new double[new_length];
            for (int k = 0; k < new_length; k++) {
                downsampled[k] = samples[k * downsample_factor];
            }
            // apply the hamming-window
            int N = downsampled.length;
            for (int n = 0; n < N; n++) {
                double w = 0.54 - 0.46 * Math.cos((2 * Math.PI * n) / (N - 1));
                downsampled[n] *= w;
            }
            // apply FFT and transform to frequency-domain
            DoubleFFT_1D fft = new DoubleFFT_1D(new_length);
            fft.realForward(downsampled);
            result[i] = downsampled;
        }
        return result;
    }

    public static int getIndex(int freq) {
        int i = 0;
        while (i < freq) i++;
        return i;
    }

    private static double[][] ExtractPeaks(double[][] spectrogram) {

        int total_peaks = spectrogram.length;
        final int[] Range = new int[] {20, 40, 80, 120, 180, 300};
        double[][] points = new double[total_peaks][Range.length];
        double[][] highscores = new double[total_peaks][Range.length];

        for (int t = 0; t < total_peaks; t++) {
            for (int freq = 20; freq < 300; freq++) {
                double mag = Math.log(Math.abs(spectrogram[t][freq]) + 1); // betrachte chunk, für jeden chunk nehmen wir innerhalb eines frequenz-intervalls (segment) die frequenz mit der höchsten magnitude
                int index = getIndex(freq);
                if (mag > highscores[t][index]) {
                    highscores[t][index] = mag;
                    points[t][index] = freq;
                }
            }
        }
        return points;
    }

    private static void SaveAudio(ByteArrayOutputStream buffer, AudioFormat format) {
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer.toByteArray());
        AudioInputStream ais = new AudioInputStream(bais, format, buffer.size() / format.getFrameSize());
        File wavefile = new File("test.wav");
        try {
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, wavefile);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static void main(String[] args) {

        TargetDataLine line = getTargetDataLineInstance();
        ByteArrayOutputStream output = RecordWithMic(line, 2500);
        double[][] result = applyFFT(output);
        System.out.println(result[0]);
        // SpectrogramPlot.show(result);
        // SaveAudio(output, getFormat());
        // System.out.println(output.toByteArray());
    }
}

