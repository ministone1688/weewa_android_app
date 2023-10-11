package com.xh.hotme.icloud.item;

import com.chad.library.adapter.base.entity.MultiItemEntity;

public abstract class ListItem implements MultiItemEntity {
    public static final int TYPE_DATE = 0;
    public static final int TYPE_VIDEO_LIST = 1;
    public static final int TYPE_LOCAL_VIDEO_LIST = 2;

    abstract public int getType();
}
