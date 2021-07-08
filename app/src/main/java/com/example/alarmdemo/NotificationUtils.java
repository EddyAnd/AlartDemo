package com.example.alarmdemo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.os.Build;

public class NotificationUtils {
    public static String CHANNEL_R_PUSH_ID = "TEST";
    public static String CHANNEL_R_PUSH_DESC = "TEST_ALARM";
    public static String CHANNEL_R_PUSH_NAME = "王哲凡";

    public static NotificationChannel getPushNotificationChannel() {
        // Android8.0及以上NotificationChannel机制
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_R_PUSH_ID, CHANNEL_R_PUSH_NAME, importance);
            // 配置通知渠道的属性
            mChannel.setDescription(CHANNEL_R_PUSH_DESC);
            // 设置通知出现时的闪灯（如果 android 设备支持的话）
            mChannel.enableLights(true);
            // 设置通知出现时声音，默认通知是有声音的
            mChannel.setSound(null, null);
            mChannel.setLightColor(Color.RED);
            // 设置通知出现时的震动（如果 android 设备支持的话）
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            return mChannel;
        } else {
            return null;
        }
    }

}
