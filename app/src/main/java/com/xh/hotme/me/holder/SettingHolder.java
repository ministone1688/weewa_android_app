package com.xh.hotme.me.holder;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.xh.hotme.R;
import com.xh.hotme.account.LoginManager;
import com.xh.hotme.account.MobileLoginActivity;
import com.xh.hotme.active.MyDeviceActivity;
import com.xh.hotme.active.PowerDeviceListActivity;
import com.xh.hotme.camera.CameraInfoActivity;
import com.xh.hotme.camera.CameraRunningInfoActivity;
import com.xh.hotme.lay.PhotoSetActivity;
import com.xh.hotme.lay.PushLiveActivity;
import com.xh.hotme.me.bean.SettingBean;
import com.xh.hotme.setting.ProfileActivity;
import com.xh.hotme.setting.SettingActivity;
import com.xh.hotme.softap.CameraNetworkSettingActivity;
import com.xh.hotme.softap.CameraSoftApActivity;
import com.xh.hotme.softap.SoftApDeviceListActivity;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.ToastUtil;


import java.text.DecimalFormat;

public class SettingHolder extends CommonViewHolder<SettingBean> {

    // views
    private final TextView _nameLabel;
    private final ImageView _iconIv;
    private final View _itemView;

    Context _ctx;


    SettingBean _settingMode;


    public static SettingHolder create(Context ctx, ViewGroup parent) {
        View convertView = LayoutInflater.from(ctx).inflate(R.layout.me_layout_setting_list_item, parent, false);
        return new SettingHolder(ctx, convertView);
    }

    public SettingHolder(Context context, View itemView) {
        super(itemView);

        // find views
        _ctx = context;
        _iconIv = itemView.findViewById(R.id.iv_icon);
        _nameLabel = itemView.findViewById(R.id.tv_name);
        _itemView = itemView.findViewById(R.id.setting_item);
    }

    @Override
    public void onBind(final SettingBean model, int position) {
        _settingMode = model;
        if (model.getIcon() != 0) {
            _iconIv.setVisibility(View.VISIBLE);
            _iconIv.setImageResource(model.getIcon());
        } else {
            _iconIv.setVisibility(View.GONE);
        }
        _nameLabel.setText(model.getName());

        _itemView.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                switch (model.getType()) {
                    case SettingAdapter.SETTING_TYPE_USERINFO:
                        if (LoginManager.isSignedIn(_ctx)) {
                            ProfileActivity.start(_ctx);
                        } else {
                            MobileLoginActivity.start(_ctx);
                        }
                        break;
                    case SettingAdapter.SETTING_TYPE_ACCOUNT:
                        SettingActivity.start(_ctx);
                        break;

                    case SettingAdapter.SETTING_TYPE_DEVICES:
                        MyDeviceActivity.start(_ctx);
                        break;
                    case SettingAdapter.SETTING_TYPE_KUAICHUAN:
                        SoftApDeviceListActivity.start(_ctx);
                        break;

                    case SettingAdapter.SETTING_TYPE_CAMERA_SOFT_AP_SETTING:
                        CameraSoftApActivity.start(_ctx, CameraSoftApActivity.REQUEST_SOFT_AP_CAMERA_INFO);
                        break;
                    case SettingAdapter.SETTING_TYPE_CAMERA_RUNNING_INFO:
                        CameraRunningInfoActivity.start(_ctx);
                        break;
                    case SettingAdapter.SETTING_TYPE_CAMERA_ABOUT:
                        ToastUtil.s(_ctx, "关于我们");
                        break;

                        //相机功能
                    case SettingAdapter.SETTING_TYPE_CAMERA_SETTING:
                        if (LoginManager.isSignedIn(_ctx)) {
                            _ctx.startActivity(new Intent(_ctx, PhotoSetActivity.class));
                        } else {
                            MobileLoginActivity.start(_ctx);
                        }
                        break;
                    //相机网络
                    case SettingAdapter.SETTING_TYPE_CAMERA_NET:
                        if (LoginManager.isSignedIn(_ctx)) {
                            _ctx.startActivity(new Intent(_ctx, CameraNetworkSettingActivity.class));
                        } else {
                            MobileLoginActivity.start(_ctx);
                        }
                        break;

                    //直播管理
                    case SettingAdapter.SETTING_TYPE_CAMERA_LIVE:
                        if (LoginManager.isSignedIn(_ctx)) {
                            _ctx.startActivity(new Intent(_ctx, PushLiveActivity.class));
                        } else {
                            MobileLoginActivity.start(_ctx);
                        }
                        break;
                }


                return true;
            }
        });

    }

}
