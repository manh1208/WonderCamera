package com.superapp.wondercamera.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ManhNV on 2/11/17.
 */

public class ImageResponseModel {
    @SerializedName("status")
    private int status;
    @SerializedName("message")
    private String message;
    @SerializedName("error")
    private ErrorResponse error;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ErrorResponse getError() {
        return error;
    }

    public void setError(ErrorResponse error) {
        this.error = error;
    }
}
