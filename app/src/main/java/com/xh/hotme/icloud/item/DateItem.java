package com.xh.hotme.icloud.item;

public class DateItem extends ListItem{
    private String date;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    @Override
    public int getType(){
        return TYPE_DATE;
    }

    @Override
    public int getItemType() {
        return TYPE_DATE;
    }

    public DateItem(String date){
        this.date = date;
    }
}
