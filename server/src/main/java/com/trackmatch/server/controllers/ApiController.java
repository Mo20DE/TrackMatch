package com.trackmatch.server.controllers;

import com.trackmatch.server.models.ServiceResult;
import com.trackmatch.server.repositories.SongRecordRepository;
import com.trackmatch.server.services.MainDownloadService;
import com.trackmatch.server.services.MatchingService;
import com.trackmatch.server.services.SpotifyService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/api")
@CrossOrigin(origins="http://localhost:3000")
public class ApiController {

    private final SpotifyService spotifyService;
    private final MainDownloadService mainDownloadService;
    private final MatchingService matchingService;
    private final SongRecordRepository songRecordRepository;

    @PostMapping("/process-audio")
    public ResponseEntity<?> processSnippet(@RequestBody Map<String, String> body) {
        // check if there is at least one song saved
        long totalSongs = songRecordRepository.count();
        if (totalSongs < 2) {
            System.out.println("No songs found");
            return ResponseEntity.noContent().build();
        }
        String base64Audio = body.get("audio");
        ServiceResult result = matchingService.computeMatching(base64Audio);
        if (!result.isSuccess()) {
            return ResponseEntity.unprocessableEntity().body(result.getMessage());
        }
        return ResponseEntity.ok(result.getData());
    }

    @PostMapping("/process-url")
    public ResponseEntity<?> processUrl(@RequestBody Map<String, String> body) {

        String url = body.get("url");
        // get music data from spotify web-api
        ServiceResult result = spotifyService.getSongsMetadata(url);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result.getMessage());
        }
        // download songs from YouTube and generate fingerprints
        mainDownloadService.startDownloadSongsAndProcess(result);
        return ResponseEntity.ok(result.getData());
    }
}
