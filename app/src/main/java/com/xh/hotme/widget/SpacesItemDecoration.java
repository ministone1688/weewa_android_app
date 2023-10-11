package com.xh.hotme.widget;

import android.graphics.Rect;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by zzh on 2018/4/24.
 */

public    class SpacesItemDecoration extends RecyclerView.ItemDecoration {

    private final int space;
    private boolean isVerical=true;

    public SpacesItemDecoration(int space, boolean direct ) {
        this.space = space;
        this.isVerical =direct;
    }
    public SpacesItemDecoration(int space) {
        this.space = space;
    }
    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        outRect.left = space;
        outRect.right = space;
        outRect.bottom = space;
        outRect.top = space;

        // Add top margin only for the first item to avoid double space between items
//        if (parent.getChildPosition(view) == 0)
//            outRect.top = space;
        if (parent.getChildPosition(view) == 0) {
            if (isVerical) {
                outRect.top = space;
            } else {
                outRect.left = space;
            }
        }
    }
}
