package com.xh.hotme.me.holder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xh.hotme.R;
import com.xh.hotme.me.MeModuleBean;
import com.xh.hotme.me.bean.SettingBean;

import java.util.ArrayList;
import java.util.List;


public class MeSettingHolder extends CommonViewHolder<MeModuleBean> {
    View _splitSpace;
    RecyclerView _recyclerView;
    SettingAdapter _taskAdapter;

    Context _context;
    List<SettingBean> _taskList = new ArrayList<>();


    public static MeSettingHolder create(Context ctx, ViewGroup parent) {
        // load game row, and leave a gap so that next column can be seen
        View view = LayoutInflater.from(ctx)
                .inflate(R.layout.me_layout_setting, parent, false);
        return new MeSettingHolder(ctx, view);
    }

    public MeSettingHolder(Context context, View view) {
        super(view);

        _context = context;
        this._recyclerView = view.findViewById(R.id.recyclerView);
        this._splitSpace = view.findViewById(R.id.split_space);

        _taskAdapter = new SettingAdapter(_context, _taskList);

        // setup views
        _recyclerView.setLayoutManager(new LinearLayoutManager(context));
        _recyclerView.setAdapter(_taskAdapter);

        _recyclerView.setNestedScrollingEnabled(false);

    }

    @Override
    public void onBind(final MeModuleBean signin, final int position) {
        // name & desc
        final Context ctx = itemView.getContext();

        _splitSpace.setVisibility(position == 0 ? View.GONE : View.VISIBLE);

        initData();
        _taskAdapter.notifyDataSetChanged();
    }


    private void initData() {
        if (_taskList == null) {
            _taskList = new ArrayList<>();
        } else {
            _taskList.clear();
        }

        _taskList.add(new SettingBean(SettingAdapter.SETTING_TYPE_USERINFO, R.mipmap.me_info, _context.getString(R.string.ly_me_info)));
        _taskList.add(new SettingBean(SettingAdapter.SETTING_TYPE_CAMERA_SETTING, R.mipmap.me_photo, _context.getString(R.string.ly_me_photo_set)));
        _taskList.add(new SettingBean(SettingAdapter.SETTING_TYPE_CAMERA_NET, R.mipmap.me_net, _context.getString(R.string.ly_me_photo_net)));
        _taskList.add(new SettingBean(SettingAdapter.SETTING_TYPE_CAMERA_LIVE, R.mipmap.me_live, _context.getString(R.string.ly_me_live)));
        _taskList.add(new SettingBean(SettingAdapter.SETTING_TYPE_ACCOUNT, R.mipmap.me_set, _context.getString(R.string.ly_me_setting)));

    }

}