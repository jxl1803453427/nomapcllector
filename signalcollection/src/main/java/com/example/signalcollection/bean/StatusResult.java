package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Created by Konmin on 2017/5/17.
 */

public class StatusResult {


    @Expose
    private int retCode;
    @Expose
    private String msg;


    @Expose
    private List<Status> data;


    public static class Status {
        @Expose
        private String areaCode;
        @Expose
        private int status;


        public String getAreaCode() {
            return areaCode;
        }

        public int getStatus() {
            return status;
        }
    }


    public int getRetCode() {
        return retCode;
    }

    public String getMsg() {
        return msg;
    }

    public List<Status> getData() {
        return data;
    }
}
