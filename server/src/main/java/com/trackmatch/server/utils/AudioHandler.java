package com.trackmatch.server.utils;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;

import javax.sound.sampled.AudioFormat;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFloatConverter;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;

public class AudioHandler {

    private static final int SAMPLING_RATE = 44100;
    private static final int CHANNELS = 1;
    private static final int BITS_PER_SAMPLE = 16;

    private AudioFormat getFormat() {
        return new AudioFormat(
                SAMPLING_RATE,
                BITS_PER_SAMPLE,
                CHANNELS,
                true,
                false
        );
    }

    public byte[] convertAudioToByteArray(String trackname, Path d_path) {

        String song_path = d_path.resolve(trackname).toString();
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromPipe(
                song_path,
                SAMPLING_RATE,
                256, // Buffer Size
                0 // Overlap
        );
        TarsosDSPAudioFormat taf = new TarsosDSPAudioFormat(
                SAMPLING_RATE,
                BITS_PER_SAMPLE,
                CHANNELS,
                true,
                false
        );
        TarsosDSPAudioFloatConverter converter = TarsosDSPAudioFloatConverter.getConverter(taf);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        dispatcher.addAudioProcessor(new AudioProcessor() {
            @Override
            public boolean process(AudioEvent audioEvent) {
                float[] floatBuffer = audioEvent.getFloatBuffer();
                byte[] byteBuffer = new byte[floatBuffer.length * 2];
                converter.toByteArray(floatBuffer, byteBuffer);
                try {
                    baos.write(byteBuffer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
            @Override
            public void processingFinished() {}
        });

        dispatcher.run();
        return baos.toByteArray();
    }

    public boolean isTooQuite(byte[] audio) {

        double silenceThreshold = 0.01;
        double silenceDuration = 5.0;

        int samplesPerWindow = 1024;
        int totalSamples = audio.length / 2;
        double frameDuration = (double) samplesPerWindow / SAMPLING_RATE;
        double silentTime = 0;

        for (int i = 0; i < totalSamples; i += samplesPerWindow) {
            int end = Math.min(totalSamples, i + samplesPerWindow);
            double rms = computeRMS(audio, i, end);
            if (rms < silenceThreshold) {
                silentTime += frameDuration;
                if (silentTime >= silenceDuration) {
                    return true;
                }
            }
            else silentTime = 0;
        }
        return false;
    }

    private double computeRMS(byte[] audio, int startSample, int endSample) {
        long sum = 0;
        int sampleCount = endSample - startSample;
        for (int i = startSample; i < endSample * 2 - 1; i += 2) {
            int sample = (audio[i + 1] << 8) | (audio[i] & 0xFF);
            sum += (long) sample * sample;
        }
        double mean = (double) sum / sampleCount;
        return Math.sqrt(mean) / 32768.0;
    }

}
