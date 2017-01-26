package com.sensorberg.iot_autoupdate;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.ContentResolver;
import android.os.Bundle;
import android.text.TextUtils;

import com.orhanobut.logger.Logger;

/**
 * Created by ronaldo on 1/25/17.
 */
public class App extends Application {

    public static final String AUTHORITY = "com.sensorberg.android.datasync.provider";
    public static final String ACCOUNT_TYPE = "sensorberg.com";
    public static final String ACCOUNT = "auto-update";

    @Override
    public void onCreate() {
        super.onCreate();

        if (TextUtils.isEmpty(BuildConfig.HOCKEY_APP_ID) ||
                TextUtils.isEmpty(BuildConfig.HOCKEY_APP_PACKAGE_NAME) ||
                TextUtils.isEmpty(BuildConfig.HOCKEY_TOKEN)) {
            throw new RuntimeException("Cannot run without hockey access. " +
                    "Check build.gradle and " +
                    "add the necessary parameters on secret.properties or " +
                    "as a environment variable");
        }

        Logger.init("IoT-Update").hideThreadInfo().methodCount(1);
        Account account = new Account(ACCOUNT, ACCOUNT_TYPE);
        AccountManager am = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        if (am.addAccountExplicitly(account, null, null)) {
            Logger.d("Account added successfully");
            ContentResolver.addPeriodicSync(account, AUTHORITY, Bundle.EMPTY, 3601);
            ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
            ContentResolver.setMasterSyncAutomatically(true);
        } else {
            Logger.v("Account already exist");
        }
    }
}
