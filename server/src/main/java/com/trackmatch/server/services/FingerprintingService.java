package com.trackmatch.server.services;

import com.trackmatch.server.algo.TrackMatch;
import com.trackmatch.server.models.SongData;
import com.trackmatch.server.utils.AudioHandler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

@Service
public class FingerprintingService {

    private final AudioHandler audioHandler = new AudioHandler();
    private final TrackMatch trackMatch = new TrackMatch();

    @Async("fpExecutor")
    public CompletableFuture<HashMap<Long, Integer>> generateFingerprint(SongData song, Path d_path) {
        try {
            String artist = song.getArtist();
            String songName = song.getSong();
            String file_name = artist + " - " + songName + ".mp3";
            byte[] bytesMp3 = audioHandler.convertAudioToByteArray(file_name, d_path);
            // generate songs fingerprint
            HashMap<Long, Integer> song_fingerprint = trackMatch.generateFingerprint(bytesMp3);
            System.out.println("Number of hashes generated: " + song_fingerprint.size() + "  |  " + song.getArtist() + " - " + song.getSong());
            return CompletableFuture.completedFuture(song_fingerprint);
        }
        catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
