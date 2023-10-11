package com.xh.hotme.bean;

import androidx.annotation.Keep;


@Keep
public class UnbindRequestBean extends BaseRequestBean {
    private String mobile;//	是	STRING	玩家注册手机号
    private String mac;//	是	STRING	短信类型 1 注册 2 登陆 3 修改密码 4 信息变更
    private String code;//	是	STRING	短信校验码
//    private String password;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String smscode) {
        this.code = smscode;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}
