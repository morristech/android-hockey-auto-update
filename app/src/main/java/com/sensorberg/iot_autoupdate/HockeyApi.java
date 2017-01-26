package com.sensorberg.iot_autoupdate;

import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by ronaldo on 1/25/17.
 */
public class HockeyApi {

    private static HockeyAPI instance;

    public static HockeyAPI get() {
        if (instance == null) {
            Retrofit restAdapter = new Retrofit.Builder()
                    .baseUrl("https://sdk.hockeyapp.net")
                    .client(new OkHttpClient.Builder().build())
                    .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create()))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            instance = restAdapter.create(HockeyAPI.class);
        }
        return instance;
    }
}
