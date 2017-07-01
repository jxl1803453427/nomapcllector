package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;

/**
 * 缺省的返回结果
 * Created by hehe on 2016/7/15.
 */
public class DefaultResult {

    /**
     * retCode : 601
     * data : false
     * msg : 用户不存在
     */
    @Expose
    private int retCode;
    @Expose
    private boolean data;
    @Expose
    private String msg;

    public int getRetCode() {
        return retCode;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    public boolean isData() {
        return data;
    }

    public void setData(boolean data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "DefaultResult{" + "retCode=" + retCode + ", data=" + data + ", msg='" + msg + '\'' + '}';
    }
}
