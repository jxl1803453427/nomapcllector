package com.example.signalcollection.network;


import com.example.signalcollection.network.NetWorkService;

import com.example.signalcollection.network.RetrofitUtil;

import retrofit2.Retrofit;

/**
 * Created by hehe on 2016/5/26.
 */
public class WorkService {

    public static NetWorkService.Work getWorkService() {
        Retrofit retrofitl = RetrofitUtil.getInstance();
        NetWorkService.Work work = retrofitl.create(NetWorkService.Work.class);
        return work;
    }
}
