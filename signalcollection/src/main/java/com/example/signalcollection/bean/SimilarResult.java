package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.List;

/**
 * 相似商圈
 * Created by Konmin on 2017/5/26.
 */

public class SimilarResult implements Serializable {

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
    private Data data;
    @Expose
    private String msg;


    public int getRetCode() {
        return retCode;
    }

    public Data getData() {
        return data;
    }

    public String getMsg() {
        return msg;
    }

    public static class Data implements Serializable {

        @Expose
        private List<String> segmentAreaNames;
        @Expose
        private List<WorkListResult.DataBean> similarAreas;


        public List<String> getSegmentAreaNames() {
            return segmentAreaNames;
        }

        public List<WorkListResult.DataBean> getSimilarAreas() {
            return similarAreas;
        }
    }

}
