package com.xh.hotme.bean;

public class TextModelBean {
    public int type;
    public String name;
    public boolean isSelect;

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

    public TextModelBean(int type, String name){
        this.type = type;
        this.name = name;
    }

    public TextModelBean(int type, String name, boolean isSelect){
        this.type = type;
        this.name = name;
        this.isSelect = isSelect;
    }
}
