package com.example.android.panoimageuploader;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import net.gotev.uploadservice.UploadServiceConfig;

public class PanoApplication extends Application {

    public static final String notificationChannelID = "TestChannel";

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT > 26) {
            NotificationChannel nc = new NotificationChannel(notificationChannelID, "TestApp", NotificationManager.IMPORTANCE_LOW);

            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            nm.createNotificationChannel(nc);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();

        UploadServiceConfig.initialize(
                this, notificationChannelID, BuildConfig.DEBUG
        );
    }
}
