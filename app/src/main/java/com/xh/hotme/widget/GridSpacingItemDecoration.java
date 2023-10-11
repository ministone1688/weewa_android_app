package com.xh.hotme.widget;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.Keep;
import androidx.recyclerview.widget.RecyclerView;

@Keep
public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
    private final int spanCount;
    private final int vSpacing;
    private final int hSpacing;
    private final boolean includeEdge;
    private final int orientation;
    private int headerNum;

    public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.vSpacing = spacing;
        this.hSpacing = spacing;
        this.includeEdge = includeEdge;
        this.orientation = RecyclerView.VERTICAL;
    }

    public GridSpacingItemDecoration(int spanCount, int orientation, int vSpacing, int hSpacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.vSpacing = vSpacing;
        this.hSpacing = hSpacing;
        this.includeEdge = includeEdge;
        this.orientation = orientation;
    }

    public GridSpacingItemDecoration(int spanCount, int orientation, int spacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.vSpacing = spacing;
        this.hSpacing = spacing;
        this.includeEdge = includeEdge;
        this.orientation = orientation;
    }

    public GridSpacingItemDecoration(int spanCount, int orientation, int spacing, boolean includeEdge, int headerNum) {
        this.spanCount = spanCount;
        this.vSpacing = spacing;
        this.hSpacing = spacing;
        this.includeEdge = includeEdge;
        this.orientation = orientation;
        this.headerNum = headerNum;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view) - headerNum; // item position
        if (position >= 0) {
            if (this.orientation == RecyclerView.VERTICAL) {
                int column = position % spanCount; // item column

                if (includeEdge) {
                    outRect.left = hSpacing - column * hSpacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                    outRect.right = (column + 1) * hSpacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                    if (position < spanCount) { // top edge
                        outRect.top = vSpacing;
                    }
                    outRect.bottom = vSpacing; // item bottom
                } else {
                    outRect.left = column * hSpacing / spanCount; // column * ((1f / spanCount) * spacing)
                    outRect.right = hSpacing - (column + 1) * hSpacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                    if (position >= spanCount) {
                        outRect.top = vSpacing; // item top
                    }
                }
            } else {
                int row = position % spanCount; // item row
                if (includeEdge) {
                    outRect.top = vSpacing - row * vSpacing / spanCount; // spacing - row * ((1f / spanCount) * spacing)
                    outRect.bottom = (row + 1) * vSpacing / spanCount; // (row + 1) * ((1f / spanCount) * spacing)

                    if (position < spanCount) { // top edge
                        outRect.left = hSpacing;
                    }
                    outRect.right = hSpacing; // item right
                } else {
                    outRect.top = row * vSpacing / spanCount; // row * ((1f / spanCount) * spacing)
                    outRect.bottom = vSpacing - (row + 1) * vSpacing / spanCount; // spacing - (row + 1) * ((1f /    spanCount) * spacing)
                    if (position >= spanCount) {
                        outRect.left = hSpacing; // item left
                    }
                }
            }
        } else {
            if (this.orientation == RecyclerView.VERTICAL) {
                outRect.left = 0;
                outRect.right = 0;
                outRect.top = 0;
                outRect.bottom = 0;
            } else {
                if (includeEdge) {
                    outRect.left = hSpacing;
                } else {
                    outRect.left = 0;
                }
                outRect.right = 0;
                outRect.top = 0;
                outRect.bottom = 0;
            }
        }
    }
}