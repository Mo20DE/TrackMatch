package com.trackmatch.server.entities;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class SongRecord {
    @Id
    @Column(name="song_id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long song_id;
    @Column(name = "artist")
    private String artist;
    @Column(name = "song")
    private String song;
    @Column(name = "yt_id")
    private String yt_id;
}
