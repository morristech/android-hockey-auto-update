package com.sensorberg.iot_autoupdate.radios;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.orhanobut.logger.Logger;
import com.sensorberg.iot_autoupdate.BuildConfig;

import static com.sensorberg.iot_autoupdate.Util.installWiFiIfNeeded;
import static com.sensorberg.iot_autoupdate.Util.requestSync;

public class CheckConnectionsService extends Service {

    private static final long DLY_WIFI_OFF = 5000;
    private static final long DLY_WIFI_ON = 8000 + DLY_WIFI_OFF;
    private static final long DLY_WIFI_CHECK = 30000 + DLY_WIFI_ON;
    private static final long DLY_BT_OFF = 1000;
    private static final long DLY_BT_ON = 5000 + DLY_BT_OFF;

    private static final int MSG_WIFI_ON = 1;
    private static final int MSG_WIFI_OFF = 2;
    private static final int MSG_WIFI_CHECK = 3;
    private static final int MSG_BT_ON = 4;
    private static final int MSG_BT_OFF = 5;
    private static final String KEY = "key";

    private final Handler handler;

    public static void checkWiFi(Context context) {
        Intent i = new Intent(context, CheckConnectionsService.class);
        i.putExtra(KEY, MSG_WIFI_ON);
        context.startService(i);
    }

    public static void checkBluetooth(Context context) {
        Intent i = new Intent(context, CheckConnectionsService.class);
        i.putExtra(KEY, MSG_BT_ON);
        context.startService(i);
    }

    public CheckConnectionsService() {
        handler = new ConnectionHandler(Looper.myLooper());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (!BuildConfig.TURN_RADIOS_ON) {
            stopSelf();
            return START_NOT_STICKY;
        }

        switch (intent.getIntExtra(KEY, 0)) {
            case MSG_WIFI_ON:

                if (hasWiFiMessages()) {
                    Logger.d("onStartCommand.hasWiFiMessages");
                    return START_STICKY;
                }

                if (!wifiGood()) {
                    Logger.d("onStartCommand.wifiNotGood");
                    scheduleWiFi();
                    return START_STICKY;
                }

                break;
            case MSG_BT_ON:

                if (hasBtMessages()) {
                    Logger.d("onStartCommand.hasBtMessages");
                    return START_STICKY;
                }

                if (!btGood()) {
                    Logger.d("onStartCommand.btNotGood");
                    scheduleBt();
                    return START_STICKY;
                }

                break;
        }
        Logger.d("onStartCommand.nothingToDo");
        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Logger.d("onDestroy");
        super.onDestroy();
    }

    //region helpers
    private boolean hasWiFiMessages() {
        return handler.hasMessages(MSG_WIFI_OFF) ||
                handler.hasMessages(MSG_WIFI_ON) ||
                handler.hasMessages(MSG_WIFI_CHECK);
    }

    private boolean hasBtMessages() {
        return handler.hasMessages(MSG_BT_OFF) ||
                handler.hasMessages(MSG_BT_ON);
    }


    private boolean wifiGood() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifi != null && wifi.isConnected();
    }

    private void removeWiFiMessages() {
        handler.removeMessages(MSG_WIFI_OFF);
        handler.removeMessages(MSG_WIFI_ON);
        handler.removeMessages(MSG_WIFI_CHECK);
    }

    private void removeBtMessages() {
        handler.removeMessages(MSG_BT_OFF);
        handler.removeMessages(MSG_BT_ON);
    }

    private void scheduleWiFi() {
        handler.sendMessageDelayed(handler.obtainMessage(MSG_WIFI_OFF), DLY_WIFI_OFF);
        handler.sendMessageDelayed(handler.obtainMessage(MSG_WIFI_ON), DLY_WIFI_ON);
        handler.sendMessageDelayed(handler.obtainMessage(MSG_WIFI_CHECK), DLY_WIFI_CHECK);
    }

    private void scheduleBt() {
        handler.sendMessageDelayed(handler.obtainMessage(MSG_BT_OFF, 0), DLY_BT_OFF);
        handler.sendMessageDelayed(handler.obtainMessage(MSG_BT_ON, 0), DLY_BT_ON);
    }

    private boolean btGood() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }
    //endregion

    private class ConnectionHandler extends Handler {

        ConnectionHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            WifiManager wiFiManager;
            BluetoothAdapter bluetoothAdapter;

            switch (msg.what) {
                case MSG_WIFI_OFF:
                    if (wifiGood()) {

                        Logger.d("handleMessage.MSG_WIFI_OFF.wifiGood");

                        removeWiFiMessages();
                        if (!hasBtMessages()) {
                            stopSelf();
                        }
                    } else {

                        Logger.d("handleMessage.MSG_WIFI_OFF.wifiNotGood");

                        wiFiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                        wiFiManager.setWifiEnabled(false);
                    }
                    break;
                case MSG_WIFI_ON:
                    if (wifiGood()) {

                        Logger.d("handleMessage.MSG_WIFI_ON.wifiGood");

                        removeWiFiMessages();
                        if (!hasBtMessages()) {
                            stopSelf();
                        }
                    } else {

                        installWiFiIfNeeded(CheckConnectionsService.this);

                        Logger.d("handleMessage.MSG_WIFI_ON.wifiNotGood");

                        wiFiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                        wiFiManager.setWifiEnabled(true);
                    }
                    break;
                case MSG_WIFI_CHECK:
                    if (wifiGood()) {

                        Logger.d("handleMessage.MSG_WIFI_CHECK.wifiGood");
                        requestSync();

                        removeWiFiMessages();
                        if (!hasBtMessages()) {
                            stopSelf();
                        }
                    } else {

                        Logger.d("handleMessage.MSG_WIFI_CHECK.wifiNotGood");

                        wiFiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                        wiFiManager.reassociate();
                        scheduleWiFi();
                    }
                    break;
                case MSG_BT_OFF:
                    if (btGood()) {

                        Logger.d("handleMessage.MSG_BT_OFF.btGood");

                        removeBtMessages();
                        if (!hasWiFiMessages()) {
                            stopSelf();
                        }
                    } else {

                        Logger.d("handleMessage.MSG_BT_OFF.btNotGood");

                        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        bluetoothAdapter.disable();
                    }
                    break;
                case MSG_BT_ON:

                    Logger.d("handleMessage.MSG_BT_ON");

                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    bluetoothAdapter.enable();

                    if (!hasWiFiMessages()) {
                        stopSelf();
                    }
                    break;
            }
        }
    }
}
