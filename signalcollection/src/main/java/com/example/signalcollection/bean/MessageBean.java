package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * 消息
 * Created by Konmin on 2016/11/1.
 */

public class MessageBean extends DataSupport implements Serializable {

    /**
     * {"areaCode":"areaCode","areaName":"商圈名字","title":"标题呀","pushType":1,"retCode":0,"msg":"OK","pushTime":1478069966783,"remark":"备注"}
     */
    private int id;
    private String title;
    private long pushTime;
    private int retCode;
    private String areaCode;
    private String areaName;
    private int pushType;
    private String remark;
    private String msg;

    private long receciveTime;

    private String content;

    private int status;//0 -未读  1- 已读


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getPushTime() {
        return pushTime;
    }

    public void setPushTime(long pushTime) {
        this.pushTime = pushTime;
    }

    public int getRetCode() {
        return retCode;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
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


    public int getPushType() {
        return pushType;
    }

    public void setPushType(int pushType) {
        this.pushType = pushType;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getRececiveTime() {
        return receciveTime;
    }

    public void setRececiveTime(long receciveTime) {
        this.receciveTime = receciveTime;
    }
}
