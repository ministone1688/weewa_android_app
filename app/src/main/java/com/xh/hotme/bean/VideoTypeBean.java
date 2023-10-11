package com.xh.hotme.bean;

public class VideoTypeBean {
    public int type;
    public String name;
    public boolean isSelect;

    public int iconResSelect;
    public int iconResUnselect;

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

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public int getIconResSelect() {
        return iconResSelect;
    }

    public void setIconResSelect(int iconResSelect) {
        this.iconResSelect = iconResSelect;
    }

    public int getIconResUnselect() {
        return iconResUnselect;
    }

    public void setIconResUnselect(int iconResUnselect) {
        this.iconResUnselect = iconResUnselect;
    }
}
