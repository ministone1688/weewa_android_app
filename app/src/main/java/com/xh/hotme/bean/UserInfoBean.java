package com.xh.hotme.bean;

import androidx.annotation.Keep;

@Keep
public class UserInfoBean {
    public String nickName;
    public String phone;
    public String city;
    public String avatar;
    public int userId;
    public String userToken;

    public String toString() {
        return "nickName=" + nickName + ", phone=" + phone + ", city=" + city + ", token=" + userToken;
    }
}
