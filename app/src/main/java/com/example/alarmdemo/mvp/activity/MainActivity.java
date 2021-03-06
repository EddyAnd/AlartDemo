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
import com.example.alarmdemo.R;
import com.example.alarmdemo.bean.ConfigBean;
import com.example.alarmdemo.bean.ItemBean;
import com.example.alarmdemo.bean.JsonBean;
import com.example.alarmdemo.bean.MsgBean;
import com.example.alarmdemo.http.RetrofitManager;
import com.example.alarmdemo.mvp.contract.MainContract;
import com.example.alarmdemo.mvp.model.MainModel;
import com.example.alarmdemo.mvp.presenter.MainPresenter;
import com.example.alarmdemo.mvp.view.adapter.NormalAdapter;
import com.example.alarmdemo.service.AlarmService;
import com.example.alarmdemo.utils.DeviceIdUtils;
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

import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity implements MainContract.View {
    RecyclerView recyclerView;
    NormalAdapter normalAdapter;
    MainPresenter mMainPresenter;
    int num = 0;
    /**
     * ??????????????????
     **/
    private boolean isConnected;

    /**
     * socket????????????
     */
    private ISocketActionListener socketActionListener = new SocketActionListener() {
        /**
         * socket????????????
         * @param socketAddress
         */
        @Override
        public void onSocketConnSuccess(SocketAddress socketAddress) {
            LogUtil.d("??????" + socketAddress.getPort() + "---> ????????????");
            isConnected = true;
            startHeartbeat();
        }

        /**
         * socket????????????
         * @param socketAddress
         * @param isNeedReconnect ??????????????????
         */
        @Override
        public void onSocketConnFail(SocketAddress socketAddress, boolean isNeedReconnect) {
            LogUtil.d(socketAddress.getPort() + "??????" + "socket????????????????????????????????????");
            isConnected = false;
        }

        /**
         * socket????????????
         * @param socketAddress
         * @param isNeedReconnect ??????????????????
         */
        @Override
        public void onSocketDisconnect(SocketAddress socketAddress, boolean isNeedReconnect) {
            LogUtil.d(socketAddress.getPort() + "??????" + "---> socket????????????????????????????????????" + isNeedReconnect);
            isConnected = false;
        }

        /**
         * socket???????????????
         * @param socketAddress
         * @param readData
         */
        @Override
        public void onSocketResponse(SocketAddress socketAddress, String readData) {
            LogUtil.d(socketAddress.getPort() + "??????" + "SocketActionListener????????????-->" + readData);
            if (!TextUtils.isEmpty(readData) && readData.contains("data")) {
                JsonBean json = new Gson().fromJson(readData, JsonBean.class);
                if (json.ret == 0) {
                    Log.e("=====?????????====", json.data.content);
                    int id = randomTest();
                    Intent intent = new Intent(getApplicationContext(), NotificationDetailsActivity.class);
                    intent.putExtra("content", json.data.content);
                    intent.putExtra("id", id);
                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    notification(id, json.data.title, json.data.content, pendingIntent);
                }
            }
        }

        @Override
        public void onSocketResponse(SocketAddress socketAddress, OriginReadData originReadData) {
            super.onSocketResponse(socketAddress, originReadData);
            LogUtil.d(socketAddress.getPort() + "??????" + "SocketActionListener????????????-->" + originReadData.getBodyString());
        }
    };

    private int randomTest() {
        Random random = new Random();
        //?????????????????????
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
        mMainPresenter = new MainPresenter(this, new MainModel());
        initView();

    }

    /**
     * ??????socket??????
     */
    private void createConnect(String ip, int port) {
        if (isConnected) {
            Toast.makeText(MainActivity.this, "Socket?????????", Toast.LENGTH_SHORT).show();
            return;
        }
        // ?????????socket
        initEasySocket(ip, port);
        // ??????socket??????
        EasySocket.getInstance().subscribeSocketAction(socketActionListener);
    }

    /**
     * ?????????EasySocket
     */
    private void initEasySocket(String ip, int port) {
        // socket??????
        EasySocketOptions options = new EasySocketOptions.Builder()
                // ?????????????????????????????????IP????????????getString???????????????????????????????????????IP??????
                .setSocketAddress(new SocketAddress(ip, port))
                .setCallbackIDFactory(new CallbackIDFactoryImpl())
                // ????????????????????????????????? socket????????????????????????????????????????????????????????????????????????
                // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
                // .setReaderProtocol(new DefaultMessageProtocol())
                .build();

        // ????????????socket??????
        EasySocket.getInstance()
                .createConnection(options, this);
    }

    /**
     * ????????????????????????
     **/
    private void startHeartbeat() {
        // ????????????
        ClientHeartBeat clientHeartBeat = new ClientHeartBeat();
        clientHeartBeat.setMsgId("heart_beat");
        clientHeartBeat.setFrom("client");
        EasySocket.getInstance().startHeartBeat(clientHeartBeat.pack(), new HeartManager.HeartbeatListener() {
            // ?????????????????????????????????????????????????????????????????????????????????????????????
            @Override
            public boolean isServerHeartbeat(OriginReadData orginReadData) {
                try {
                    String s = orginReadData.getBodyString();
                    JSONObject jsonObject = new JSONObject(s);
                    if (jsonObject.has("msgId") && "heart_beat".equals(jsonObject.getString("msgId"))) {
                        LogUtil.d("---> ?????????????????????");
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

        //???????????????
        Toolbar toolbar = findViewById(R.id.tb_register_back);
        setSupportActionBar(toolbar);
        //??????????????????
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("??????");
        }
        RefreshLayout refreshLayout = (RefreshLayout) findViewById(R.id.refreshLayout);
        refreshLayout.setRefreshHeader(new ClassicsHeader(this));
        refreshLayout.setRefreshFooter(new ClassicsFooter(this));
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                mMainPresenter.getList(DeviceIdUtils.getDeviceId(getApplicationContext()));
                refreshlayout.finishRefresh(2000);//??????false??????????????????
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                refreshlayout.finishLoadMore(2000);//??????false??????????????????
            }
        });
        normalAdapter = new NormalAdapter();
        normalAdapter.setClickItem(new NormalAdapter.ClickItem() {
            @Override
            public void clickItem(ItemBean itemBean) {
                Log.e("====?????????title???", itemBean.title);
                Intent intent = new Intent(getApplicationContext(), NotificationDetailsActivity.class);
                intent.putExtra("url", itemBean.url);
                intent.putExtra("id", itemBean.id);
                startActivity(intent);
            }
        });
        recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(normalAdapter);
        mMainPresenter.getConfig();
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
    public void getConfigSuccess(ConfigBean configBean) {
        if (configBean == null) {
            Toast.makeText(this, "????????????????????????", Toast.LENGTH_LONG).show();
            return;

        }
        createConnect(configBean.socket_domain, Integer.parseInt(configBean.socket_port));
        if (!TextUtils.isEmpty(configBean.domain)) {
            RetrofitManager.getInstance().setHost(configBean.domain + "/app/");
            startAlarmService(configBean.timer);
            //getList();
        }
    }

    @Override
    public void getFail(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void getMsgSuccess(MsgBean msgBean) {
        Intent intent = new Intent(getApplicationContext(), NotificationDetailsActivity.class);
        intent.putExtra("id", msgBean.id);
        intent.putExtra("url", msgBean.url);
        intent.putExtra("content", msgBean.content);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), msgBean.id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification(msgBean.id, msgBean.title, msgBean.content, pendingIntent);
    }

    @Override
    public void getListSuccess(ArrayList<ItemBean> list) {
        normalAdapter.setDatas(list);
    }

    @Override
    public void getReplySuccess(ResponseBody body) {
        Log.e("====reply", "??????==");
    }

    /**
     * ????????????
     */

    public void startAlarmService(int interval) {
        Intent intent = new Intent(getApplicationContext(), AlarmService.class);
        intent.putExtra("time_interval", interval);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    /**
     * ????????????
     */

    public void stopUpdateGpsService() {
        stopService(new Intent(getApplicationContext(), AlarmService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopUpdateGpsService();
        EasySocket.getInstance().disconnect(false);
        EasySocket.getInstance().destroyConnection();
    }
}