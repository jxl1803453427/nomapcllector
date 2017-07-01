package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;

/**
 * 用户信息
 * Created by hehe on 2016/7/15.
 */
public class UserInfoRequest {


    /**
     * userName : nonmap001
     * password : 53b646a41eca71b92dc1e0d5614b82426d4a9cea
     */
    @Expose
    private String userName;
    @Expose
    private String password;




    public UserInfoRequest() {

    }

    public UserInfoRequest(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
