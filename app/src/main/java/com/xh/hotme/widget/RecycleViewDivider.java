package com.xh.hotme.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by zzh on 2018/3/20.
 */

public class RecycleViewDivider extends RecyclerView.ItemDecoration {
    /**
     * RecyclerView的布局方向，默认先赋值
     * 为纵向布局
     * RecyclerView 布局可横向，也可纵向
     * 横向和纵向对应的分割想画法不一样
     */
    private int mOrientation = LinearLayoutManager.VERTICAL;

    /**
     * item之间分割线的size，默认为1
     */
    private int mItemSize = 2;

    /**
     * 绘制item分割线的画笔，和设置其属性
     * 来绘制个性分割线
     */
    private final Paint mPaint;

    /**
     * 构造方法传入布局方向，不可不传
     *
     * @param context
     * @param orientation
     */
    public RecycleViewDivider(Context context, int orientation, int color) {
        //样式的方向
        this.mOrientation = orientation;
        if (orientation != LinearLayoutManager.VERTICAL && orientation != LinearLayoutManager.HORIZONTAL) {
            throw new IllegalArgumentException("请传入正确的参数");
        }
        //转换
        mItemSize = (int) TypedValue.applyDimension(mItemSize, TypedValue.COMPLEX_UNIT_DIP, context.getResources().getDisplayMetrics());
        //创建画笔
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //设置画笔颜色
        mPaint.setColor(color);

        /**
         * 设置画笔模式
         */
        mPaint.setStyle(Paint.Style.FILL);
    }

    //绘制

    @Override
    public void onDraw (Canvas c, RecyclerView parent, RecyclerView.State state) {
        if(mOrientation == LinearLayoutManager.VERTICAL){
            drawVertical(c,parent) ;
        }else {
            drawHorizontal(c,parent) ;
        }
    }

    /**
     * 绘制纵向 item 分割线
     * @param canvas
     * @param parent
     */
    private void drawVertical(Canvas canvas, RecyclerView parent){
        final int left = parent.getPaddingLeft() ;
        final int right = parent.getMeasuredWidth() - parent.getPaddingRight() ;
        final int childSize = parent.getChildCount() ;

        for(int i = 0 ; i < childSize ; i ++){

            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getBottom() + layoutParams.bottomMargin;
            final int bottom = top + mItemSize;
            canvas.drawRect(left, top, right, bottom, mPaint);

        }
    }

    /**
     * 绘制横向 item 分割线
     * @param canvas
     * @param parent
     */
    private void drawHorizontal(Canvas canvas, RecyclerView parent){
        final int top = parent.getPaddingTop() ;
        final int bottom = parent.getMeasuredHeight() - parent.getPaddingBottom() ;
        final int childSize = parent.getChildCount() ;
        for(int i = 0 ; i < childSize ; i ++){
            if(i<childSize-1&&childSize>1) {
                final View child = parent.getChildAt(i);
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int left = child.getRight() + layoutParams.rightMargin;
                final int right = left + mItemSize;
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        }
    }


    //设置大小

    @Override
    public void getItemOffsets (Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if(mOrientation == LinearLayoutManager.VERTICAL){
            outRect.set(0,0,0,mItemSize);
        }else {
            outRect.set(0,0,mItemSize,0);
        }
    }
}