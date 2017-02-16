package com.superapp.wondercamera.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ManhNV on 2/16/17.
 */

public class StoreVersion {
    @SerializedName("version")
    private VersionCode versionCode;
    @SerializedName("storeURL")
    private String storeUrl;

    public VersionCode getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(VersionCode versionCode) {
        this.versionCode = versionCode;
    }

    public String getStoreUrl() {
        return storeUrl;
    }

    public void setStoreUrl(String storeUrl) {
        this.storeUrl = storeUrl;
    }
}
