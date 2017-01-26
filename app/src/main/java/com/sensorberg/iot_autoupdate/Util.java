package com.sensorberg.iot_autoupdate;

import android.accounts.Account;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import okhttp3.ResponseBody;

import static com.sensorberg.iot_autoupdate.App.ACCOUNT;
import static com.sensorberg.iot_autoupdate.App.ACCOUNT_TYPE;
import static com.sensorberg.iot_autoupdate.App.AUTHORITY;

/**
 * Created by ronaldo on 1/25/17.
 */
public class Util {

    public static boolean installAPK(File file) throws IOException, InterruptedException {
        if (file.exists()) {
            long start = System.currentTimeMillis();
            String cmd = String.format("pm install -r %s", file.getAbsolutePath());
            Logger.d("Executing: " + cmd);
            Process proc = Runtime.getRuntime().exec(cmd);
            int result = proc.waitFor();
            long end = System.currentTimeMillis();
            float diff = ((float) (end - start)) / 1000f;
            Logger.i(String.format("pm install executed in %.2fs with result, %s", diff, result));
            return true;
        } else {
            throw new FileNotFoundException("Cannot find " + file.getAbsolutePath());
        }
    }

    public static boolean launchAPK(Context context, String packageName) throws ActivityNotFoundException {
        Intent i;
        i = context.getPackageManager().getLaunchIntentForPackage(packageName);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Logger.d("Launching " + i.toString());
        context.startActivity(i);
        return true;
    }

    public static long writeResponseBodyToDisk(ResponseBody body, File file) throws IOException {

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            byte[] fileReader = new byte[8192];

            long fileSize = body.contentLength();
            long fileSizeDownloaded = 0;

            inputStream = body.byteStream();
            outputStream = new FileOutputStream(file);

            while (true) {
                int read = inputStream.read(fileReader);

                if (read == -1) {
                    break;
                }
                outputStream.write(fileReader, 0, read);
                fileSizeDownloaded += read;
                Logger.v("file download: " + fileSizeDownloaded + " of " + fileSize);
            }
            outputStream.flush();
            return fileSizeDownloaded;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }

            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    public static boolean exist(Context context, String packageName) {
        return context.getPackageManager().getLaunchIntentForPackage(packageName) != null;
    }

    public static String versionHash(AppVersion a) {
        return String.format("%s-%s-%s", a.version, a.timestamp, a.appsize);
    }

    public static void requestSync() {
        Account account = new Account(ACCOUNT, ACCOUNT_TYPE);
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(account, AUTHORITY, settingsBundle);
    }

    public static void installWiFiIfNeeded(Context context) {

        if (!TextUtils.isEmpty(BuildConfig.WIFI_SSID) &&
                !TextUtils.isEmpty(BuildConfig.WIFI_PASS)) {

            Logger.d("setup WiFi network");

            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            boolean found = false;
            List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
            if (list != null) {
                for (WifiConfiguration i : list) {
                    if (BuildConfig.WIFI_SSID.equals(i.SSID)) {
                        Logger.d("WiFi already configured");
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                Logger.d("adding WiFi network");

                wifiManager.setWifiEnabled(true);

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {/**/}

                WifiConfiguration conf = new WifiConfiguration();
                conf.SSID = "\"" + BuildConfig.WIFI_SSID + "\"";
                conf.preSharedKey = "\"" + BuildConfig.WIFI_PASS + "\"";
                conf.priority = Integer.MAX_VALUE;
                wifiManager.addNetwork(conf);

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {/**/}

                list = wifiManager.getConfiguredNetworks();
                if (list != null) {
                    for (WifiConfiguration i : list) {
                        if (i.SSID != null && i.SSID.equals("\"" + BuildConfig.WIFI_SSID + "\"")) {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {/**/}
                            wifiManager.disconnect();
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {/**/}
                            Logger.d("enableNetwork = %s", wifiManager.enableNetwork(i.networkId, true));
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {/**/}
                            wifiManager.reconnect();
                            break;
                        }
                    }
                } else {
                    Logger.e("failed to add WiFi network");
                }
            }
        }
    }
}
