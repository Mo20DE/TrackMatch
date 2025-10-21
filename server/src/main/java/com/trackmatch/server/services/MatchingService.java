package com.trackmatch.server.services;

import com.trackmatch.server.algo.TrackMatch;
import com.trackmatch.server.entities.FingerprintPair;
import com.trackmatch.server.entities.SongRecord;
import com.trackmatch.server.models.ServiceResult;
import com.trackmatch.server.repositories.FingerprintPairRepository;
import com.trackmatch.server.repositories.SongRecordRepository;
import com.trackmatch.server.utils.AudioHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class MatchingService {

    private final FingerprintPairRepository fingerprintPairRepository;
    private final SongRecordRepository songRecordRepository;
    private final TrackMatch trackMatch = new TrackMatch();

    public MatchingService(FingerprintPairRepository fingerprintPairRepository, SongRecordRepository songRecordRepository) {
        this.fingerprintPairRepository = fingerprintPairRepository;
        this.songRecordRepository = songRecordRepository;
    }

    private HashMap<Long, Integer> analyzeHashMap(HashMap<Long, List<int[]>> matches) {

        HashMap<Long, HashMap<Integer, Integer>> histograms = new HashMap<>();
        // construct histograms
        for (Map.Entry<Long, List<int[]>> entry : matches.entrySet()) {

            Long songID = entry.getKey();
            List<int[]> timePairs = entry.getValue();
            histograms.put(songID, new HashMap<>());
            HashMap<Integer, Integer> histogram = histograms.get(songID);

            for (int[] pair : timePairs) {
                int sampleTimeMs = pair[0];
                int matchTimeMs = pair[1];
                int offset = Math.abs(matchTimeMs - sampleTimeMs);
                histogram.put(offset, histogram.getOrDefault(offset, 0) + 1);
            }
        }

        HashMap<Long, Integer> scores = new HashMap<>();
        for (Map.Entry<Long, HashMap<Integer, Integer>> entry : histograms.entrySet()) {
            Long songID = entry.getKey();
            int max_score = 0;
            for (int count : entry.getValue().values()) {
                if (count > max_score) {
                    max_score = count;
                }
            }
            scores.put(songID, max_score);
        }
        return scores;
    }

    private String queryHashMap(byte[] standardized_audio) {

        // generate fingerprints for audio snippet
        HashMap<Long, Integer> fingerprint = trackMatch.generateFingerprint(standardized_audio);
        if (fingerprint.isEmpty()) {
            return null;
        }
        List<Long> allHashes = new ArrayList<>(fingerprint.keySet());
        List<FingerprintPair> allMatches = fingerprintPairRepository.findByHashIn(allHashes);
        System.out.println("Total Snippet fingerprint hashes: " + fingerprint.size());

        // matching - extract all matched hashes from global hashmap for every songID
        HashMap<Long, List<int[]>> matchedTimePairs = new HashMap<>();

        Map<Long, Integer> snippetAnchorTimes = new HashMap<>(fingerprint);

        for (FingerprintPair match : allMatches) {
            Long hash = match.getHash();
            Long songID = match.getSong_id();
            int matchAnchorTimeMs = match.getAnchor_time();
            Integer sampleAnchorTimeMs = snippetAnchorTimes.get(hash);
            if (sampleAnchorTimeMs != null) {
                int[] timePair = new int[] {sampleAnchorTimeMs, matchAnchorTimeMs};
                matchedTimePairs.computeIfAbsent(songID, k -> new ArrayList<>())
                        .add(timePair);
            }
        }
        // compute scoring for every song hashmap
        HashMap<Long, Integer> scores = analyzeHashMap(matchedTimePairs);
        List<Map.Entry<Long, Integer>> sortedScores = new ArrayList<>(scores.entrySet());
        sortedScores.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        sortedScores = sortedScores.stream().limit(4).toList(); // retrieve the 10 most likely matches

        StringBuilder songs_yt_ids = new StringBuilder();
        for (Map.Entry<Long, Integer> scoreEntry : sortedScores) {
            SongRecord songRecord = songRecordRepository.findById(scoreEntry.getKey()).orElse(null);
            assert songRecord != null;
            songs_yt_ids.append(songRecord.getYt_id()).append(",");
            System.out.printf("Song-ID: %d | Score: %d | %s - %s\n", scoreEntry.getKey(), scoreEntry.getValue(), songRecord.getArtist(), songRecord.getSong());
        }
        return songs_yt_ids.toString();
    }

    public ServiceResult computeMatching(String base64Audio) {
        try {
            AudioHandler audioHandler = new AudioHandler();
            // save audio to filesystem
            byte[] audio = Base64.getDecoder().decode(base64Audio);
            Path path = Paths.get("snippet.mp3");
            Files.write(path, audio);
            // convert audio to standardized format
            byte[] standardized_audio = audioHandler.convertAudioToByteArray("snippet.mp3", Path.of(""));
            // check if audio is too quite
            if (audioHandler.isTooQuite(standardized_audio)) {
                String message = "The recorded audio is too quite";
                System.out.println(message);
                return ServiceResult.error(message);
            }
            Files.delete(path);
            System.out.println("Starting matching-process...");
            String final_result = queryHashMap(standardized_audio); // youtube-id or null
            if  (final_result == null) {
                return ServiceResult.error("The recorded audio is invalid");
            }
            return ServiceResult.success(final_result, "");
        }
        catch (IOException e) {
            System.err.println("Error writing test.mp3");
            throw new RuntimeException(e);
        }
    }
}
