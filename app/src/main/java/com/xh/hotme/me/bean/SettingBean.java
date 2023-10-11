package com.xh.hotme.me.bean;

public class SettingBean {
    private int type;
    private String name;
    private int icon;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public  SettingBean(int type, int iconId, String name){
        this.type = type;
        this.icon = iconId;
        this.name = name;
    }
}
