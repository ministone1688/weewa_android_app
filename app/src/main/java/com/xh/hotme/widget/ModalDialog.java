//
// Copyright (c) 2017, ledong.com
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// * Redistributions of source code must retain the above copyright notice, this
// list of conditions and the following disclaimer.
//
// * Redistributions in binary form must reproduce the above copyright notice,
// this list of conditions and the following disclaimer in the documentation
// and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//


package com.xh.hotme.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;


import com.xh.hotme.R;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.ColorUtil;
import com.xh.hotme.utils.DensityUtil;
import com.xh.hotme.utils.DeviceInfo;

/**
 * 自定义白色对话框，由api调用的模态对话框
 */
@Keep
public class ModalDialog extends Dialog {

    private static final String TAG = "ModalDialog";

    private final View mTitleView;
    private final View mButtonView;
    private final TextView mTitle;
    private final TextView mMessage;
    private final TextView mLeftBtn;
    private final TextView mRightBtn;
    private final ImageView mBtnDivideLine;

    private View.OnClickListener mLeftBtnClickListener;
    private View.OnClickListener mRightBtnClickListener;


    String mLeftBtnText, mRightBtnText;

    public ModalDialog(Context context) {
        this(context, "", "",  true);
    }


    public ModalDialog(Context context, boolean cancelable) {
        this(context, "", "",  cancelable);

    }

    public ModalDialog(@NonNull Context context, String title, String msg, boolean cancelable) {
        super(context, R.style.hotme_modal_dialog);
        setCancelable(cancelable);

        View contentView = View.inflate(context, R.layout.hotme_dialog_modal, null);
        mTitleView = contentView.findViewById(R.id.dlg_title_view);
        mButtonView = contentView.findViewById(R.id.dlg_btn_view);
        mTitle = contentView.findViewById(R.id.dlg_title);
        mMessage = contentView.findViewById(R.id.leto_dlg_msg);
        mBtnDivideLine = contentView.findViewById(R.id.leto_line_v);
        mLeftBtn = contentView.findViewById(R.id.dlg_left_btn);
        mRightBtn = contentView.findViewById(R.id.dlg_right_btn);
        mLeftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLeftBtnClickListener != null) {
                    mLeftBtnClickListener.onClick(v);
                }
                dismiss();
            }
        });
        mRightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRightBtnClickListener != null) {
                    mRightBtnClickListener.onClick(v);
                }
                dismiss();
            }
        });
        setContentView(contentView);

        mTitle.setText(title);
        mMessage.setText(msg);

//        Window window = getWindow();
//        window.setGravity(Gravity.CENTER);
//        WindowManager.LayoutParams windowparams = window.getAttributes();
//        windowparams.width = DeviceInfo.getWidth(context);
//        windowparams.height = DeviceInfo.getHeight(context);


    }

    /**
     * 设置标题栏图标
     *
     * @param drawableId
     */
    public void setTitleIcon(int drawableId) {
        Drawable drawable = getContext().getResources().getDrawable(drawableId);
        setTitleIcon(drawable);
    }

    /**
     * 设置标题栏图标
     *
     * @param drawable
     */
    public void setTitleIcon(Drawable drawable) {
        if (drawable == null) {
            return;
        }
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        mTitle.setCompoundDrawables(drawable, null, null, null);
        mTitleView.setVisibility(View.VISIBLE);
    }

    /**
     * 设置对话框标题
     *
     * @param strId
     */
    public void setTitle(int strId) {
        setTitle(getContext().getString(strId));
    }

    /**
     * 设置对话框标题
     *
     * @param title
     */
    public void setTitle(String title) {
        if (TextUtils.isEmpty(title)) {
            mTitleView.setVisibility(View.GONE);
            return;
        }
        mTitle.setText(title);
        mTitleView.setVisibility(View.VISIBLE);
    }

    /**
     * 设置对话框内容
     *
     * @param strId
     */
    public void setMessage(int strId) {
        setMessage(getContext().getString(strId));
    }

    /**
     * 设置对话框内容
     *
     * @param msg
     */
    public void setMessage(String msg) {
        mMessage.setText(msg);
    }

    /**
     * 设置对话框内容文字颜色
     *
     * @param color 颜色值
     */
    public void setMessageTextColor(String color) {
        try {
            mMessage.setTextColor(ColorUtil.parseColor(color));
        } catch (Exception e) {
            AppTrace.e(TAG, String.format("setMessageTextColor(%s) parse color error", color));
        }
    }

    /**
     * 设置对话框内容文字大小
     *
     * @param size 颜色值
     */
    public void setMessageTextSize(int unit, float size) {
        try {
            mMessage.setTextSize(unit, size);
        } catch (Exception e) {
            AppTrace.e(TAG, String.format("setMessageTextSize(%s) parse size ", size));
        }
    }

    /**
     * 设置左侧按钮文字大小
     *
     * @param size 大小
     */
    public void setLeftButtonTextSize(int unit, float size) {
        try {
            mLeftBtn.setTextSize(unit, size);
        } catch (Exception e) {
            AppTrace.e(TAG, String.format("setLeftButtonTextSize(%s) parse size ", size));
        }
    }

    /**
     * 设置右侧按钮文字大小
     *
     * @param size 大小
     */
    public void setRightButtonTextSize(int unit, float size) {
        try {
            mRightBtn.setTextSize(unit, size);
        } catch (Exception e) {
            AppTrace.e(TAG, String.format("setRightButtonTextSize(%s) parse size ", size));
        }
    }


    /**
     * 设置左侧按钮文字颜色
     *
     * @param color 颜色值
     */
    public void setLeftButtonTextColor(String color) {
        try {
            mLeftBtn.setTextColor(ColorUtil.parseColor(color));
        } catch (Exception e) {
            AppTrace.e(TAG, String.format("setLeftButtonTextColor(%s) parse color error", color));
        }
    }

    /**
     * 设置右侧按钮文字颜色
     *
     * @param color 颜色值
     */
    public void setRightButtonTextColor(String color) {
        try {
            mRightBtn.setTextColor(ColorUtil.parseColor(color));
        } catch (Exception e) {
            AppTrace.e(TAG, String.format("setRightButtonTextColor(%s) parse color error", color));
        }
    }

    /**
     * 设置左边按钮文字和点击监听
     *
     * @param strId
     * @param listener
     */
    public void setLeftButton(int strId, View.OnClickListener listener) {
        setLeftButton(getContext().getString(strId), listener);
    }

    /**
     * 设置左边按钮文字和点击监听
     *
     * @param text
     * @param listener
     */
    public void setLeftButton(String text, View.OnClickListener listener) {
        mButtonView.setVisibility(View.VISIBLE);
        if (mRightBtn.getVisibility() == View.VISIBLE) {
            mBtnDivideLine.setVisibility(View.VISIBLE);
        } else {
            mBtnDivideLine.setVisibility(View.GONE);
        }
        mLeftBtnText = text;
        mLeftBtn.setText(text);
        mLeftBtn.setVisibility(View.VISIBLE);
        mLeftBtnClickListener = listener;
    }

    /**
     * 设置右边按钮文字和点击监听
     *
     * @param strId
     * @param listener
     */
    public void setRightButton(int strId, View.OnClickListener listener) {
        setRightButton(getContext().getString(strId), listener);
    }

    /**
     * 设置右边按钮文字和监听
     *
     * @param text
     * @param listener
     */
    public void setRightButton(String text, View.OnClickListener listener) {
        mButtonView.setVisibility(View.VISIBLE);
        if (mLeftBtn.getVisibility() == View.VISIBLE) {
            mBtnDivideLine.setVisibility(View.VISIBLE);
        } else {
            mBtnDivideLine.setVisibility(View.GONE);
        }
        mRightBtnText = text;
        mRightBtn.setText(text);
        mRightBtn.setVisibility(View.VISIBLE);
        mRightBtnClickListener = listener;
    }

    @Override
    public void show() {
        try {
            if (isCountDown && mHandler != null) {
                mHandler.sendEmptyMessageDelayed(0, 1000);
            }

            super.show();
        } catch (Exception e) {
            AppTrace.e(TAG, "show dialog exception");
        }
    }

    @Override
    public void dismiss() {
        try {
            isCountDown = false;
            if (mHandler != null) {
                mHandler.removeMessages(0);
            }
            super.dismiss();
        } catch (Exception e) {
            AppTrace.e(TAG, "dismiss dialog exception");
        }
    }

    boolean isCountDown = false;
    int mCountDown = 5;
    int mCurrentCount;

    Handler mHandler;

    public void setCountDown(int duration, final int buttonIndex) {
        if (duration > 0) {
            isCountDown = true;
            mHandler = new Handler() {
                public void handleMessage(android.os.Message msg) {
                    try {
                        if (isCountDown) {
                            if (buttonIndex == 0 && mLeftBtn.getVisibility() == View.VISIBLE) {
                                mLeftBtn.setText(mLeftBtnText + " (" + mCurrentCount + "秒)");
                            } else if (buttonIndex == 1 && mRightBtn.getVisibility() == View.VISIBLE) {
                                mRightBtn.setText(mRightBtnText + " (" + mCurrentCount + "秒)");
                            }
                            if (mCurrentCount > 0) {
                                mHandler.sendEmptyMessageDelayed(0, 1000);
                                mCurrentCount--;
                            } else {
                                dismiss();
                            }
                        }
                    }catch (Throwable e){

                    }
                }
            };

        } else {
            isCountDown = false;
        }
        mCountDown = duration;
        mCurrentCount = duration;
    }

}
