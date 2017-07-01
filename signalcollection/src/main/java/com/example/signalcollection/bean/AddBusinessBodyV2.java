package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Created by hehe on 2016/7/22.
 */
public class AddBusinessBodyV2 {


    /**
     * {
     * "areaName": "用户自行添加的美容院测试",
     * "refCityCode": "SZ755",
     * "remark": "在这个地方找到了一个美容院",
     * "longitude": 22.33333,
     * "latitude": 109.34234,
     * "cityRegion": "南山区",
     * "cityStress": "石厦北三街",
     * "refAreaTypeCode": "AT-BEAUTY",
     * "refAddUsername": "nonmap001",
     * "exProps ":[
     * {
     * "refExpropCode":"EXP-STARS",
     * "propValue":"三星级", //上一个接口中返回的tagName
     * "tagCode":"tag0001", //上一个接口中返回的tagCode
     * },
     * {
     * "refExpropCode":"EXP-STYLE",
     * "propValue":"不知道的风格", //用户在其他项输入框录入的内容
     * "isOtherInput":1 //用户输入其他项时需要传此字段
     * },
     * {
     * "propCode":" EXP-CATEGORY ",
     * "propValue":"其他"
     * }
     * ]
     * }
     */

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
    private long refRegionId;
    @Expose
    private String cityStress;
    @Expose
    private String refAreaTypeCode;
    @Expose
    private String refAddUsername;
    @Expose
    private List<AreaExprop> exProps;


    @Expose
    private String uid;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public long getRefRegionId() {
        return refRegionId;
    }

    public void setRefRegionId(long refRegionId) {
        this.refRegionId = refRegionId;
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

    public String getRefAddUsername() {
        return refAddUsername;
    }

    public void setRefAddUsername(String refAddUsername) {
        this.refAddUsername = refAddUsername;
    }

    public List<AreaExprop> getExProps() {
        return exProps;
    }

    public void setExProps(List<AreaExprop> exProps) {
        this.exProps = exProps;
    }
}
