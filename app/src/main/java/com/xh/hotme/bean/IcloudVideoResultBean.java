package com.xh.hotme.bean;

import androidx.annotation.Keep;

import java.io.Serializable;
import java.util.List;

@Keep
public class IcloudVideoResultBean implements Serializable {

    int total;

    List<IcloudVideoBean> rows;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<IcloudVideoBean> getRows() {
        return rows;
    }

    public void setRows(List<IcloudVideoBean> rows) {
        this.rows = rows;
    }
}
