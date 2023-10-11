package com.xh.hotme.bean;

import androidx.annotation.Keep;


@Keep
public class BindResultBean extends BaseRequestBean {
    private String mac;//	是	STRING	短信类型 1 注册 2 登陆 3 修改密码 4 信息变更

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}
