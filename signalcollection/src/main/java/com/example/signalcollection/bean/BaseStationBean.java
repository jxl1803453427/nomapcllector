package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;
import com.yyc.mcollector.bean.BaseStationData;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * 基站信息
 * Created by Konmin on 2016/12/5.
 */

public class BaseStationBean extends DataSupport implements Serializable {

    @Expose
    private Integer type;
    @Expose
    private Integer mcc;
    @Expose
    private Integer mnc;
    @Expose
    private Integer lac;
    @Expose
    private Integer cid;
    @Expose
    private Integer tac;
    @Expose
    private Integer ci;
    @Expose
    private Integer pci;
    @Expose
    private Integer psc;
    @Expose
    private Integer sid;
    @Expose
    private Integer nid;
    @Expose
    private Integer bid;
    @Expose
    private Integer dBm;
    @Expose
    private Integer asuLevel;
    @Expose
    private Integer level;

    private NmpReportPoint nmpReportPoint;



    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getMcc() {
        return mcc;
    }

    public void setMcc(Integer mcc) {
        this.mcc = mcc;
    }

    public Integer getMnc() {
        return mnc;
    }

    public void setMnc(Integer mnc) {
        this.mnc = mnc;
    }

    public Integer getLac() {
        return lac;
    }

    public void setLac(Integer lac) {
        this.lac = lac;
    }

    public Integer getCid() {
        return cid;
    }

    public void setCid(Integer cid) {
        this.cid = cid;
    }

    public Integer getTac() {
        return tac;
    }

    public void setTac(Integer tac) {
        this.tac = tac;
    }

    public Integer getCi() {
        return ci;
    }

    public void setCi(Integer ci) {
        this.ci = ci;
    }

    public Integer getPci() {
        return pci;
    }

    public void setPci(Integer pci) {
        this.pci = pci;
    }

    public Integer getPsc() {
        return psc;
    }

    public void setPsc(Integer psc) {
        this.psc = psc;
    }

    public Integer getSid() {
        return sid;
    }

    public void setSid(Integer sid) {
        this.sid = sid;
    }

    public Integer getNid() {
        return nid;
    }

    public void setNid(Integer nid) {
        this.nid = nid;
    }

    public Integer getBid() {
        return bid;
    }

    public void setBid(Integer bid) {
        this.bid = bid;
    }

    public Integer getdBm() {
        return dBm;
    }

    public void setdBm(Integer dBm) {
        this.dBm = dBm;
    }

    public Integer getAsuLevel() {
        return asuLevel;
    }

    public void setAsuLevel(Integer asuLevel) {
        this.asuLevel = asuLevel;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public NmpReportPoint getNmpReportPoint() {
        return nmpReportPoint;
    }

    public void setNmpReportPoint(NmpReportPoint nmpReportPoint) {
        this.nmpReportPoint = nmpReportPoint;
    }


    public static BaseStationBean creatFromBaseStationData(NmpReportPoint point, BaseStationData data) {

        BaseStationBean baseStationBean = new BaseStationBean();
        baseStationBean.setNmpReportPoint(point);
        baseStationBean.setMnc(data.getMnc());
        baseStationBean.setMcc(data.getMcc());
        baseStationBean.setCid(data.getCid());
        baseStationBean.setCi(data.getCi());
        baseStationBean.setBid(data.getBid());
        baseStationBean.setLac(data.getLac());
        baseStationBean.setTac(data.getTac());
        baseStationBean.setType(data.getType());
        baseStationBean.setPsc(data.getPsc());
        baseStationBean.setPci(data.getPci());
        baseStationBean.setSid(data.getSid());
        baseStationBean.setNid(data.getNid());
        baseStationBean.setAsuLevel(data.getAsuLevel());
        baseStationBean.setdBm(data.getdBm());
        baseStationBean.setLevel(data.getLevel());
        return baseStationBean;

    }
}
