package com.trackmatch.server.entities;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class FingerprintPair {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "hash", nullable = false)
    private Long hash;
    @Column(name = "song_id", nullable = false)
    private Long song_id;
    @Column(name = "anchor_time", nullable = false)
    private int anchor_time;
}
