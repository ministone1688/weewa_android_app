package com.xh.hotme.setting;


import android.content.Context;
import android.content.Intent;

import com.xh.hotme.base.BaseViewActivity;
import com.xh.hotme.base.TitleLayoutHelper;
import com.xh.hotme.databinding.ActivityContactUsBinding;
import com.xh.hotme.utils.ClickGuard;

public class ContactUsActivity extends BaseViewActivity<ActivityContactUsBinding> {


    public static void start(Context context) {
        Intent intent = new Intent(context, ContactUsActivity.class);
        context.startActivity(intent);

    }

    @Override
    protected void initView() {
        TitleLayoutHelper.initTitle(viewBinding.title, "联系我们", new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                finish();
                return true;
            }
        });

    }

    @Override
    protected void initData() {

    }
}