package com.xh.hotme.account;


import static com.xh.hotme.utils.Constants.REQUEST_CODE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;

import com.xh.hotme.R;
import com.xh.hotme.base.BaseViewActivity;
import com.xh.hotme.bean.UserInfoBean;
import com.xh.hotme.databinding.ActivityUserNicknameBinding;
import com.xh.hotme.event.UpdateNameEvent;
import com.xh.hotme.listener.ICommonListener;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.ColorUtil;
import com.xh.hotme.utils.StatusBarUtil;
import com.xh.hotme.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;


public class SetNicknameActivity extends BaseViewActivity<ActivityUserNicknameBinding> {
    private static final String TAG = SetNicknameActivity.class.getSimpleName();

    UserInfoBean _userInfoBean;

    private int reqestCode = -1;

    public static void start(Context context) {
        if (null != context) {
            Intent intent = new Intent(context, SetNicknameActivity.class);
            context.startActivity(intent);
        }
    }

    public static void startActivityByRequestCode(Activity context, int requestCode) {
        if (null != context) {
            Intent intent = new Intent(context, SetNicknameActivity.class);
            intent.putExtra(REQUEST_CODE, requestCode);

            context.startActivityForResult(intent, requestCode);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (getIntent() != null) {
            reqestCode = getIntent().getIntExtra(REQUEST_CODE, -1);
        }
        super.onCreate(savedInstanceState);

        // set status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StatusBarUtil.setStatusBarColor(this, ColorUtil.parseColor("#ffffff"));
        }

    }

    @Override
    protected void initView() {
        // find views
        viewBinding.titleBar.tvTitle.setText(getString(R.string.me_setting_userinfo));
        viewBinding.titleBar.ivBack.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                finish();
                return true;
            }
        });
        viewBinding.titleBar.tvRight.setText(getText(R.string.save));
        viewBinding.titleBar.tvRight.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                String nick = viewBinding.etNickname.getText().toString();
                if (TextUtils.isEmpty(nick)) {
                    ToastUtil.s(SetNicknameActivity.this, getString(R.string.info_nickname_is_null));
                    return true;
                }
                saveNickName(nick);
                return true;
            }
        });
        viewBinding.titleBar.ivRight.setVisibility(View.GONE);

        viewBinding.ivClear.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                viewBinding.etNickname.getText().clear();
                return true;
            }
        });

    }

    @Override
    protected void initData() {
        _userInfoBean = LoginManager.getUserLoginInfo(SetNicknameActivity.this);
        if (_userInfoBean != null) {
            viewBinding.etNickname.setText(_userInfoBean.nickName);
        }
    }

    public void doFinish(boolean success) {
        if (reqestCode > -1) {
            Intent i = new Intent();
            i.putExtra("result", 0);
            setResult(success ? 1 : 0, i);
            finish();
        } else {
            finish();
        }
    }

    private void saveNickName(String nickname) {
        NicknameInteract.updateNickname(SetNicknameActivity.this, nickname, new ICommonListener() {
            @Override
            public void onSuccess() {
                if (_userInfoBean != null) {
                    _userInfoBean.nickName = nickname;
                    LoginManager.saveLoginInfo(SetNicknameActivity.this, _userInfoBean);
                }
                EventBus.getDefault().post(new UpdateNameEvent(nickname));
                ToastUtil.s(SetNicknameActivity.this, getString(R.string.info_nickname_update_suc));
                finish();
            }

            @Override
            public void onFail(String code, String message) {

            }

            @Override
            public void onFinish() {

            }
        });
    }
}
