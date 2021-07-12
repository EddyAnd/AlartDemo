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
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.easysocket.EasySocket;
import com.easysocket.config.EasySocketOptions;
import com.easysocket.connection.heartbeat.HeartManager;
import com.easysocket.entity.OriginReadData;
import com.easysocket.entity.SocketAddress;
import com.easysocket.interfaces.conn.ISocketActionListener;
import com.easysocket.interfaces.conn.SocketActionListener;
import com.easysocket.utils.LogUtil;
import com.example.easysocket.CallbackIDFactoryImpl;
import com.example.easysocket.message.ClientHeartBeat;
import com.google.gson.Gson;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    NormalAdapter normalAdapter;
    int num = 0;
    private Notification notification;
    private NotificationCompat.Builder builder;
    private NotificationManager manager;
    /**
     * 是否已经连接
     **/
    private boolean isConnected;

    /**
     * socket行为监听
     */
    private ISocketActionListener socketActionListener = new SocketActionListener() {
        /**
         * socket连接成功
         * @param socketAddress
         */
        @Override
        public void onSocketConnSuccess(SocketAddress socketAddress) {
            LogUtil.d("端口" + socketAddress.getPort() + "---> 连接成功");
            isConnected = true;
            startHeartbeat();
        }

        /**
         * socket连接失败
         * @param socketAddress
         * @param isNeedReconnect 是否需要重连
         */
        @Override
        public void onSocketConnFail(SocketAddress socketAddress, boolean isNeedReconnect) {
            LogUtil.d(socketAddress.getPort() + "端口" + "socket连接被断开，点击进行连接");
            isConnected = false;
        }

        /**
         * socket断开连接
         * @param socketAddress
         * @param isNeedReconnect 是否需要重连
         */
        @Override
        public void onSocketDisconnect(SocketAddress socketAddress, boolean isNeedReconnect) {
            LogUtil.d(socketAddress.getPort() + "端口" + "---> socket断开连接，是否需要重连：" + isNeedReconnect);
            isConnected = false;
        }

        /**
         * socket接收的数据
         * @param socketAddress
         * @param readData
         */
        @Override
        public void onSocketResponse(SocketAddress socketAddress, String readData) {
            LogUtil.d(socketAddress.getPort() + "端口" + "SocketActionListener收到数据-->" + readData);
            if (!TextUtils.isEmpty(readData) && readData.contains("data")) {
                JsonBean json = new Gson().fromJson(readData, JsonBean.class);
                if (json.ret == 0) {
                    Log.e("=====内容为====", json.data.content);
                    int id = randomTest();
                    Intent intent = new Intent(getApplicationContext(), NotificationDetailsActivity.class);
                    intent.putExtra("content", json.data.content);
                    intent.putExtra("id", id);
                    intent.putExtra("url", "www.baidu.com");
                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    notification(id, "www.baidu.com", json.data.content, pendingIntent);
                }
            }
        }

        @Override
        public void onSocketResponse(SocketAddress socketAddress, OriginReadData originReadData) {
            super.onSocketResponse(socketAddress, originReadData);
            LogUtil.d(socketAddress.getPort() + "端口" + "SocketActionListener收到数据-->" + originReadData.getBodyString());
        }
    };

    private int randomTest() {
        Random random = new Random();
        //范围内的随机数
        int min = 1;
        int max = 999;
        return random.nextInt(max - min + 1) + min;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        createConnect();
    }

    /**
     * 创建socket连接
     */
    private void createConnect() {
        if (isConnected) {
            Toast.makeText(MainActivity.this, "Socket已连接", Toast.LENGTH_SHORT).show();
            return;
        }
        // 初始化socket
        initEasySocket();
        // 监听socket行为
        EasySocket.getInstance().subscribeSocketAction(socketActionListener);
    }

    /**
     * 初始化EasySocket
     */
    private void initEasySocket() {
        // socket配置
        EasySocketOptions options = new EasySocketOptions.Builder()
                // 主机地址，请填写自己的IP地址，以getString的方式是为了隐藏作者自己的IP地址
                .setSocketAddress(new SocketAddress(getResources().getString(R.string.local_ip), 9090))
                .setCallbackIDFactory(new CallbackIDFactoryImpl())
                // 定义消息协议，方便解决 socket黏包、分包的问题，如果客户端定义了消息协议，那么
                // 服务端也要对应对应的消息协议，如果这里没有定义消息协议，服务端也不需要定义
                // .setReaderProtocol(new DefaultMessageProtocol())
                .build();

        // 创建一个socket连接
        EasySocket.getInstance()
                .createConnection(options, this);
    }

    /**
     * 启动心跳检测功能
     **/
    private void startHeartbeat() {
        // 心跳实例
        ClientHeartBeat clientHeartBeat = new ClientHeartBeat();
        clientHeartBeat.setMsgId("heart_beat");
        clientHeartBeat.setFrom("client");
        EasySocket.getInstance().startHeartBeat(clientHeartBeat.pack(), new HeartManager.HeartbeatListener() {
            // 用于判断当前收到的信息是否为服务器心跳，根据自己的实际情况实现
            @Override
            public boolean isServerHeartbeat(OriginReadData orginReadData) {
                try {
                    String s = orginReadData.getBodyString();
                    JSONObject jsonObject = new JSONObject(s);
                    if (jsonObject.has("msgId") && "heart_beat".equals(jsonObject.getString("msgId"))) {
                        LogUtil.d("---> 收到服务端心跳");
                        return true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
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
        RefreshLayout refreshLayout = (RefreshLayout) findViewById(R.id.refreshLayout);
        refreshLayout.setRefreshHeader(new ClassicsHeader(this));
        refreshLayout.setRefreshFooter(new ClassicsFooter(this));
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                AlarmManager.getInstance(getApplicationContext()).getList();
                refreshlayout.finishRefresh(2000);//传入false表示刷新失败
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                refreshlayout.finishLoadMore(2000);//传入false表示加载失败
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
                intent.putExtra("content","写死列表来的测试数据");
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
                        // TODO: 2021/7/12 测试代码
                        //if(list == null){
                        list.add(new ItemBean(1, "百度", "www.baidu.com"));
                        list.add(new ItemBean(2, "火辣辣", "www.huolala.cn"));
                        // }

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