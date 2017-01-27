package com.sensorberg.iot_autoupdate;

import android.content.Context;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.f2prateek.rx.preferences2.Preference;
import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.orhanobut.logger.Logger;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import okhttp3.ResponseBody;

import static com.sensorberg.iot_autoupdate.Util.exist;
import static com.sensorberg.iot_autoupdate.Util.installAPK;
import static com.sensorberg.iot_autoupdate.Util.launchAPK;
import static com.sensorberg.iot_autoupdate.Util.versionHash;
import static com.sensorberg.iot_autoupdate.Util.writeResponseBodyToDisk;

/**
 * Created by ronaldo on 1/25/17.
 */
public class Updater {

    private final Context context;
    private final String token;
    private final String appId;
    private final String packageName;
    private final Preference<String> savedHash;
    private AppVersion latestVersion;
    private String latestHash;

    public Updater(Context context, String token, String appId, String packageName) {
        this.context = context.getApplicationContext();
        this.token = token;
        this.appId = appId;
        this.packageName = packageName;
        RxSharedPreferences pref = RxSharedPreferences.create(PreferenceManager.getDefaultSharedPreferences(context));
        savedHash = pref.getString(appId, "<blank>");
    }

    public void clear() {
        savedHash.delete();
    }

    public Observable<Boolean> check() {

        return HockeyApi.get().getVersions(token, appId)
                // get latest from hockey
                .flatMap(new Function<HockeyResponse, ObservableSource<ResponseBody>>() {
                    @Override
                    public ObservableSource<ResponseBody> apply(HockeyResponse hockeyResponse) throws Exception {

                        latestVersion = hockeyResponse.appVersions.get(0);
                        latestHash = versionHash(latestVersion);

                        Logger.d("saved is %s and latest is %s", savedHash.get(), latestHash);

                        if (exist(context, packageName) && latestHash.equals(savedHash.get())) {
                            Logger.d("Nothing to do, terminating");
                            return Observable.empty();
                        } else {
                            Logger.d(String.format("Downloading: %s - %s", latestVersion.title, latestVersion.version));
                            return HockeyApi.get().getFile(token, latestVersion.build_url);
                        }
                    }
                })
                // download the APK
                .map(new Function<ResponseBody, File>() {
                    @Override
                    public File apply(ResponseBody responseBody) throws Exception {
                        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        String appName = latestVersion.title.replaceAll("[^a-zA-Z0-9]", "");
                        File f = File.createTempFile(TextUtils.isEmpty(appName) ? "temp_" : appName, ".apk", dir);
                        f.deleteOnExit();
                        long size = writeResponseBodyToDisk(responseBody, f);
                        Logger.d(String.format("%s bytes written to %s", size, f.getAbsolutePath()));
                        return f;
                    }
                })
                // install the APK
                .map(new Function<File, Boolean>() {
                    @Override
                    public Boolean apply(File file) throws Exception {
                        Logger.d("Installing ... ");
                        boolean val = installAPK(file);
                        Logger.d("Installation complete!");
                        if (!file.delete()) {
                            Logger.e("Failed to delete " + file.getAbsolutePath());
                        }
                        return val;
                    }
                })
                // launch the APK
                .map(new Function<Boolean, Boolean>() {
                    @Override
                    public Boolean apply(Boolean aBoolean) throws Exception {
                        return launchAPK(context, BuildConfig.HOCKEY_APP_PACKAGE_NAME);
                    }
                })
                // save this install
                .map(new Function<Boolean, Boolean>() {
                    @Override
                    public Boolean apply(Boolean aBoolean) throws Exception {
                        savedHash.set(latestHash);
                        return aBoolean;
                    }
                });
    }
}
