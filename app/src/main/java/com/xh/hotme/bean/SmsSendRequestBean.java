package com.xh.hotme.bean;

import androidx.annotation.Keep;

/**
 * Created by liu hong liang on 2016/11/12.
 */
@Keep
public class SmsSendRequestBean extends BaseRequestBean {
    public final static String TYPE_BIND="BIND";
    public final static String TYPE_LOGIN="LOGIN";
    public final static String TYPE_UNBIND="UNBIND";
    public final static String TYPE_UPDATE_PHONE="UPDATE_PHONE";

    public String mobile;//	是	STRING	手机号
    public String smsType;//	是	STRING	此次发送短信类型 1 注册 2 登陆 3 修改密码 4 信息变更

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getSmsType() {
        return smsType;
    }

    public void setSmsType(String smstype) {
        this.smsType = smstype;
    }

    String sn;
    String mac;
    String deviceModel;
    String bluetoothUuid;
    String deviceName;
    int deviceType;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getBluetoothUuid() {
        return bluetoothUuid;
    }

    public void setBluetoothUuid(String bluetoothUuid) {
        this.bluetoothUuid = bluetoothUuid;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }
}
