package com.example.alarmdemo;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface HttpList {
    @GET("config")
    Call<ConfigBean> getConfig();

    @GET("msg")
    Call<AlarmResponseBean> getAlarmSetting(@Query("deviceId") String deviceId);

    @GET("reply")
    Call<ResponseBody> replyId(@Query("id") int id);

    @GET("list")
    Call<ArrayList<ItemBean>> getList(@Query("deviceId") String deviceId);
}
