package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.List;

/**
 * Created by hehe on 2016/7/15.
 */
public class WorkListResult implements Serializable {


    /**
     * retCode : 0
     * data : [{"id":261,"areaCode":"A-TEMP-TEST-1","areaName":"无图采-测-深圳4S店测试地","refCityCode":"SZ755","remark":"测","longitude":0,"latitude":0,"cityRegion":"南山区","cityStress":"海天100路","address":"软基软基软基","refAreaTypeCode":"AT-4S","cityName":"深圳","areaTypeName":"4S店"},{"id":262,"areaCode":"A-TEMP-TEST-2","areaName":"无图采-测-深圳美容整形测试地","refCityCode":"SZ755","remark":"测","longitude":0,"latitude":0,"cityRegion":"南山区","cityStress":"海天100路","address":"软基软基软基","refAreaTypeCode":"AT-BEAUTY","cityName":"深圳","areaTypeName":"美容整形"}]
     * msg : OK
     */
    @Expose
    private int retCode;
    @Expose
    private String msg;
    /**
     * id : 261
     * areaCode : A-TEMP-TEST-1
     * areaName : 无图采-测-深圳4S店测试地
     * refCityCode : SZ755
     * remark : 测
     * longitude : 0
     * latitude : 0
     * cityRegion : 南山区
     * cityStress : 海天100路
     * address : 软基软基软基
     * refAreaTypeCode : AT-4S
     * cityName : 深圳
     * areaTypeName : 4S店
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

    public static class DataBean implements Serializable {
        @Expose
        private int id;
        @Expose
        private String areaCode;
        @Expose
        private String areaName;
        @Expose
        private String refCityCode;
        @Expose
        private double longitude;
        @Expose
        private double latitude;

        @Expose
        private String cityRegion;
        @Expose
        private String cityStress;
        @Expose
        private String address;
        @Expose
        private String refAreaTypeCode;
        @Expose
        private String cityName;
        @Expose
        private String areaTypeName;
        @Expose
        private String remark;
        @Expose
        private String remark1;
        @Expose
        private int statusResult;

        @Expose
        private int regionId;
        @Expose
        private int cityRegionId;


        public int getRegionId() {
            return regionId;
        }

        public void setRegionId(int regionId) {
            this.regionId = regionId;
        }

        public int getCityRegionId() {
            return cityRegionId;
        }

        public void setCityRegionId(int cityRegionId) {
            this.cityRegionId = cityRegionId;
        }

        public String getRemark1() {
            return remark1;
        }

        public void setRemark1(String remark1) {
            this.remark1 = remark1;
        }

        public int getStatusResult() {
            return statusResult;
        }

        public void setStatusResult(int statusResult) {
            this.statusResult = statusResult;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getAreaCode() {
            return areaCode;
        }

        public void setAreaCode(String areaCode) {
            this.areaCode = areaCode;
        }

        public String getAreaName() {
            return areaName;
        }

        public void setAreaName(String areaName) {
            this.areaName = areaName;
        }

        public String getRefCityCode() {
            return refCityCode;
        }

        public void setRefCityCode(String refCityCode) {
            this.refCityCode = refCityCode;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public String getCityRegion() {
            return cityRegion;
        }

        public void setCityRegion(String cityRegion) {
            this.cityRegion = cityRegion;
        }

        public String getCityStress() {
            return cityStress;
        }

        public void setCityStress(String cityStress) {
            this.cityStress = cityStress;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getRefAreaTypeCode() {
            return refAreaTypeCode;
        }

        public void setRefAreaTypeCode(String refAreaTypeCode) {
            this.refAreaTypeCode = refAreaTypeCode;
        }

        public String getCityName() {
            return cityName;
        }

        public void setCityName(String cityName) {
            this.cityName = cityName;
        }

        public String getAreaTypeName() {
            return areaTypeName;
        }

        public void setAreaTypeName(String areaTypeName) {
            this.areaTypeName = areaTypeName;
        }
    }
}
