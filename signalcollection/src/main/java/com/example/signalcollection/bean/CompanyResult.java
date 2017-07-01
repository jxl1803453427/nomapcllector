package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Created by Konmin on 2016/9/11.
 */
public class CompanyResult {
    /**
     * {
     * "retCode": 0,
     * "data": [
     * {
     * "companyCode": "CM_SELF",
     * "companyName": "译元成自有兼职"
     * },
     * {
     * "companyCode": "CM_LAOWU1",
     * "companyName": "劳务外包公司1"
     * },
     * {
     * "companyCode": "CM_LAOWU2",
     * "companyName": "劳务外包公司2"
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

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    public static class Data {

        @Expose
        private String companyCode;
        @Expose
        private String companyName;

        public String getCompanyCode() {
            return companyCode;
        }

        public void setCompanyCode(String companyCode) {
            this.companyCode = companyCode;
        }

        public String getCompanyName() {
            return companyName;
        }

        public void setCompanyName(String companyName) {
            this.companyName = companyName;
        }
    }


}
