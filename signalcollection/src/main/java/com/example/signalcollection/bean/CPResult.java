package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Created by hehe on 2016/7/15.
 */
public class CPResult {


    /**
     * retCode : 0
     * data : [{"id":2,"cpTypeCode":"CP-JIANCAI-DP","cpTypeName":"店铺","refAreaTypeCode":"AT-JIANCAI","remark":"建材-店铺"},{"id":1,"cpTypeCode":"CP-JIANCAI-LKD","cpTypeName":"路口","refAreaTypeCode":"AT-JIANCAI","remark":"建材-路口"}]
     * msg : OK
     */
    @Expose
    private int retCode;

    @Expose
    private String msg;
    /**
     * id : 2
     * cpTypeCode : CP-JIANCAI-DP
     * cpTypeName : 店铺
     * refAreaTypeCode : AT-JIANCAI
     * remark : 建材-店铺
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

    public static class DataBean {
        @Expose
        private int id;
        @Expose
        private String cpTypeCode;
        @Expose
        private String cpTypeName;
        @Expose
        private String refAreaTypeCode;
        @Expose
        private String remark;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getCpTypeCode() {
            return cpTypeCode;
        }

        public void setCpTypeCode(String cpTypeCode) {
            this.cpTypeCode = cpTypeCode;
        }

        public String getCpTypeName() {
            return cpTypeName;
        }

        public void setCpTypeName(String cpTypeName) {
            this.cpTypeName = cpTypeName;
        }

        public String getRefAreaTypeCode() {
            return refAreaTypeCode;
        }

        public void setRefAreaTypeCode(String refAreaTypeCode) {
            this.refAreaTypeCode = refAreaTypeCode;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }
}
