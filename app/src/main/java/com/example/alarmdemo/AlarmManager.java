package com.example.alarmdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class AlarmManager {
    @SuppressLint("StaticFieldLeak")
    public static volatile AlarmManager instance = null;
    public AlarmCall mAlarmCall;
    public ListCall mListCall;

    public ConfigCall mConfigCall;
    private Retrofit mRetrofit;
    private Context mContext;
    private String mHost;
    public int mTime;

    private AlarmManager(Context context) {
        this.mContext = context;
        initHttpBase();
    }

    public static AlarmManager getInstance(Context ctx) {
        if (instance == null) {
            synchronized (AlarmManager.class) {
                if (instance == null) {
                    instance = new AlarmManager(ctx);
                }
            }
        }
        return instance;
    }

    private void initHttpBase() {
        if (TextUtils.isEmpty(mHost)) {
            mHost = "https://it.kiss250.com/app/";
        }
        mRetrofit = new Retrofit.Builder().baseUrl(mHost).addConverterFactory(GsonConverterFactory.create()).callbackExecutor(Executors.newSingleThreadExecutor()).build();

    }

    public ConfigCall getConfigCall() {
        return mConfigCall;
    }

    public void setConfigCall(ConfigCall mConfigCall) {
        this.mConfigCall = mConfigCall;
    }


    public void setAlarmCall(AlarmCall mAlarmCall) {
        this.mAlarmCall = mAlarmCall;
    }

    public void setListCall(ListCall mListCall) {
        this.mListCall = mListCall;
    }

    public void getConfig() {
        HttpList httpList = mRetrofit.create(HttpList.class);
        Call<ConfigBean> call = httpList.getConfig();
        call.enqueue(new Callback<ConfigBean>() {
            @Override
            public void onResponse(Call<ConfigBean> call, Response<ConfigBean> response) {
                if (response != null && response.body() != null) {
                    Log.e("=====返回config", new Gson().toJson(response.body()));
                    mTime = response.body().timer;
                    if (mConfigCall != null) {
                        mConfigCall.configCall(response.body());
                    }
                    if (!TextUtils.isEmpty(response.body().domain)) {
                        mHost = response.body().domain + "/app/";
                        initHttpBase();
                        getList();
                    }
                }


            }

            @Override
            public void onFailure(Call<ConfigBean> call, Throwable t) {
                Log.e("====", "list: 网络请求失败=" + t.getMessage());
            }
        });
    }

    public void postHttp() {
        HttpList httpList = mRetrofit.create(HttpList.class);
        Call<AlarmResponseBean> call = httpList.getAlarmSetting(DeviceIdUtils.getDeviceId(mContext));
        call.enqueue(new Callback<AlarmResponseBean>() {
            @Override
            public void onResponse(Call<AlarmResponseBean> call, Response<AlarmResponseBean> response) {
                AlarmResponseBean bean = response.body();
                Log.e("=====msg:", new Gson().toJson(bean));
                if (bean != null && !TextUtils.isEmpty(bean.title)) {
                    Calendar c = Calendar.getInstance();
                    if (mAlarmCall != null) {
                        mAlarmCall.callAlarm(bean, bean.title, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE) + 1);
                    }
                }
            }

            @Override
            public void onFailure(Call<AlarmResponseBean> call, Throwable t) {
                Log.e("====msg:", " 网络请求失败=" + t.getMessage());

            }
        });
    }

    public void getList() {
        HttpList httpList = mRetrofit.create(HttpList.class);
        Call<ArrayList<ItemBean>> call = httpList.getList(DeviceIdUtils.getDeviceId(mContext));
        call.enqueue(new Callback<ArrayList<ItemBean>>() {
            @Override
            public void onResponse(Call<ArrayList<ItemBean>> call, Response<ArrayList<ItemBean>> response) {
                ArrayList<ItemBean> bean = response.body();
                Log.e("=====返回list", new Gson().toJson(bean));
                if (mListCall != null) {
                    mListCall.listCall(bean);
                }

            }

            @Override
            public void onFailure(Call<ArrayList<ItemBean>> call, Throwable t) {
                Log.e("====", "list: 网络请求失败=" + t.getMessage());
            }
        });
    }

    public void postReply(int id) {
        HttpList httpList = mRetrofit.create(HttpList.class);
        Call<ResponseBody> call = httpList.replyId(id);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.e("====reply", "成功==" + response.code());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("====reply", "reply: 网络请求失败=" + t.getMessage());

            }
        });
    }

    /**
     * 开启服务
     */

    public void startAlarmService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mContext.startForegroundService(new Intent(mContext, AlarmService.class));
        } else {
            mContext.startService(new Intent(mContext, AlarmService.class));
        }
    }

    /**
     * 停止服务
     */

    public void stoptUpdateGpsService() {
        mContext.stopService(new Intent(mContext, AlarmService.class));
    }


    public interface AlarmCall {
        void callAlarm(AlarmResponseBean bean, String title, int hour, int minute);
    }

    public interface ListCall {
        void listCall(ArrayList<ItemBean> list);
    }

    public interface ConfigCall {
        void configCall(ConfigBean configBean);
    }

}
