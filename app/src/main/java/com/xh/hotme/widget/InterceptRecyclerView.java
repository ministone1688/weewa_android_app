package com.xh.hotme.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Create by zhaozhihui on 2021/6/24
 **/
public class InterceptRecyclerView extends RecyclerView {

    private int mDownY;
    private int mTouchSlop;

    onTouchListener mTouchListener;

    public InterceptRecyclerView(Context context) {
        super(context);
    }
    public InterceptRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownY = (int) ev.getRawY();
                if (mTouchListener != null) {
                    mTouchListener.onTouch();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int moveY = (int) ev.getRawY();
                if (Math.abs(mDownY - moveY) > mTouchSlop) {
                    //拦截事件，自己消费
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void setTouchListener(onTouchListener l) {
        mTouchListener = l;
    }

    public interface onTouchListener {
        void onTouch();
    }
}
