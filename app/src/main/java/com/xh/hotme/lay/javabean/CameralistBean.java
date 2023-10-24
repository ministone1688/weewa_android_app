package com.xh.hotme.lay.javabean;

import com.google.common.collect.Lists;

import java.util.List;

public class CameralistBean {

    private List<Lists> rows;
    private Integer total;

    public void setLists(Integer total) {
        this.total = total;
    }
    public Integer getTotal() {
        return total;
    }

    public void setLists(List<Lists> lists) {
        this.rows = lists;
    }
    public List<Lists> getLists() {
        return rows;
    }

    public class Lists {

    }

}
