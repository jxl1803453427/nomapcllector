package com.example.signalcollection.bean;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.util.List;

/**
 * 无地图的上报数据,单个点
 * Created by bukp on 16/7/15.
 */
public class NmpReportPoint extends DataSupport implements Serializable {


    private int id;

    @Expose
    private Integer pointIndex;//点序号，由楼层和自编号构成

    // @Expose
    //private String pointName;//这个好像不用了吧

    @Expose
    private long createTime;//创建数据用

    @Expose
    private String pointTypeCode;//采集点类型code
    @Expose
    private String pointType;//采集点类型

    @Expose
    private Integer floorNumber;//楼层编号

    @Expose
    private List<WifiSignalItem> signals;//采集点的WiFi信号

    @Expose
    private MagneticBean magnetic;//采集点的地磁信息

    @Expose
    private List<BaseStationBean> baseStations;//采集点的基站信息

   // @Expose
    //private List<BleSignal> bleSignals;//采集点蓝牙信息

    private NmpReportData nmpReportData;//关联数据用的


    //@Expose
    //private List<String> photoUrls;//照片列表


    @Expose
    private List<PhotoUrl> photoList;//


    //private String strPhotoUrls;//数据库保存url用，json


    @Expose
    private List<Exprop> exPropList;//扩展属性


    private String strExprop;//数据库保存扩展属性使用 json


    private int remark;//标注点,为了防止 任务提交后，任何被打回，重新领取，这些采集点会被加上

    private String samePageCode;//一个页面双采集点，标识同一页面采集的两个点

    /**
     * 扩展属性
     */
    public static class Exprop implements Serializable {
        @Expose
        private String refExPropCode;//code

        @Expose
        private String propValue;//扩展属性值,传给服务器用的//上一个接口中返回的tagName

        @Expose
        private String tagCode; ////上一个接口中返回的tagCode

        //@Column(ignore = true)
        // private String propName;//属性值的名字，为了显示用的


        @Expose
        private Integer isOtherInput;//是否是其他输入的


        public String getTagCode() {
            return tagCode;
        }

        public void setTagCode(String tagCode) {
            this.tagCode = tagCode;
        }


        public String getRefExPropCode() {
            return refExPropCode;
        }

        public void setRefExPropCode(String refExPropCode) {
            this.refExPropCode = refExPropCode;
        }

        public String getPropValue() {
            return propValue;
        }

        public void setPropValue(String propValue) {
            this.propValue = propValue;
        }

        public Integer getIsOtherInput() {
            return isOtherInput;
        }

        public void setIsOtherInput(Integer isOtherInput) {
            this.isOtherInput = isOtherInput;
        }
    }


    public Integer getPointIndex() {
        return pointIndex;
    }

    public void setPointIndex(Integer pointIndex) {
        this.pointIndex = pointIndex;
    }

    public String getSamePageCode() {
        return samePageCode;
    }

    public void setSamePageCode(String samePageCode) {
        this.samePageCode = samePageCode;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getPointTypeCode() {
        return pointTypeCode;
    }

    public void setPointTypeCode(String pointTypeCode) {
        this.pointTypeCode = pointTypeCode;
    }

    public String getPointType() {
        return pointType;
    }

    public void setPointType(String pointType) {
        this.pointType = pointType;
    }

    public Integer getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(Integer floorNumber) {
        this.floorNumber = floorNumber;
    }

    public List<WifiSignalItem> getSignals() {
        return signals;
    }

    public void setSignals(List<WifiSignalItem> signals) {
        this.signals = signals;
    }

    public MagneticBean getMagnetic() {
        return magnetic;
    }

    public void setMagnetic(MagneticBean magnetic) {
        this.magnetic = magnetic;
    }

    public List<BaseStationBean> getBaseStations() {
        return baseStations;
    }

    public void setBaseStations(List<BaseStationBean> baseStations) {
        this.baseStations = baseStations;
    }

    public NmpReportData getNmpReportData() {
        return nmpReportData;
    }

    public void setNmpReportData(NmpReportData nmpReportData) {
        this.nmpReportData = nmpReportData;
    }


   /* public List<String> getPhotoUrls() {
        return photoUrls;
    }

    public void setPhotoUrls(List<String> photoUrls) {

        Gson gson = new Gson();
        strPhotoUrls = gson.toJson(photoUrls);
        this.photoUrls = photoUrls;
    }*/

    public List<Exprop> getExPropList() {
        return exPropList;
    }

    public void setExPropList(List<Exprop> exPropList) {
        Gson gson = new Gson();
        strExprop = gson.toJson(exPropList);
        this.exPropList = exPropList;
    }

    public int getRemark() {
        return remark;
    }

    public void setRemark(int remark) {
        this.remark = remark;
    }


    /*public String getStrPhotoUrls() {
        return strPhotoUrls;
    }*/

    public String getStrExprop() {
        return strExprop;
    }

    public int getId() {
        return id;
    }


    /*public List<BleSignal> getBleSignals() {
        return bleSignals;
    }

    public void setBleSignals(List<BleSignal> bleSignals) {
        this.bleSignals = bleSignals;
    }*/

    public List<PhotoUrl> getPhotoList() {
        return photoList;
    }

    public void setPhotoList(List<PhotoUrl> photoList) {
        this.photoList = photoList;
    }
}
