package com.example.alarmdemo.mvp.presenter;

import com.example.alarmdemo.bean.ApiResponse;
import com.example.alarmdemo.bean.ConfigBean;
import com.example.alarmdemo.bean.ItemBean;
import com.example.alarmdemo.bean.MsgBean;
import com.example.alarmdemo.mvp.contract.MainContract;

import java.util.ArrayList;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class MainPresenter implements MainContract.Presenter {
    public MainContract.View mView;
    public MainContract.Model mModel;

    public MainPresenter(MainContract.View view, MainContract.Model model) {
        mView = view;
        mModel = model;
    }

    @Override
    public void getConfig() {
        mModel.getConfig().subscribeOn(Schedulers.io())//请求数据的事件发生在io线程
                .observeOn(AndroidSchedulers.mainThread())//请求完成后在主线程更显UI
                .subscribe(new Observer<ApiResponse<ConfigBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ApiResponse<ConfigBean> configBeanApiResponse) {
                        mView.getConfigSuccess(configBeanApiResponse.getData());
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.getFail(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public void getMsg(String deviceId) {
        mModel.getMsg(deviceId).subscribeOn(Schedulers.io())//请求数据的事件发生在io线程
                .observeOn(AndroidSchedulers.mainThread())//请求完成后在主线程更显UI
                .subscribe(new Observer<ApiResponse<MsgBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ApiResponse<MsgBean> alarmResponseBeanApiResponse) {
                        mView.getMsgSuccess(alarmResponseBeanApiResponse.getData());
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.getFail(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public void getList(String deviceId) {
        mModel.getList(deviceId).subscribeOn(Schedulers.io())//请求数据的事件发生在io线程
                .observeOn(AndroidSchedulers.mainThread())//请求完成后在主线程更显UI
                .subscribe(new Observer<ApiResponse<ArrayList<ItemBean>>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ApiResponse<ArrayList<ItemBean>> response) {
                        mView.getListSuccess(response.getData());
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.getFail(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public void getReply(int id) {
        mModel.getReply(id).subscribeOn(Schedulers.io())//请求数据的事件发生在io线程
                .observeOn(AndroidSchedulers.mainThread())//请求完成后在主线程更显UI
                .subscribe(new Observer<ApiResponse<ResponseBody>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ApiResponse<ResponseBody> responseBodyApiResponse) {
                        mView.getReplySuccess(responseBodyApiResponse.getData());
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.getFail(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
