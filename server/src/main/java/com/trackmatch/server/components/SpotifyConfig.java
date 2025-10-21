package com.trackmatch.server.components;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class SpotifyConfig {
    @Value("${spotify.api.client}")
    private String clientID;
    @Value("${spotify.api.secret}")
    private String clientSecret;
}
