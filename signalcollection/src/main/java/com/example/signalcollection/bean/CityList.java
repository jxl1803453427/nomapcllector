package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Created by hehe on 2016/7/22.
 */
public class CityList {


    /**
     * retCode : 0
     * data : [{"cityCode":"SZ755","cityName":"深圳"},{"cityCode":"GZ20","cityName":"广州"},{"cityCode":"TJ22","cityName":"天津"},{"cityCode":"CQ23","cityName":"重庆"},{"cityCode":"XA29","cityName":"西安"}]
     * msg : OK
     */
    @Expose
    private int retCode;
    @Expose
    private String msg;
    /**
     * cityCode : SZ755
     * cityName : 深圳
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
        private String cityCode;
        @Expose
        private String cityName;


        private String sortLetter;

        public String getCityCode() {
            return cityCode;
        }

        public void setCityCode(String cityCode) {
            this.cityCode = cityCode;
        }

        public String getCityName() {
            return cityName;
        }

        public String getSortLetter() {
            return sortLetter;
        }

        public void setSortLetter(String sortLetter) {
            this.sortLetter = sortLetter;
        }

        public void setCityName(String cityName) {
            this.cityName = cityName;
        }


    }
}
