package com.superapp.wondercamera.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ManhNV on 2/16/17.
 */

public class VersionCode {
    @SerializedName("_id")
    private String id;
    @SerializedName("versionNumber")
    private String versionNumber;
    @SerializedName("description")
    private String description;
    @SerializedName("create_at")
    private String createDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }
}
