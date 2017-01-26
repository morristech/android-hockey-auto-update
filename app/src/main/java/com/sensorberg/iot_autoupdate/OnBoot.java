package com.sensorberg.iot_autoupdate;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;

import com.orhanobut.logger.Logger;
import com.sensorberg.iot_autoupdate.radios.CheckConnectionsService;

import static com.sensorberg.iot_autoupdate.App.ACCOUNT;
import static com.sensorberg.iot_autoupdate.App.ACCOUNT_TYPE;
import static com.sensorberg.iot_autoupdate.App.AUTHORITY;
import static com.sensorberg.iot_autoupdate.Util.installWiFiIfNeeded;

/**
 * Created by ronaldo on 1/25/17.
 */
public class OnBoot extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Logger.d("onBoot");

        if (BuildConfig.SET_CLOCK) {
            Logger.d("changing to automatic date-time");
            Settings.Global.putInt(context.getContentResolver(), Settings.Global.AUTO_TIME, 1);
        }

        if (!TextUtils.isEmpty(BuildConfig.WIFI_SSID) &&
                !TextUtils.isEmpty(BuildConfig.WIFI_PASS)) {
            installWiFiIfNeeded(context);
        }

        if (BuildConfig.TURN_RADIOS_ON) {
            Logger.d("executing connection service");
            CheckConnectionsService.checkWiFi(context);
            CheckConnectionsService.checkBluetooth(context);
        }

        if (BuildConfig.DISABLE_SCREEN_TIMEOUT) {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, Integer.MAX_VALUE);
        }

        Logger.d("requesting sync");
        Account account = new Account(ACCOUNT, ACCOUNT_TYPE);
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(account, AUTHORITY, settingsBundle);
    }
}
