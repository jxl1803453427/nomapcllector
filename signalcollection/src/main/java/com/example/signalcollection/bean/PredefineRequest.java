package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;

/**
 * Created by hehe on 2016/7/16.
 */
public class PredefineRequest {

    /**
     * cpTypeCode : CP-JIANCAI-DP
     * exPropCode : EXP-CATEGORY
     */
    @Expose
    private String refCpTypeCode;
    @Expose
    private String refExPropCode;

    public String getRefCpTypeCode() {
        return refCpTypeCode;
    }

    public void setRefCpTypeCode(String refCpTypeCode) {
        this.refCpTypeCode = refCpTypeCode;
    }

    public String getRefExPropCode() {
        return refExPropCode;
    }

    public void setRefExPropCode(String refExPropCode) {
        this.refExPropCode = refExPropCode;
    }
}
