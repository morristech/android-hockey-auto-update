package com.sensorberg.iot_autoupdate.radios;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.sensorberg.iot_autoupdate.BuildConfig;

/**
 * Created by ronaldo on 12/21/16.
 */

public class OnConnection extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (BuildConfig.TURN_RADIOS_ON) {
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (wifi == null || !wifi.isConnected()) {
                CheckConnectionsService.checkWiFi(context);
            }

            if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                CheckConnectionsService.checkBluetooth(context);
            }
        }
    }
}
