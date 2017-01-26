package com.sensorberg.iot_autoupdate;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.orhanobut.logger.Logger;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


/**
 * Created by ronaldo on 1/25/17.
 */
public class Sync extends AbstractThreadedSyncAdapter {

    public Sync(Context context) {
        super(context, true, false);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Logger.i("--- onPerformSync ---");

        Updater updater = new Updater(getContext(), BuildConfig.HOCKEY_TOKEN, BuildConfig.HOCKEY_APP_ID, BuildConfig.HOCKEY_APP_PACKAGE_NAME);
        updater.check()
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            Logger.i("Sync completed");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "Sync error");
                    }

                    @Override
                    public void onComplete() {
                        Logger.i("Sync completed");
                    }
                });
    }
}
