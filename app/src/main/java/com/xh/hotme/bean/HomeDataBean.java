package com.xh.hotme.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xh.hotme.bluetooth.Device;

import java.util.List;

public class HomeDataBean  implements MultiItemEntity {
    int type;

    public List<? extends Object> dataList;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<? extends Object> getDataList() {
        return dataList;
    }

    public void setDataList(List<? extends Object> dataList) {
        this.dataList = dataList;
    }

    @Override
    public int getItemType() {
        return type;
    }
}
