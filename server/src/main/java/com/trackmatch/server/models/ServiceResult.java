package com.trackmatch.server.models;

import lombok.Getter;

@Getter
public class ServiceResult {

    private boolean success;
    private String message;
    private String data;
    private String media_type;

    public static ServiceResult success(String data, String media_type) {
        ServiceResult result = new ServiceResult();
        result.success = true;
        result.data = data;
        result.media_type = media_type;
        return result;
    }

    public static ServiceResult error(String message) {
        ServiceResult result = new ServiceResult();
        result.success = false;
        result.message = message;
        return result;
    }
}
