package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Konmin on 2017/3/9.
 */

public class SearchResult implements Serializable {


    /**
     * {
     * "retCode": 0,
     * "data": [
     * {
     * "id": 261,
     * "brandCode": "BRAND-BUSSINESS-KDJ-DB9 ",
     * "showName": "肯德基",
     * …
     * },
     * {
     * "id": 261,
     * "brandCode": "BRAND-BUSSINESS-DKS-DB8 ",
     * "showName": "德克士",
     * …
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
        private String brandCode;
        @Expose
        private String showName;
        @Expose
        private List<Exprop> exProps;

        public int getId() {
            return id;
        }

        public String getBrandCode() {
            return brandCode;
        }

        public String getShowName() {
            return showName;
        }

        public List<Exprop> getExProps() {
            return exProps;
        }
    }


    public static class Exprop {
        @Expose
        private String propCode;
        @Expose
        private String propName;
        @Expose
        private String valueName;
        @Expose
        private String valueCode;


        public String getPropCode() {
            return propCode;
        }

        public String getPropName() {
            return propName;
        }

        public String getValueName() {
            return valueName;
        }

        public String getValueCode() {
            return valueCode;
        }
    }

}
