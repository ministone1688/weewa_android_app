package com.xh.hotme.me.holder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextClock;
import android.widget.TextView;

import com.xh.hotme.MainActivity;
import com.xh.hotme.R;
import com.xh.hotme.account.LoginManager;
import com.xh.hotme.account.MobileLoginActivity;
import com.xh.hotme.lay.utils.MyToolUtils;
import com.xh.hotme.me.MeModuleBean;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.widget.ModalDialog;

public class MeLoginOutHolder extends CommonViewHolder<MeModuleBean> {

    Context _ctx;
    private Button _sign_out;
    private TextView _version_num;

    public static MeLoginOutHolder create(Context ctx, ViewGroup parent) {
        View convertView = LayoutInflater.from(ctx).inflate(R.layout.me_layout_login_out, parent, false);
        return new MeLoginOutHolder(convertView);
    }

    public MeLoginOutHolder(View itemView) {
        super(itemView);
        _ctx = itemView.getContext();

        _version_num = itemView.findViewById(R.id.version_num);
        String ver = MyToolUtils.getAppVersionName(_ctx);
        _version_num.setText("当前版本：" + ver);

        _sign_out = itemView.findViewById(R.id.sign_out);
        if (!LoginManager.isSignedIn(_ctx)) {
            _sign_out.setVisibility(View.GONE);
        }else{
            _sign_out.setVisibility(View.VISIBLE);
        }
        _sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ModalDialog d = new ModalDialog(_ctx,
                        "",
                        _ctx.getString(R.string.out_sys_mg),
                        true
                );
                d.setLeftButton("取消", new ClickGuard.GuardedOnClickListener() {
                    @Override
                    public boolean onClicked() {
                        d.dismiss();
                        return true;
                    }
                });
                d.setRightButton("退出", new ClickGuard.GuardedOnClickListener() {
                    @Override
                    public boolean onClicked() {
                        LoginManager.clearLoginInfo(_ctx);
                        MobileLoginActivity.start(_ctx);
                        return true;
                    }
                });
                d.show();

            }
        });


    }

    @Override
    public void onBind(MeModuleBean model, int position) {

    }
}
