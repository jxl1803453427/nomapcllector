package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;

/**
 * Created by Konmin on 2016/9/11.
 */
public class RegisterResult {

    /**
     * {
     * "retCode": 0,
     * "data": "556720",
     * "msg": "OK"
     * }
     */

    @Expose
    private int retCode;
    @Expose
    private String data;
    @Expose
    private String msg;


    public int getRetCode() {
        return retCode;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
