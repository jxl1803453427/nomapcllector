package com.example.signalcollection.bean;

import android.support.annotation.RawRes;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Created by Konmin on 2016/9/11.
 */
public class RegionList {


    @Expose
    private int retCode;
    @Expose
    private String msg;


    @Expose
    private List<DataBean> data;


    public int getRetCode() {
        return retCode;
    }

    public String getMsg() {
        return msg;
    }

    public List<DataBean> getData() {
        return data;
    }

    public static class DataBean {

        /**
         * "id": 5,
         * "cityRegionCode": "REGION-SZ755-LGQ",
         * "cityRegionName": "龙岗区",
         * "refCityCode": "SZ755",
         * "remark": null
         */

        @Expose
        private int id;
        @Expose
        private String cityRegionCode;
        @Expose
        private String cityRegionName;
        @Expose
        private String refCityCode;
        @Expose
        private String remark;


        public int getId() {
            return id;
        }

        public String getCityRegionCode() {
            return cityRegionCode;
        }

        public String getCityRegionName() {
            return cityRegionName;
        }

        public String getRefCityCode() {
            return refCityCode;
        }

        public String getRemark() {
            return remark;
        }
    }
}
