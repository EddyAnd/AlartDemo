package com.example.alarmdemo.mvp.model;

import com.example.alarmdemo.bean.ApiResponse;
import com.example.alarmdemo.bean.ConfigBean;
import com.example.alarmdemo.bean.ItemBean;
import com.example.alarmdemo.bean.MsgBean;
import com.example.alarmdemo.http.RetrofitManager;
import com.example.alarmdemo.mvp.contract.MainContract;

import java.util.ArrayList;

import io.reactivex.Observable;
import okhttp3.ResponseBody;


public class MainModel implements MainContract.Model {

    @Override
    public Observable<ApiResponse<ConfigBean>> getConfig() {
        return RetrofitManager.getInstance().getRetrofitService().getConfig();
    }

    @Override
    public Observable<ApiResponse<MsgBean>> getMsg(String deviceId) {
        return RetrofitManager.getInstance().getRetrofitService().getMsg(deviceId);
    }

    @Override
    public Observable<ApiResponse<ArrayList<ItemBean>>> getList(String deviceId) {
        return RetrofitManager.getInstance().getRetrofitService().getList(deviceId);
    }

    @Override
    public Observable<ApiResponse<ResponseBody>> getReply(int id) {
        return RetrofitManager.getInstance().getRetrofitService().getReply(id);
    }
}