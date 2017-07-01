package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Created by hehe on 2016/7/16.
 */
public class Exprop {


    /**
     * retCode : 0
     * data : [{"id":3,"propCode":"EXP-CATEGORY","propName":"品类","scope":1,"valueType":1,"controlType":2,"remark":"品类扩展属性"},{"id":1,"propCode":"EXP-BRAND","propName":"品牌","scope":1,"valueType":1,"controlType":2,"remark":"品牌扩展属性"}]
     * msg : OK
     */
    @Expose
    private int retCode;
    @Expose
    private String msg;
    /**
     * id : 3
     * propCode : EXP-CATEGORY
     * propName : 品类
     * scope : 1
     * valueType : 1
     * controlType : 2
     * remark : 品类扩展属性
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
        private String propCode;
        @Expose
        private String propName;
        @Expose
        private int scope;
        @Expose
        private int valueType;
        @Expose
        private int controlType;
        @Expose
        private String remark;

        @Expose
        private String searchApi;//搜索的时候需要的


        public String getSearchApi() {
            return searchApi;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getPropCode() {
            return propCode;
        }

        public void setPropCode(String propCode) {
            this.propCode = propCode;
        }

        public String getPropName() {
            return propName;
        }

        public void setPropName(String propName) {
            this.propName = propName;
        }

        public int getScope() {
            return scope;
        }

        public void setScope(int scope) {
            this.scope = scope;
        }

        public int getValueType() {
            return valueType;
        }

        public void setValueType(int valueType) {
            this.valueType = valueType;
        }

        public int getControlType() {
            return controlType;
        }

        public void setControlType(int controlType) {
            this.controlType = controlType;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }
}
