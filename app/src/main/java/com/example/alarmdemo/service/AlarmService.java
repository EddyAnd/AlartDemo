package com.example.alarmdemo.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;

import androidx.annotation.Nullable;


public class AlarmService extends Service {
    private static String CHANNEL_ID = "gps";
    private static String CHANNEL_NAME = "update_gps";
    private int mAcquisitionInterval = 5;// 10秒采集一次
    @Override
    public void onCreate() {
        super.onCreate();

        startForeground();
    }


    private void startForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_MIN);
            NotificationManager manager = (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
            Notification notification = new Notification.Builder(getApplicationContext(), CHANNEL_ID).build();
            startForeground(1, notification);
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mAcquisitionInterval = intent.getExtras().getInt("time_interval");
        //  判断应用是否在前后台
        startForeground();
        // TODO: 2021/7/19 怎么调用getMsg
        android.app.AlarmManager manager = (android.app.AlarmManager) getSystemService(ALARM_SERVICE);
        long triggerAtTime = SystemClock.elapsedRealtime() + mAcquisitionInterval * 1000;
        Intent intent2 = new Intent(this, AutoUpdateReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent2, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            manager.setExact(android.app.AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        } else {
            manager.set(android.app.AlarmManager.RTC_WAKEUP, triggerAtTime, pi);
        }
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();


    }

}
