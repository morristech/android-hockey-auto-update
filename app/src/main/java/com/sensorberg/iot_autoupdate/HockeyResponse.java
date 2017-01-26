package com.sensorberg.iot_autoupdate;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ronaldo on 1/25/17.
 */
public class HockeyResponse {

    @SerializedName("app_versions")
    @Expose
    public List<AppVersion> appVersions = new ArrayList<>();
    @SerializedName("status")
    @Expose
    public String status;
}
