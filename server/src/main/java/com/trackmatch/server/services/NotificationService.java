package com.trackmatch.server.services;

import com.trackmatch.server.repositories.SongRecordRepository;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

@Service
public class NotificationService {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final SongRecordRepository songRecordRepository;

    public NotificationService(SimpMessagingTemplate simpMessagingTemplate, SongRecordRepository songRecordRepository) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.songRecordRepository = songRecordRepository;
    }

    @EventListener
    public void handleNotification(SessionConnectedEvent event) {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
            int total_songs = Math.toIntExact(songRecordRepository.count());
            simpMessagingTemplate.convertAndSend("/topic/default", total_songs);
        }).start();
    }

    public void sendNumberTotalSongs() {
        int total_songs = Math.toIntExact(songRecordRepository.count());
        simpMessagingTemplate.convertAndSend("/topic/number-total-songs", total_songs);
    }

    public void sendProcessStageNotification(String message) {
        simpMessagingTemplate.convertAndSend("/topic/process-status", message);
    }

    public void sendDownloadNotification(String message) {
        simpMessagingTemplate.convertAndSend("/topic/download-status", message);
    }
}
