package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

/**
 * 新增商圈的扩展属性
 * Created by Konmin on 2017/4/18.
 */

public class AreaExprop implements Serializable {

    @Expose
    private String refExpropCode;
    @Expose
    private String propValue;
    @Expose
    private String tagCode;
    @Expose
    private Integer isOtherInput;


    public String getRefExpropCode() {
        return refExpropCode;
    }

    public void setRefExpropCode(String refExpropCode) {
        this.refExpropCode = refExpropCode;
    }

    public String getPropValue() {
        return propValue;
    }

    public void setPropValue(String propValue) {
        this.propValue = propValue;
    }

    public String getTagCode() {
        return tagCode;
    }

    public void setTagCode(String tagCode) {
        this.tagCode = tagCode;
    }

    public Integer getIsOtherInput() {
        return isOtherInput;
    }

    public void setIsOtherInput(Integer isOtherInput) {
        this.isOtherInput = isOtherInput;
    }
}
