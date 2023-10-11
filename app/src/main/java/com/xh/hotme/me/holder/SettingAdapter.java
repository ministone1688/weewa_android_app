package com.xh.hotme.me.holder;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xh.hotme.me.bean.SettingBean;

import java.util.List;

/**
 * Create by zhaozhihui on 2019-09-10
 **/
public class SettingAdapter extends RecyclerView.Adapter<SettingHolder> {

    Context _context;

    List<SettingBean> _list;

    public final static  int SETTING_TYPE_USERINFO = 0;
    public final static int SETTING_TYPE_CONTACT = 1;
    public final static int SETTING_TYPE_ACCOUNT = 2;
    public final static int SETTING_TYPE_EXIT = 3;

    public final static int SETTING_TYPE_POWER = 4;

    public final static int SETTING_TYPE_DEVICES = 5;
    public final static int SETTING_TYPE_KUAICHUAN = 6;


    public final static int SETTING_TYPE_CAMERA_RUNNING_INFO = 10;
    public final static int SETTING_TYPE_CAMERA_SOFT_AP_SETTING = 11;
    public final static int SETTING_TYPE_CAMERA_ABOUT = 12;

    public final static int SETTING_TYPE_CAMERA_SETTING = 13;
    public final static int SETTING_TYPE_CAMERA_NET = 14;
    public final static int SETTING_TYPE_CAMERA_LIVE = 15;

    public SettingAdapter(Context context, List<SettingBean> dataList) {
        _context = context;
        _list = dataList;
    }

    @NonNull
    @Override
    public SettingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return SettingHolder.create(_context, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingHolder holder, int position) {
        holder.onBind(_list.get(position), position);
    }

    @Override
    public int getItemCount() {
        return _list == null ? 0 : _list.size();
    }
}
