package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;

/**
 * Created by hehe on 2016/7/22.
 */
public class AddBusinessResult {


    /**
     * retCode : 0
     * data : {"id":510,"areaCode":"A-NMPSZ755-USERADD-nonmap001-20160721185655","areaName":"用户自行添加的美容院测试 234 45","refCityCode":"SZ755","remark":"在这个地方又找到了一个美容院","longitude":22.3333302,"latitude":109.342339,"cityRegion":"南山区","cityStress":"石厦北三街","refAreaTypeCode":"AT-BEAUTY","dataSource":1,"userAddStatus":0,"cityName":"深圳","areaTypeName":"整形美容"}
     * msg : OK
     */
    @Expose
    private int retCode;
    /**
     * id : 510
     * areaCode : A-NMPSZ755-USERADD-nonmap001-20160721185655
     * areaName : 用户自行添加的美容院测试 234 45
     * refCityCode : SZ755
     * remark : 在这个地方又找到了一个美容院
     * longitude : 22.3333302
     * latitude : 109.342339
     * cityRegion : 南山区
     * cityStress : 石厦北三街
     * refAreaTypeCode : AT-BEAUTY
     * dataSource : 1
     * userAddStatus : 0
     * cityName : 深圳
     * areaTypeName : 整形美容
     */
    @Expose
    private DataBean data;
    @Expose
    private String msg;

    public int getRetCode() {
        return retCode;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static class DataBean {
        @Expose
        private int id;
        @Expose
        private String areaCode;
        @Expose
        private String areaName;
        @Expose
        private String refCityCode;
        @Expose
        private String remark;
        @Expose
        private double longitude;
        @Expose
        private double latitude;
        @Expose
        private String cityRegion;
        @Expose
        private String cityStress;
        @Expose
        private String refAreaTypeCode;
        @Expose
        private int dataSource;
        @Expose
        private int userAddStatus;
        @Expose
        private String cityName;
        @Expose
        private String areaTypeName;

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

        public String getRefAreaTypeCode() {
            return refAreaTypeCode;
        }

        public void setRefAreaTypeCode(String refAreaTypeCode) {
            this.refAreaTypeCode = refAreaTypeCode;
        }

        public int getDataSource() {
            return dataSource;
        }

        public void setDataSource(int dataSource) {
            this.dataSource = dataSource;
        }

        public int getUserAddStatus() {
            return userAddStatus;
        }

        public void setUserAddStatus(int userAddStatus) {
            this.userAddStatus = userAddStatus;
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
