package com.xh.hotme.me;

import androidx.annotation.Keep;

/**
 * Create by zhaozhihui on 2019-09-15
 **/
@Keep
public class MeModuleBean {
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    int type;

    public MeModuleBean(int type){
        this.type = type;
    }

    public void onDestroy(){

    }
}
