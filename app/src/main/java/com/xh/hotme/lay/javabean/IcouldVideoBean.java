package com.xh.hotme.lay.javabean;

import com.google.common.collect.Lists;

import java.util.List;

public class IcouldVideoBean {

    private List<Lists> lists;
    public void setLists(List<Lists> lists) {
        this.lists = lists;
    }
    public List<Lists> getLists() {
        return lists;
    }

    public static class Lists {
        private String title;
        private String litpic;

        public void setTitle(String title) {
            this.title = title;
        }
        public String getTitle() {
            return title;
        }

        public void setLitpic(String litpic) {
            this.litpic = litpic;
        }
        public String getLitpic() {
            return litpic;
        }
    }
}
