package com.xh.hotme.bean;

import java.io.Serializable;

public class NetworkBean implements Serializable {
    NetworkStatus wifi;
    NetworkStatus eth;
    NetworkStatus sim;
    ApInfo ap;

    int http;  //http 监听1端口

    public static class NetworkStatus implements Serializable{
        public int st;    //连接状态
        public String ip;   //ip地址
        public String s;  //wifi有用 wifi ssid
        public int has_card;  //4G有用 0未插卡1:已插卡
    }

    public NetworkStatus getWifi() {
        return wifi;
    }

    public void setWifi(NetworkStatus wifi) {
        this.wifi = wifi;
    }

    public NetworkStatus getEth() {
        return eth;
    }

    public void setEth(NetworkStatus eth) {
        this.eth = eth;
    }

    public NetworkStatus getSim() {
        return sim;
    }

    public void setSim(NetworkStatus sim) {
        this.sim = sim;
    }

    public int getHttp() {
        return http;
    }

    public void setHttp(int http) {
        this.http = http;
    }


    public static class ApInfo  implements Serializable{
        public String ssid;
        public String password;
        public int st;
    }

    public ApInfo getAp() {
        return ap;
    }

    public void setAp(ApInfo ap) {
        this.ap = ap;
    }

    public String toString(){
        StringBuilder builder =new StringBuilder();
        builder.append("{");

        if(wifi!=null){
            builder.append("wifi: st" + wifi.st +", ip="+ wifi.ip +"\n");
        }

        if(eth!=null){
            builder.append("eth: st" + eth.st +", ip="+ eth.ip +"\n");
        }

        if(sim!=null){
            builder.append("sim: st" + sim.st +", ip="+ sim.ip +"\n");
        }

        if(ap!=null){
            builder.append("ap: ssid" + ap.ssid +", password="+ ap.password +", st="+ ap.st +"\n");
        }

        builder.append("http port: "+ http +"\n");

        builder.append("}");

        return builder.toString();
    }
}
