package com.xh.hotme.me.holder;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xh.hotme.R;
import com.xh.hotme.account.LoginManager;
import com.xh.hotme.account.MobileLoginActivity;
import com.xh.hotme.bean.UserInfoBean;
import com.xh.hotme.event.LoginEvent;
import com.xh.hotme.me.MeModuleBean;
import com.xh.hotme.utils.DensityUtil;
import com.xh.hotme.utils.GlideUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class MeProfileHolder extends CommonViewHolder<MeModuleBean> {
    // views
    private final TextView _nameLabel;
    private final ImageView _avatarView;
    Context _ctx;

    // strings
    private final String _loading;

    public static MeProfileHolder create(Context ctx, ViewGroup parent) {
        View convertView = LayoutInflater.from(ctx).inflate(R.layout.me_layout_profile, parent, false);
        return new MeProfileHolder(convertView);
    }

    public MeProfileHolder(View itemView) {
        super(itemView);

        // find views
        _ctx = itemView.getContext();
        _nameLabel = itemView.findViewById(R.id.name);
        _avatarView = itemView.findViewById(R.id.avatar);

        _nameLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!LoginManager.isSignedIn(_ctx)) {
                    MobileLoginActivity.start(_ctx);
                }
            }
        });
        _avatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!LoginManager.isSignedIn(_ctx)) {
                    MobileLoginActivity.start(_ctx);
                }
            }
        });

        // get strings
        _loading = _ctx.getString(R.string.loading);

        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }


    @Override
    public void onDestroy(){
        if(EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReload(LoginEvent event){
        updateProfile();
    }


    @Override
    public void onBind(final MeModuleBean model, int position) {

        // update user profile
        updateProfile();
    }

    private void updateProfile() {
        // get login info
        UserInfoBean loginInfo = LoginManager.getUserLoginInfo(_ctx);
        if (loginInfo != null) {
            // if temp account, use default avatar builtin
            // if not, load avatar
            if (!LoginManager.isSignedIn(_ctx)) {
                // avatar
                GlideUtil.loadCircleWithBorder(_ctx, R.mipmap.default_avatar, _avatarView, DensityUtil.dip2px(_ctx, 2), Color.WHITE);

                // name
                _nameLabel.setText("点击登录");
            } else {
                // avatar
                if (TextUtils.isEmpty(loginInfo.avatar)) {
                    GlideUtil.loadCircleWithBorder(_ctx, R.mipmap.default_avatar, _avatarView, DensityUtil.dip2px(_ctx, 2), Color.WHITE);
                } else {
                    GlideUtil.loadCircleWithBorder(_ctx, loginInfo.avatar, _avatarView, DensityUtil.dip2px(_ctx, 2), Color.WHITE);
                }
                // name
                String mobile = loginInfo.phone;
                if (mobile.length() == 11) {
                    mobile = mobile.substring(0, 3) + "****" + mobile.substring(7);
                } else {
                    mobile = mobile.substring(0, 11);
                }
                _nameLabel.setText(mobile);
            }
        } else {
            // avatar
            GlideUtil.loadCircleWithBorder(_ctx, R.mipmap.default_avatar, _avatarView, DensityUtil.dip2px(_ctx, 2), Color.WHITE);

            // name
            _nameLabel.setText("点击登录");

        }

    }

}
