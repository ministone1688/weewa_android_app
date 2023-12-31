package com.xh.hotme.setting;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xh.hotme.R;
import com.xh.hotme.base.BaseActivity;
import com.xh.hotme.utils.ColorUtil;
import com.xh.hotme.utils.StatusBarUtil;
import com.xh.hotme.utils.ToastUtil;


/**
 * Create by zhaozhihui on 2019-09-14
 **/
public class FeedBackActivity extends BaseActivity {

    private static final String TAG = FeedBackActivity.class.getSimpleName();

    ImageView backView;

    EditText contentText;

    TextView titleText;

    LinearLayout submitView;

    TextView submitTextView;

    TextView maxNumberTextView;
    TextView contentNumberTextView;

    Button feedbackBtn;

    final int maxNumber = 200;

    public static void start(Context context) {
        Intent intent = new Intent(context, FeedBackActivity.class);
        context.startActivity(intent);

    }

    class MaxTextLengthFilter implements InputFilter {

        private final int mMaxLength;

        public MaxTextLengthFilter(int max) {
            mMaxLength = max ;
        }

        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            int keep = mMaxLength - (dest.length() - (dend - dstart));
            if (keep < (end - start)) {
                ToastUtil.s(getApplicationContext(), "字数已达上限");
            }

            if (keep <= 0) {
                return "";
            } else if (keep >= end - start) {
                return null;
            } else {
                return source.subSequence(start, start + keep);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StatusBarUtil.setStatusBarColor(this, ColorUtil.parseColor("#ffffff"));
        }

        View decor = getWindow().getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        // set content view
        setContentView(R.layout.activity_feed_back);

        backView = findViewById(R.id.iv_back);
        contentText = findViewById(R.id.et_feedback_content);
        maxNumberTextView = findViewById(R.id.tv_max_number);
        contentNumberTextView = findViewById(R.id.tv_content_number);

        submitTextView = findViewById(R.id.tv_right);

        titleText = findViewById(R.id.tv_title);
        feedbackBtn = findViewById(R.id.btn_feedback);

        titleText.setText("问题反馈");

        contentText.setFilters(new InputFilter[]{new MaxTextLengthFilter(maxNumber)});

        maxNumberTextView.setText("" + maxNumber);
        contentNumberTextView.setText("0");

        //所有继承自TextView的类
        contentText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged: s = " + s + ", start = " + start +
                        ", before = " + before + ", count = " + count);

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                String content = s.toString();
                try {
                    if (contentNumberTextView != null) {
                        contentNumberTextView.setText(""+content.length());
                    }
                }catch (Throwable e){
                    e.printStackTrace();
                }

//                if(s!=null) {
//                    int len = s.length();
//
//                    contentNumberTextView.setText(len);
//                }
            }
        });

        backView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });


        feedbackBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String content = contentText.getText().toString();
                if (TextUtils.isEmpty(content)) {
                    ToastUtil.s(FeedBackActivity.this, "请输入反馈内容");
                }
//
//                LeBoxUtil.feedBack(FeedBackActivity.this, content, new HttpCallbackDecode(FeedBackActivity.this, null) {
//                    @Override
//                    public void onDataSuccess(Object data) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                ToastUtil.s(FeedBackActivity.this, "感谢参与～");
//
//                                finish();
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onFailure(String code, String message) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                ToastUtil.s(FeedBackActivity.this, "发送失败，请稍后再试～");
//                            }
//                        });
//                    }
//                });

            }
        });
    }
}