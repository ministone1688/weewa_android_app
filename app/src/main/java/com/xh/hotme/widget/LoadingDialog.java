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

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Keep;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.xh.hotme.R;
import com.xh.hotme.utils.AppTrace;

@Keep
public class LoadingDialog extends AlertDialog {

    private static final String TAG = "LoadingDialog";

    private TextView mTextView;

    //handler 用来更新UI
    private final Handler handler = new Handler();

    public LoadingDialog(Context context) {
        super(context, R.style.customDialog);
        setCancelable(true);
        setCanceledOnTouchOutside(false);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hotme_dialog_loading);
        mTextView = findViewById(R.id.leto_loading_msg);
    }

    public void show(String message) {
        show();

        if (TextUtils.isEmpty(message)) {
            mTextView.setText("");
            mTextView.setVisibility(View.GONE);
        } else {
            mTextView.setText(message);
            mTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void show() {
        try {
            super.show();
        } catch (Exception e) {
            AppTrace.e(TAG, e.getMessage());
        }
    }

    @Override
    public boolean isShowing() {
        try {
            return super.isShowing();
        } catch (Exception e) {
            AppTrace.e(TAG, e.getMessage());
        }

        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
//        if (handler != null) {
//            //这里用到了handler的定时器效果 延迟10秒执行dismiss();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        if (isShowing()) {
//                            dismiss();
//                        }
//                    } catch (Throwable e) {
//
//                    }
//
//                }
//            }, 10000);
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    public void destroy(){
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

}
