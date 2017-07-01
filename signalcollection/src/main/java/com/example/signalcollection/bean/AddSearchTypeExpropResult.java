package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.List;

/**
 * 搜索类型的扩展属性的返回
 * Created by Konmin on 2017/3/14.
 */

public class AddSearchTypeExpropResult implements Serializable {

    /**
     * {
     * "retCode": 0,
     * "data": [
     * {
     * "id": 3,
     * "propCode": "EXP-CATEGORY",
     * "propName": "品类",
     * "scope": 1,
     * "valueType": 1,
     * "controlType": 4,
     * "remark": "品类扩展属性",
     * "searchApi": "",
     * "predefines": [
     * "鞋帽",
     * "服装"
     * ]
     * },
     * {
     * "id": 4,
     * "propCode": "EXP-STYLE",
     * "propName": "风格",
     * "scope": 1,
     * "valueType": 1,
     * "controlType": 2,
     * "remark": "风格扩展属性",
     * "searchApi": null,
     * "predefines": [
     * " 中式",
     * " 欧式"
     * ]
     * }
     * ],
     * "msg": "OK"
     * }
     */

    @Expose
    private int retCode;
    @Expose
    private String msg;

    @Expose
    private List<Data> data;


    public int getRetCode() {
        return retCode;
    }

    public String getMsg() {
        return msg;
    }

    public List<Data> getData() {
        return data;
    }

    public static class Data {
        @Expose
        private int id;
        @Expose
        private String propCode;
        @Expose
        private String propName;
        @Expose
        private String remark;
        @Expose
        private String searchApi;
        @Expose
        private int scope;
        @Expose
        private int valueType;
        @Expose
        private int controlType;
        @Expose
        private List<String> predefines;

        public int getId() {
            return id;
        }

        public String getPropCode() {
            return propCode;
        }

        public String getPropName() {
            return propName;
        }

        public String getRemark() {
            return remark;
        }

        public String getSearchApi() {
            return searchApi;
        }

        public int getScope() {
            return scope;
        }

        public int getValueType() {
            return valueType;
        }

        public int getControlType() {
            return controlType;
        }

        public List<String> getPredefines() {
            return predefines;
        }
    }


}
