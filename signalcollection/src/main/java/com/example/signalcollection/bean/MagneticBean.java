package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;
import com.yyc.mcollector.bean.MagneticData;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * 地磁信息
 * Created by Konmin on 2016/12/5.
 */

public class MagneticBean extends DataSupport implements Serializable {

    @Expose
    private Float x;
    @Expose
    private Float y;
    @Expose
    private Float z;
    @Expose
    private Float azimuth;
    @Expose
    private Float pitch;
    @Expose
    private Float roll;


    private NmpReportPoint nmpReportPoint;

    public Float getX() {
        return x;
    }

    public void setX(Float x) {
        this.x = x;
    }

    public Float getY() {
        return y;
    }

    public void setY(Float y) {
        this.y = y;
    }

    public Float getZ() {
        return z;
    }

    public void setZ(Float z) {
        this.z = z;
    }

    public Float getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(Float azimuth) {
        this.azimuth = azimuth;
    }

    public Float getPitch() {
        return pitch;
    }

    public void setPitch(Float pitch) {
        this.pitch = pitch;
    }

    public Float getRoll() {
        return roll;
    }

    public void setRoll(Float roll) {
        this.roll = roll;
    }


    public NmpReportPoint getNmpReportPoint() {
        return nmpReportPoint;
    }

    public void setNmpReportPoint(NmpReportPoint nmpReportPoint) {
        this.nmpReportPoint = nmpReportPoint;
    }

    public static MagneticBean createFromMagneticData(NmpReportPoint nmpReportPoint, MagneticData data) {
        MagneticBean bean = new MagneticBean();
        bean.setNmpReportPoint(nmpReportPoint);
        bean.setAzimuth(data.getAzimuth());
        bean.setPitch(data.getPitch());
        bean.setRoll(data.getRoll());
        bean.setX(data.getX());
        bean.setY(data.getY());
        bean.setZ(data.getZ());
        return bean;
    }
}
