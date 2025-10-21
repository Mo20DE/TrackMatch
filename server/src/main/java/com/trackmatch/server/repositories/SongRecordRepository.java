package com.trackmatch.server.repositories;

import com.trackmatch.server.entities.SongRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SongRecordRepository extends JpaRepository<SongRecord, Long> {
    @Query("SELECT r.song_id FROM SongRecord r WHERE r.yt_id = :yt_id")
    List<Long> findByYtId(String yt_id);
}
