package com.trackmatch.server.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SongData {

    private String artist;
    private String song;
    private String youtube_id = "";
    private boolean is_downloaded = false;

    public SongData(String artist, String song) {
        this.artist = artist;
        this.song = song;
    }
}
