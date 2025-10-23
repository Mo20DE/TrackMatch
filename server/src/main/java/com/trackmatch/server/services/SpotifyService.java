package com.trackmatch.server.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackmatch.server.models.ServiceResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

@Service
public class SpotifyService {

    private final HttpClient client = HttpClient.newHttpClient();
    private final NotificationService notificationService;
    @Value("${spotify.client.id}")
    private String SPOTIFY_CLIENT_ID;
    @Value("${spotify.client.secret}")
    private String SPOTIFY_CLIENT_SECRET;


    public SpotifyService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public ServiceResult getSongsMetadata(String url) {
        try {
            notificationService.sendProcessStageNotification("Fetching data...");
            // send request to spotify to get the access-token for web-api
            String token_url = "https://accounts.spotify.com/api/token";
            String client_data = SPOTIFY_CLIENT_ID + ":" + SPOTIFY_CLIENT_SECRET;
            String encoded_client_data = Base64.getEncoder().encodeToString((client_data.getBytes()));

            HttpRequest access_token_request = HttpRequest.newBuilder()
                .uri(URI.create(token_url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", "Basic " + encoded_client_data)
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                .build();

            HttpResponse<String> access_token_response = client.send(access_token_request, HttpResponse.BodyHandlers.ofString());
            // extract access token for web-api
            String responseBody = access_token_response.body();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);
            String accessToken = root.get("access_token").asText(); // access token for web-api

            String web_api_url = "https://api.spotify.com/v1/";
            URI uri = new URI(url);
            String path = uri.getPath();
            String[] segments = path.split("/");

            // media_type: "track" or "album" or "playlist"
            String media_type = segments[segments.length - 2];
            String media_id = segments[segments.length - 1];
            String endpoint = media_type + "s/";

            HttpRequest web_api_request = HttpRequest.newBuilder()
                .uri(URI.create(web_api_url + endpoint + media_id))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

            HttpResponse<String> web_api_response = client.send(web_api_request, HttpResponse.BodyHandlers.ofString());
            if (web_api_response.statusCode() == 200) {
                return ServiceResult.success(web_api_response.body(), media_type);
            }
            else {
                notificationService.sendProcessStageNotification("error");
                return ServiceResult.error("Error while fetching songs from Spotify: " + web_api_response.statusCode());
            }
        }
        catch (InterruptedException e) {
            notificationService.sendProcessStageNotification("error");
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted while fetching songs from Spotify: " + e.getMessage());
        }
        catch (IOException | URISyntaxException e) {
            notificationService.sendProcessStageNotification("error");
            throw new RuntimeException("Error while retrieving the song(s): " + e.getMessage());
        }
    }
}
