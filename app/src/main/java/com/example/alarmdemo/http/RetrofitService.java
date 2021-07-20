package com.example.alarmdemo.http;

import com.example.alarmdemo.bean.ApiResponse;
import com.example.alarmdemo.bean.ConfigBean;
import com.example.alarmdemo.bean.ItemBean;
import com.example.alarmdemo.bean.MsgBean;

import java.util.ArrayList;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface RetrofitService {
    @GET("config")
    Observable<ApiResponse<ConfigBean>> getConfig();

    @GET("msg")
    Observable<ApiResponse<MsgBean>> getMsg(@Query("deviceId") String deviceId);

    @GET("reply")
    Observable<ApiResponse<ResponseBody>> getReply(@Query("id") int id);

    @GET("list")
    Observable<ApiResponse<ArrayList<ItemBean>>> getList(@Query("deviceId") String deviceId);
}
