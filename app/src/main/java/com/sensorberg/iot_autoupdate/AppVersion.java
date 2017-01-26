package com.sensorberg.iot_autoupdate;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by ronaldo on 1/25/17.
 */
public class AppVersion {

    @SerializedName("version")
    @Expose
    public String version;
    @SerializedName("mandatory")
    @Expose
    public boolean mandatory;
    @SerializedName("config_url")
    @Expose
    public String configUrl;
    @SerializedName("download_url")
    @Expose
    public String downloadUrl;
    @SerializedName("timestamp")
    @Expose
    public long timestamp;
    @SerializedName("appsize")
    @Expose
    public long appsize;
    @SerializedName("notes")
    @Expose
    public String notes;
    @SerializedName("status")
    @Expose
    public int status;
    @SerializedName("shortversion")
    @Expose
    public String shortversion;
    @SerializedName("title")
    @Expose
    public String title;
    @SerializedName("build_url")
    @Expose
    public String build_url;
}
