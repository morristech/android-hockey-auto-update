package com.sensorberg.iot_autoupdate;

import android.accounts.Account;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.orhanobut.logger.Logger;

import static com.sensorberg.iot_autoupdate.App.ACCOUNT;
import static com.sensorberg.iot_autoupdate.App.ACCOUNT_TYPE;
import static com.sensorberg.iot_autoupdate.App.AUTHORITY;
import static com.sensorberg.iot_autoupdate.Util.exist;
import static com.sensorberg.iot_autoupdate.Util.launchAPK;
import static com.sensorberg.iot_autoupdate.Util.requestSync;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
            StringBuilder sb = new StringBuilder();
            for (String permission : pi.requestedPermissions) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append("<uses-permission android:name=\"");
                sb.append(permission);
                sb.append("\" /> ");
                sb.append(haveDeclaredPermissions(permission));
            }
            Logger.d(sb);
        } catch (Exception e) {
            Logger.e("Failed to get permission list", e);
        }
        requestSync();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (exist(this, BuildConfig.HOCKEY_APP_PACKAGE_NAME)) {
            launchAPK(this, BuildConfig.HOCKEY_APP_PACKAGE_NAME);
            finish();
        }
    }

    private String haveDeclaredPermissions(String permissions) {
        PackageManager pm = getPackageManager();
        String packageName = getPackageName();
        if (pm.checkPermission(permissions, packageName) != PackageManager.PERMISSION_GRANTED) {
            return "denied";
        } else {
            return "granted";
        }
    }

    // oauth token: f6728d0d454643bbba440fc4e2af4e45


}
