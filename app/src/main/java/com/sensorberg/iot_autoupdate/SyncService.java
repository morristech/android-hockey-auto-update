package com.sensorberg.iot_autoupdate;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by ronaldo on 1/25/17.
 */
public class SyncService extends Service {
    private Sync sync;

    @Override
    public void onCreate() {
        super.onCreate();
        sync = new Sync(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return sync.getSyncAdapterBinder();
    }
}
