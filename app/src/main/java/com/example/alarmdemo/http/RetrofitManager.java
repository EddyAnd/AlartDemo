package com.example.alarmdemo.http;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class RetrofitManager {
    @SuppressLint("StaticFieldLeak")
    public static volatile RetrofitManager instance = null;
    private Retrofit mRetrofit;
    private String mHost;

    private RetrofitManager() {
        initHttpBase();
    }

    public static RetrofitManager getInstance() {
        if (instance == null) {
            synchronized (RetrofitManager.class) {
                if (instance == null) {
                    instance = new RetrofitManager();
                }
            }
        }
        return instance;
    }

    public void setHost(String mHost) {
        this.mHost = mHost;
        initHttpBase();
    }

    private void initHttpBase() {
        if (TextUtils.isEmpty(mHost)) {
            mHost = "https://it.kiss250.com/app/";
        }
        mRetrofit = new Retrofit.Builder().baseUrl(mHost).addConverterFactory(GsonConverterFactory.create()).addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build();

    }

    public RetrofitService getRetrofitService() {
        return mRetrofit.create(RetrofitService.class);
    }


}
