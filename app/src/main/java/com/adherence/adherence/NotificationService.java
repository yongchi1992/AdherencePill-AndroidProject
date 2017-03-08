package com.adherence.adherence;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by suhon_000 on 11/6/2015.
 */
public class NotificationService extends Service {
    NotificationManager mManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        // code to execute when the service is first created
        super.onCreate();
        Log.i("TAG", "Service Started.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showNotification();
        return START_NOT_STICKY;
    }

    private void showNotification() {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Scheduled Notification");
        builder.setContentText("Content");
        builder.setTicker("New notification alert");
        builder.setSmallIcon(R.drawable.ic_launcher);
        Notification notification =  builder.build();

        mManager = (NotificationManager) this.getApplicationContext().getSystemService(this.getApplicationContext().NOTIFICATION_SERVICE);
        mManager.notify(0, notification);
    }

    @Override
    public void onDestroy() {
        mManager.cancel(0);
        super.onDestroy();
    }
}
