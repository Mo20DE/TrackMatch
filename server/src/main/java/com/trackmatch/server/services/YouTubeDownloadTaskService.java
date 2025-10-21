package com.trackmatch.server.services;

import com.trackmatch.server.models.SongData;
import com.trackmatch.server.repositories.SongRecordRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class YouTubeDownloadTaskService {

    private final NotificationService notificationService;
    private final SongRecordRepository songRecordRepository;

    public YouTubeDownloadTaskService(NotificationService notificationService,  SongRecordRepository songRecordRepository) {
        this.notificationService = notificationService;
        this.songRecordRepository = songRecordRepository;
    }

    @Async("ytDlpExecutor")
    public CompletableFuture<Void> downloadSingleSong(SongData song, Path d_path) {
        String currentArtist = song.getArtist();
        String currentSong = song.getSong();
        try {
            String file_name = currentArtist + " - " + currentSong;
            // search for youtube-url
            ProcessBuilder idBuilder = new ProcessBuilder(
                    "yt-dlp",
                    "ytsearch1:" + currentArtist + " " +  currentSong,
                    "--no-warnings",
                    "--force-ipv4",
                    "-f", "bestaudio",
                    "--compat-options", "youtube-dl",
                    "--get-id"
            );

            Process idProcess = idBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(idProcess.getInputStream()));
            String videoId = reader.readLine();
            int exitCode = idProcess.waitFor();

            // check if the song exists on youtube
            if (exitCode != 0 || videoId == null || videoId.isEmpty()) {
                String message = "Audio for \"" + file_name + "\" could not be found on YouTube";
                notificationService.sendDownloadNotification(message);
                System.err.println("❌ " + message);
                return CompletableFuture.completedFuture(null);
            }

            // check if the id is already in the database
            List<Long> result = songRecordRepository.findByYtId(videoId);
            if (!result.isEmpty()) {
                String message = file_name + " is already stored in database";
                notificationService.sendDownloadNotification(message);
                System.err.println("❌ " + message);
                return CompletableFuture.completedFuture(null);
            }

            String download_path = d_path.toString();
            System.out.println("Starting download of: " + file_name);

            ProcessBuilder downloadBuilder = new ProcessBuilder(
                    "yt-dlp",
                    "https://www.youtube.com/watch?v=" + videoId,
                    "--no-warnings",
                    "--force-ipv4",
                    "-f", "bestaudio",
                    "-x",
                    "--audio-format", "mp3",
                    "--audio-quality", "256k",
                    "--compat-options", "youtube-dl",
                    "--concurrent-fragments", "4",
                    "--output", download_path + "/" + file_name + ".%(ext)s"
            );

            Process downloadProcess = downloadBuilder.start();
            int downloadExit = downloadProcess.waitFor(); // returns exit-code

            if (downloadExit == 0) {
                String message = "Successfully downloaded: " + file_name;
                notificationService.sendDownloadNotification(message);
                song.setYoutube_id(videoId);
                song.set_downloaded(true);
                System.out.println("✅ " + message);
            }
            else {
                String message = "Error while downloading: " + file_name;
                notificationService.sendDownloadNotification(message);
                System.err.println("❌ " + message);
            }
            return CompletableFuture.completedFuture(null);
        }
        catch (Exception e) {
            String message = "Unexpected error downloading: " + currentArtist + " " + currentSong ;
            notificationService.sendDownloadNotification(message);
            System.err.println("❌ " + message + ": " + e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }
}
