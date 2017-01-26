package com.sensorberg.iot_autoupdate;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Url;

/**
 * Created by ronaldo on 1/25/17.
 */
public interface HockeyAPI {
    // https://rink.hockeyapp.net/api/2/apps/0873e2b98ad046a92c170a243a8515f6/app_versions/274?format=apk&avtoken=904526c53b6c82b8329f60771eaf4aad92fe9b3c

    @GET("/api/2/apps/{appId}/app_versions?include_build_urls=true")
    Observable<HockeyResponse> getVersions(@Header("X-HockeyAppToken") String authToken, @Path("appId") String appId);

    @GET
    Observable<ResponseBody> getFile(@Header("X-HockeyAppToken") String authToken, @Url String fileUrl);
}
