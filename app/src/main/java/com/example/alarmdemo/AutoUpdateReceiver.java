package com.example.alarmdemo;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.WindowManager;


public class AutoUpdateReceiver extends BroadcastReceiver {
    public static final String BC_ACTION = "com.ex.action.BC_ACTION";
    CountDownTimer timer;
    private String TAG = this.getClass().getSimpleName();
    private AlertDialog.Builder builder;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, AlarmService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(i);
        } else {
            context.startService(i);
        }


    }

    private void showConfirmDialog(Context context) {
        builder = new AlertDialog.Builder(context);
        builder.setTitle("提示")
                .setMessage("将在30秒后关机")
                .setCancelable(false)
                .setPositiveButton("取消",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (timer != null) timer.cancel();

                            }

                        });

        setShowDialogType(context, builder.create());

    }

    private void setShowDialogType(Context context, AlertDialog alertDialog) {
        int type;
        if (Build.VERSION.SDK_INT > 24) {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        } else if (Build.VERSION.SDK_INT > 18) {

            type = WindowManager.LayoutParams.TYPE_TOAST;
        } else {
            type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

        }

        alertDialog.getWindow().setType(type);
        alertDialog.show();
//开启倒计时，并设置倒计时时间（秒）
        startCountDownTimer(context, alertDialog, 30);

    }

    private void startCountDownTimer(final Context context, final AlertDialog alertDialog, int time) {
        timer = new CountDownTimer(time * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
//倒计时提示文字
                Log.i(TAG, "onTick time :" + millisUntilFinished);
                alertDialog.setMessage("将在" + (millisUntilFinished / 1000) + "关机");

            }

            @Override
            public void onFinish() {
//倒计时结束
                Log.i(TAG, "倒计时结束！");
                alertDialog.dismiss();

//倒计时结束执行定时的任务

//  shutdown(context);

            }

        };

        timer.start();
    }

    //shoutDown需要 系统权限才能执行否则会提示权限异常
    public void shutDown(Context context) {
        String action = "android.intent.action.ACTION_REQUEST_SHUTDOWN";
        String extraName = "android.intent.extra.USER_REQUESTED_SHUTDOWN";
        Intent intent = new Intent(action);
        intent.putExtra(extraName, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

    }

}
