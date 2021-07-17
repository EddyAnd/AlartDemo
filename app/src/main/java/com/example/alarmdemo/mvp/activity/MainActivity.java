package com.example.alarmdemo.mvp.activity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
import com.example.alarmdemo.AlarmManager;
import com.example.alarmdemo.R;
import com.example.alarmdemo.bean.AlarmResponseBean;
import com.example.alarmdemo.bean.ConfigBean;
import com.example.alarmdemo.bean.ItemBean;
import com.example.alarmdemo.bean.JsonBean;
import com.example.alarmdemo.mvp.view.adapter.NormalAdapter;
import com.example.alarmdemo.utils.NotificationUtils;
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
import java.util.Random;

public class MainActivity extends AppCompatActivity implements AlarmManager.ConfigCall {
    RecyclerView recyclerView;
    NormalAdapter normalAdapter;
    int num = 0;
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
                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    notification(id,  json.data.title, json.data.content, pendingIntent);
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

    }

    /**
     * 创建socket连接
     */
    private void createConnect(String ip, int port) {
        if (isConnected) {
            Toast.makeText(MainActivity.this, "Socket已连接", Toast.LENGTH_SHORT).show();
            return;
        }
        // 初始化socket
        initEasySocket(ip, port);
        // 监听socket行为
        EasySocket.getInstance().subscribeSocketAction(socketActionListener);
    }

    /**
     * 初始化EasySocket
     */
    private void initEasySocket(String ip, int port) {
        // socket配置
        EasySocketOptions options = new EasySocketOptions.Builder()
                // 主机地址，请填写自己的IP地址，以getString的方式是为了隐藏作者自己的IP地址
                .setSocketAddress(new SocketAddress(ip, port))
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
        AlarmManager.getInstance(getApplication()).setConfigCall(this);
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
                startActivity(intent);
            }
        });
        recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(normalAdapter);
        AlarmManager.getInstance(getApplicationContext()).getConfig();

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


        AlarmManager.getInstance(getApplicationContext()).setAlarmCall(new AlarmManager.AlarmCall() {
            @Override
            public void callAlarm(AlarmResponseBean bean, String title, int hour, int minute) {
                // wakeUpAndUnlock(getApplicationContext());
                Intent intent = new Intent(getApplicationContext(), NotificationDetailsActivity.class);
                intent.putExtra("id", bean.id);
                intent.putExtra("url", bean.url);
                intent.putExtra("content", bean.content);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), bean.id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                notification(bean.id, bean.title, bean.content, pendingIntent);
            }
        });


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


    @Override
    public void configCall(ConfigBean configBean) {
        createConnect(configBean.socket_domain, Integer.parseInt(configBean.socket_port));
        AlarmManager.getInstance(getApplicationContext()).startAlarmService();
    }
}