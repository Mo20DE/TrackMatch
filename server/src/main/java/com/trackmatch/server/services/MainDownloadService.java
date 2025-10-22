package com.trackmatch.server.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackmatch.server.models.SongData;
import com.trackmatch.server.models.ServiceResult;
import com.trackmatch.server.repositories.SongRecordRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MainDownloadService {

    private final YouTubeDownloadTaskService taskService;
    private final StoringService storingService;
    private final NotificationService notificationService;
    private final FingerprintingService fingerprintingService;
    private final SongRecordRepository songRecordRepository;
    private final Path DOWNLOAD_PATH = Path.of("songs");

    public MainDownloadService(YouTubeDownloadTaskService taskService, StoringService storingService, NotificationService notificationService, FingerprintingService fingerprintingService,  SongRecordRepository songRecordRepository) {
        this.taskService =  taskService;
        this.storingService = storingService;
        this.notificationService = notificationService;
        this.fingerprintingService = fingerprintingService;
        this.songRecordRepository = songRecordRepository;
        try {
            Files.createDirectories(DOWNLOAD_PATH);
            // System.out.println("Created directory: " + DOWNLOAD_PATH.toAbsolutePath());
        }
        catch (IOException e) {
            notificationService.sendProcessStageNotification("error");
            System.err.println("Error creating directory: " + e.getMessage());
        }
    }

    @Async
    public void startDownloadSongsAndProcess(ServiceResult result) {
        // 1. Parsing
        List<SongData> songs_data = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(result.getData());
            String media_type = result.getMedia_type();

            if (media_type.equals("track")) {

                String artist = root.get("artists").get(0).get("name").asText();
                String song = root.get("name").asText();
                songs_data.add(new SongData(artist, song));
            }
            else if(media_type.equals("album") || media_type.equals("playlist")) {

                JsonNode songs = root.get("tracks").get("items");
                int num_songs = songs.size();
                System.out.println("Total songs in " + media_type + ": " + num_songs);

                for (int i = 0; i < num_songs; i++) {
                    JsonNode node = media_type.equals("album") ? songs.get(i) : songs.get(i).get("track");
                    String artist = node.get("artists").get(0).get("name").asText();
                    String song = node.get("name").asText();
                    songs_data.add(new SongData(artist, song));
                }
            }
            else {
                notificationService.sendProcessStageNotification("error");
                throw new IllegalArgumentException("Invalid media type: " + media_type);
            }
            System.out.println("Started downloading songs from youtube...");
        }
        catch (IOException e) {
            notificationService.sendProcessStageNotification("error");
            throw new RuntimeException(e);
        }
        // 2. Download coordination
        notificationService.sendProcessStageNotification("Downloading songs...");
        List<CompletableFuture<Void>> downloadFutures = new ArrayList<>();
        for (SongData song : songs_data) {
            downloadFutures.add(taskService.downloadSingleSong(song, DOWNLOAD_PATH));
        }
        CompletableFuture<Void> allDownloads = CompletableFuture.allOf(downloadFutures.toArray(new CompletableFuture[0]));
        // 3. Chaining and Fingerprinting
        allDownloads.thenRun(() -> {
            // delete all songs which were not downloaded
            songs_data.removeIf(song -> !song.is_downloaded());
            try {
                if (!songs_data.isEmpty()) {
                    System.out.println("✅ All YouTube-Downloads completed!");
                    // generate fingerprints, finally save them to fingerprint-db
                    notificationService.sendProcessStageNotification("Generating fingerprints...");
                    List<CompletableFuture<Void>> fpFutures = new ArrayList<>();
                    AtomicInteger counter = new AtomicInteger(0);
                    for (SongData song : songs_data) {
                        String song_name = song.getArtist() + " - " + song.getSong();
                        CompletableFuture<Void> future = fingerprintingService.generateFingerprint(song, DOWNLOAD_PATH)
                            .thenAccept(fingerprint -> {
                                storingService.saveSongDataToDB(song);
                                // extract song-id by youtube-id
                                String yt_id = song.getYoutube_id();
                                List<Long> list = songRecordRepository.findByYtId(yt_id);
                                Long song_id = list.get(0);
                                storingService.saveFingerprintToDB(fingerprint, song_id);
                                counter.incrementAndGet();
                                notificationService.sendNumberTotalSongs();
                                System.out.println("✅ Saved song & fingerprint for: " + song_name);
                            })
                            .exceptionally(ex -> {
                                notificationService.sendDownloadNotification("Fingerprint error: " + song_name);
                                System.err.println("❌ Fingerprint error for: " + song_name + ": " + ex.getMessage() );
                                return null;
                            });
                        fpFutures.add(future);
                    }
                    CompletableFuture.allOf(fpFutures.toArray(new CompletableFuture[0]))
                        .thenRun(() -> {
                            int total = songs_data.size();
                            int success = counter.get();
                            if (total == success) {
                                System.out.println("✅ All songs & fingerprints saved!");
                            } else {
                                System.err.println("⚠️ " + success + "/" + total + " songs & fingerprints saved!");
                            }
                            notificationService.sendProcessStageNotification("Fingerprinting done.");
                        });
                } else {
                    notificationService.sendProcessStageNotification("done.");
                }
            }
            catch (Exception e) {
                notificationService.sendProcessStageNotification("error");
                System.err.println("Error during chaining downloads from youtube: " + e.getMessage());
            }
        });
    }
}
