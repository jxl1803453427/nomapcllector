package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;

/**
 * Created by hehe on 2016/7/22.
 */
public class AddBusinessBody {


    /**
     * areaName : 用户自行添加的美容院测试
     * refCityCode : SZ755
     * remark : 在这个地方找到了一个美容院
     * longitude : 22.33333
     * latitude : 109.34234
     * cityRegion : 南山区
     * cityStress : 石厦北三街
     * refAreaTypeCode : AT-BEAUTY
     * refAddUsername : nonmap001
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

   /* public String getCityRegion() {
        return cityRegion;
    }

    public void setCityRegion(String cityRegion) {
        this.cityRegion = cityRegion;
    }*/

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


    public long getRefRegionId() {
        return refRegionId;
    }

    public void setRefRegionId(long refRegionId) {
        this.refRegionId = refRegionId;
    }

    public String getRefAddUsername() {
        return refAddUsername;
    }

    public void setRefAddUsername(String refAddUsername) {
        this.refAddUsername = refAddUsername;
    }
}
