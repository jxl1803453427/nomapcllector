package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Created by hehe on 2016/7/22.
 */
public class BusinessTypeList {


    /**
     * retCode : 0
     * data : [{"code":"AT-4S","name":"4S店"},{"code":"AT-JIANCAI","name":"建材"},{"code":"AT-JIAJU","name":"家具"},{"code":"AT-JIADIAN","name":"家电"},{"code":"AT-WEDDING-PHOTO","name":"婚纱摄影"},{"code":"AT-BEAUTY","name":"整形美容"}]
     * msg : OK
     */
    @Expose
    private int retCode;
    @Expose
    private String msg;
    /**
     * code : AT-4S
     * name : 4S店
     */
    @Expose
    private List<DataBean> data;

    public int getRetCode() {
        return retCode;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        @Expose
        private String code;
        @Expose
        private String name;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
