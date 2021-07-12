package com.example.alarmdemo;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.AlarmClock;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    NormalAdapter normalAdapter;
    int num = 0;
    private Notification notification;
    private NotificationCompat.Builder builder;
    private NotificationManager manager;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

//        if (intent != null && intent.getExtras() != null) {
//            PushEntity pushEntity = (PushEntity) intent.getExtras().getSerializable("PushMsg");
//            startActivityForNotification(pushEntity);
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SocketActivity.class));
            }
        });

        //初始标题栏
        Toolbar toolbar = findViewById(R.id.tb_register_back);
        setSupportActionBar(toolbar);
        //显示返回按钮
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("首页");
        }
        RefreshLayout refreshLayout = (RefreshLayout)findViewById(R.id.refreshLayout);
        refreshLayout.setRefreshHeader(new ClassicsHeader(this));
        refreshLayout.setRefreshFooter(new ClassicsFooter(this));
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                refreshlayout.finishRefresh(2000/*,false*/);//传入false表示刷新失败
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                refreshlayout.finishLoadMore(2000/*,false*/);//传入false表示加载失败
            }
        });
        normalAdapter = new NormalAdapter();
        normalAdapter.setClickItem(new NormalAdapter.ClickItem() {
            @Override
            public void clickItem(ItemBean itemBean) {
                Log.e("====点击的title为", itemBean.title);
                Intent intent = new Intent(getApplicationContext(), NotificationDetailsActivity.class);
                intent.putExtra("url", itemBean.url);
                intent.putExtra("id", itemBean.id);
                startActivity(intent);
            }
        });
        recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(normalAdapter);
        AlarmManager.getInstance(getApplicationContext()).getList();
        AlarmManager.getInstance(getApplicationContext()).setListCall(new AlarmManager.ListCall() {
            @Override
            public void listCall(final ArrayList<ItemBean> list) {
                Log.e("===list", new Gson().toJson(list));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        normalAdapter.setDatas(list);
                    }
                });

            }
        });

        AlarmManager.getInstance(getApplicationContext()).startUpdateGpsService();
        AlarmManager.getInstance(getApplicationContext()).setAlarmCall(new AlarmManager.AlarmCall() {
            @Override
            public void callAlarm(AlarmResponseBean bean, String title, int hour, int minute) {
                // wakeUpAndUnlock(getApplicationContext());
                Intent intent = new Intent(getApplicationContext(), NotificationDetailsActivity.class);
                intent.putExtra("content", bean.content);
                intent.putExtra("id", bean.id);
                intent.putExtra("url", bean.url);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), bean.id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                notification(bean.id, title, bean.content, pendingIntent);
            }
        });


    }

    //设置通知栏消息样式
    private void setNotification(int type) {
        //点击通知栏消息跳转页
        Intent intent = new Intent(this, NotificationDetailsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        //创建通知消息管理类
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(this)//创建通知消息实例
                .setContentTitle("我是标题")
                .setContentText("我是内容")
                .setWhen(System.currentTimeMillis())//通知栏显示时间
                .setSmallIcon(R.mipmap.ic_launcher)//通知栏小图标
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))//通知栏下拉是图标
                .setContentIntent(pendingIntent)//关联点击通知栏跳转页面
                .setPriority(NotificationCompat.PRIORITY_MAX)//设置通知消息优先级
                .setAutoCancel(true)//设置点击通知栏消息后，通知消息自动消失
//                .setSound(Uri.fromFile(new File("/system/MP3/music.mp3"))) //通知栏消息提示音
                .setVibrate(new long[]{0, 1000, 1000, 1000}) //通知栏消息震动
                .setLights(Color.GREEN, 1000, 2000) //通知栏消息闪灯(亮一秒间隔两秒再亮)
                .setDefaults(NotificationCompat.DEFAULT_ALL); //通知栏提示音、震动、闪灯等都设置为默认

        if (type == 1) {
            //短文本
            notification = builder.build();
            //Constant.TYPE1为通知栏消息标识符，每个id都是不同的
            manager.notify(Constant.TYPE1, notification);
        } else if (type == 2) {
            //长文本
            notification = builder.setStyle(new NotificationCompat.BigTextStyle().
                    bigText("我是长文字内容:　今年双十一结束后，一如既往又出现了一波冲动剁手党被理智唤醒的退货潮。不过，一位来自福建厦门的网友在这其中贡献了堪称历史里程碑式的高光时刻。别人退衣服退鞋子，而他要退的是一只蓝孔雀、一只宠物小香猪、还有一斤娃娃鱼……"))
                    .build();
            manager.notify(Constant.TYPE2, notification);
        } else {
            //带图片
            notification = builder.setStyle(new NotificationCompat.BigPictureStyle().
                    bigPicture(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher)))
                    .build();
            manager.notify(Constant.TYPE3, notification);
        }
    }

    //唤醒屏幕并解锁
    public void wakeUpAndUnlock(Context context) {
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
//        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
//        //解锁
//        kl.disableKeyguard();
        //获取电源管理器对象
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
        //点亮屏幕
        wl.acquire();
        //释放
        wl.release();
    }

    private void createAlarm(String message, int hour, int minutes) {
//        ArrayList<Integer> testDays = new ArrayList<>();
//        testDays.add(Calendar.MONDAY);//周一
//        testDays.add(Calendar.TUESDAY);//周二
//        testDays.add(Calendar.FRIDAY);//周五
//
        //action为AlarmClock.ACTION_SET_ALARM
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)
                //闹钟的小时
                .putExtra(AlarmClock.EXTRA_HOUR, hour)
                //闹钟的分钟
                .putExtra(AlarmClock.EXTRA_MINUTES, minutes)
                //响铃时提示的信息
                .putExtra(AlarmClock.EXTRA_MESSAGE, message)
                //用于指定该闹铃触发时是否振动
                .putExtra(AlarmClock.EXTRA_VIBRATE, true)
                //一个 content: URI，用于指定闹铃使用的铃声，也可指定 VALUE_RINGTONE_SILENT 以不使用铃声。
                //如需使用默认铃声，则无需指定此 extra。
                // .putExtra(AlarmClock.EXTRA_RINGTONE, ringtoneUri)
                //一个 ArrayList，其中包括应重复触发该闹铃的每个周日。
                // 每一天都必须使用 Calendar 类中的某个整型值（如 MONDAY）进行声明。
                //对于一次性闹铃，无需指定此 extra
                // .putExtra(AlarmClock.EXTRA_DAYS, testDays)
                //如果为true，则调用startActivity()不会进入手机的闹钟设置界面
                .putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void notification(int id, String title, String content, PendingIntent intent) {
        num++;
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = NotificationUtils.getPushNotificationChannel();
            manager.createNotificationChannel(channel);
        }
        long when = System.currentTimeMillis();
        Notification notification = new NotificationCompat.Builder(getApplicationContext(), NotificationUtils.CHANNEL_R_PUSH_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setWhen(when)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(intent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build();
        manager.notify((int) when, notification);
    }

    private void createAlarm(String message, int hour, int minutes, int resId) {
        ArrayList<Integer> testDays = new ArrayList<>();
        testDays.add(Calendar.MONDAY);//周一
        testDays.add(Calendar.TUESDAY);//周二
        testDays.add(Calendar.FRIDAY);//周五

        String packageName = getApplication().getPackageName();
        Uri ringtoneUri = Uri.parse("android.resource://" + packageName + "/" + resId);
        //action为AlarmClock.ACTION_SET_ALARM
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)
                //闹钟的小时
                .putExtra(AlarmClock.EXTRA_HOUR, hour)
                //闹钟的分钟
                .putExtra(AlarmClock.EXTRA_MINUTES, minutes)
                //响铃时提示的信息
                .putExtra(AlarmClock.EXTRA_MESSAGE, message)
                //用于指定该闹铃触发时是否振动
                .putExtra(AlarmClock.EXTRA_VIBRATE, true)
                //一个 content: URI，用于指定闹铃使用的铃声，也可指定 VALUE_RINGTONE_SILENT 以不使用铃声。
                //如需使用默认铃声，则无需指定此 extra。
                .putExtra(AlarmClock.EXTRA_RINGTONE, ringtoneUri)
                //一个 ArrayList，其中包括应重复触发该闹铃的每个周日。
                // 每一天都必须使用 Calendar 类中的某个整型值（如 MONDAY）进行声明。
                //对于一次性闹铃，无需指定此 extra
                .putExtra(AlarmClock.EXTRA_DAYS, testDays)
                //如果为true，则调用startActivity()不会进入手机的闹钟设置界面
                .putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }


}