package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Created by hehe on 2016/7/16.
 */
public class Predefine {


    /**
     * retCode : 0
     * data : ["瓷砖石材","布衣软装","灯具开关","地板地暖","卫浴五金","厨房电器","卫浴洁具","橱柜","门窗"]
     * msg : OK
     */
    @Expose
    private int retCode;
    @Expose
    private String msg;
    @Expose
    private List<Data> data;


    public int getRetCode() {
        return retCode;
    }

    public String getMsg() {
        return msg;
    }

    public List<Data> getData() {
        return data;
    }

    public static class Data {

        @Expose
        private int id;
        @Expose
        private String tagName;
        @Expose
        private String tagCode;
        @Expose
        private int needOtherInput;//0 不需要其他输入，1需要其他输入

        public int getId() {
            return id;
        }

        public String getTagName() {
            return tagName;
        }

        public String getTagCode() {
            return tagCode;
        }

        public int getNeedOtherInput() {
            return needOtherInput;
        }
    }
}
