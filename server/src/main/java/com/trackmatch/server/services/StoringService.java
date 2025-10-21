package com.trackmatch.server.services;

import com.trackmatch.server.entities.FingerprintPair;
import com.trackmatch.server.entities.SongRecord;
import com.trackmatch.server.models.SongData;
import com.trackmatch.server.repositories.FingerprintPairRepository;
import com.trackmatch.server.repositories.SongRecordRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class StoringService {

    private final SongRecordRepository songRecordRepository;
    private final FingerprintPairRepository fingerprintPairRepository;

    public StoringService(SongRecordRepository songRecordRepository, FingerprintPairRepository fingerprintPairRepository) {
        this.songRecordRepository = songRecordRepository;
        this.fingerprintPairRepository = fingerprintPairRepository;
    }

    public void saveSongDataToDB(SongData song) {
        SongRecord songRecord = new SongRecord();
        String artist = song.getArtist();
        String songName = song.getSong();
        String yt_id = song.getYoutube_id();
        songRecord.setArtist(artist);
        songRecord.setSong(songName);
        songRecord.setYt_id(yt_id);
        songRecordRepository.save(songRecord);
    }

    public void saveFingerprintToDB(HashMap<Long, Integer> song_fingerprint, Long song_id) {
        List<FingerprintPair> pairsToSave = new ArrayList<>(song_fingerprint.size());
        for  (Long hash : song_fingerprint.keySet()) {
            Integer anchor_time = song_fingerprint.get(hash);
            FingerprintPair fingerprintPair = new FingerprintPair();
            fingerprintPair.setHash(hash);
            fingerprintPair.setSong_id(song_id);
            fingerprintPair.setAnchor_time(anchor_time);
            pairsToSave.add(fingerprintPair);
        }
        fingerprintPairRepository.saveAll(pairsToSave);
    }
}
