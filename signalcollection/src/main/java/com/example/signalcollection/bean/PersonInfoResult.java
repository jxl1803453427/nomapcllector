package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;

/**
 * Created by Konmin on 2017/2/15.
 */

public class PersonInfoResult {

    /**
     * {
     * "retCode": 0,
     * "data": {
     * "id": 1556,
     * "userName": "zzxx",
     * "userPwd": "7c4a8d09ca3762af61e59520943dc26494f8941b",
     * "userEmail": null,
     * "userPhone": "15800038674",
     * "userStatus": 1,
     * "lastLoginTime": 1487146637474,
     * "remark": "                                    q\r\n                                ",
     * "trueName": "ZKM",
     * "identityId": "441427199501170037",
     * "bankCardNumber": "6225000100018834847",
     * "bankName": "中国银行",
     * "bankCity": "广州",
     * "alipayId": null,
     * "userType": null,
     * "refCompanyCode": "CM-SELF"
     * },
     * "msg": "OK"
     * }
     */

    @Expose
    private int retCode;

    @Expose
    private String msg;

    @Expose
    private PersonInfo data;

    public int getRetCode() {
        return retCode;
    }

    public String getMsg() {
        return msg;
    }

    public PersonInfo getData() {
        return data;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setData(PersonInfo data) {
        this.data = data;
    }

    public static class PersonInfo {
        @Expose
        private String trueName;
        @Expose
        private String identityId;
        @Expose
        private String bankCardNumber;
        @Expose
        private String bankName;
        @Expose
        private String bankCity;
        @Expose
        private int id;


        private boolean updataBankCard;


        private String bankCardUrl;

        private String idCardFrontUrl;

        private String idCardBackgroundUrl;

        public String getTrueName() {
            return trueName;
        }

        public void setTrueName(String trueName) {
            this.trueName = trueName;
        }

        public String getIdentityId() {
            return identityId;
        }

        public void setIdentityId(String identityId) {
            this.identityId = identityId;
        }

        public String getBankCardNumber() {
            return bankCardNumber;
        }

        public void setBankCardNumber(String bankCardNumber) {
            this.bankCardNumber = bankCardNumber;
        }

        public String getBankName() {
            return bankName;
        }

        public void setBankName(String bankName) {
            this.bankName = bankName;
        }

        public String getBankCity() {
            return bankCity;
        }

        public void setBankCity(String bankCity) {
            this.bankCity = bankCity;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getBankCardUrl() {
            return bankCardUrl;
        }

        public void setBankCardUrl(String bankCardUrl) {
            this.bankCardUrl = bankCardUrl;
        }

        public String getIdCardFrontUrl() {
            return idCardFrontUrl;
        }

        public void setIdCardFrontUrl(String idCardFrontUrl) {
            this.idCardFrontUrl = idCardFrontUrl;
        }

        public String getIdCardBackgroundUrl() {
            return idCardBackgroundUrl;
        }

        public void setIdCardBackgroundUrl(String idCardBackgroundUrl) {
            this.idCardBackgroundUrl = idCardBackgroundUrl;
        }


        public boolean isUpdataBankCard() {
            return updataBankCard;
        }

        public void setUpdataBankCard(boolean updataBankCard) {
            this.updataBankCard = updataBankCard;
        }
    }


}
