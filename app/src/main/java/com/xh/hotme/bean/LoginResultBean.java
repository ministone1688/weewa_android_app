package com.xh.hotme.bean;

/**
 * Created by liu hong liang on 2016/11/11.
 */
public class LoginResultBean {
    private String token;    //STRING	用户在平台的用户ID

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LoginResultBean(String t){
        token = t;
    }
}
