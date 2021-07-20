package com.example.alarmdemo.mvp.contract;

import com.example.alarmdemo.bean.ApiResponse;
import com.example.alarmdemo.bean.ConfigBean;
import com.example.alarmdemo.bean.ItemBean;
import com.example.alarmdemo.bean.MsgBean;

import java.util.ArrayList;

import io.reactivex.Observable;
import okhttp3.ResponseBody;


public interface MainContract {
    interface View {
        void getConfigSuccess(ConfigBean configBean);

        void getFail(String msg);

        void getMsgSuccess(MsgBean msgBean);

        void getListSuccess(ArrayList<ItemBean> list);

        void getReplySuccess(ResponseBody body);
    }

    interface Presenter {
        void getConfig();

        void getMsg(String deviceId);

        void getList(String deviceId);

        void getReply(int id);
    }


    interface Model {
        Observable<ApiResponse<ConfigBean>> getConfig();

        Observable<ApiResponse<MsgBean>> getMsg(String deviceId);

        Observable<ApiResponse<ArrayList<ItemBean>>> getList(String deviceId);

        Observable<ApiResponse<ResponseBody>> getReply(int id);


    }
}
