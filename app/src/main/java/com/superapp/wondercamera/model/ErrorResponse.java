package com.superapp.wondercamera.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ManhNV on 2/11/17.
 */

public class ErrorResponse {
    @SerializedName("statusCode")
    private int statusCode;
    @SerializedName("message")
    private String message;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
