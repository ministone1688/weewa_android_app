package com.xh.hotme.me;

import android.app.Activity;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.xh.hotme.me.holder.CommonViewHolder;
import com.xh.hotme.me.holder.MeLoginOutHolder;
import com.xh.hotme.me.holder.MeProfileHolder;
import com.xh.hotme.me.holder.MeSettingHolder;
import com.xh.hotme.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class MeHomeAdapter extends RecyclerView.Adapter<CommonViewHolder> {
    private final List<MeModuleBean> _models;
    private List<Integer> _itemTypes;
    private final Activity mContext;


    public MeHomeAdapter(Activity ctx) {
        mContext = ctx;
        _models = new ArrayList<>();

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return _models == null ? 0 : _models.size();
    }

    @Override
    public int getItemViewType(int position) {
        return _models.get(position).getType();
    }

    @Override
    public CommonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case Constants.ME_MODULE_PROFILE:
                return MeProfileHolder.create(mContext, parent);
            case Constants.ME_MODULE_SETTING:
                return MeSettingHolder.create(mContext, parent);
            case Constants.ME_MODULE_OUT:
                return MeLoginOutHolder.create(mContext, parent);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull CommonViewHolder holder, int pos) {
        CommonViewHolder vh = holder;

        vh.onBind(_models.get(pos), pos);
    }

    public List<MeModuleBean> getModels() {
        return _models;
    }

    public void setModels(List<MeModuleBean> moduleList) {

        if (moduleList == null || moduleList.size() == 0) {
            return;
        }

        _models.clear();

        _models.addAll(moduleList);

    }
}
