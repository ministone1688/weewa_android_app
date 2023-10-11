package com.xh.hotme.device;

import android.content.Context;
import android.widget.LinearLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.xh.hotme.R;
import com.xh.hotme.bean.VideoBean;
import com.xh.hotme.utils.BaseAppUtil;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.DensityUtil;
import com.xh.hotme.utils.GlideUtil;
import com.xh.hotme.utils.ToastUtil;


import java.util.List;

public class HomeVideoAdapter extends BaseQuickAdapter<VideoBean, BaseViewHolder> {

    public HomeVideoAdapter(Context context, List data) {

        super(R.layout.home_fragment_layout_video_list_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, VideoBean item) {

        int left_width = (BaseAppUtil.getDeviceWidth(getContext()) - 2 * DensityUtil.dip2px(getContext(), 10)) / 2;
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) helper.getView(R.id.video_view).getLayoutParams();
        params.height = left_width;
        params.width = left_width;
        helper.getView(R.id.video_view).setLayoutParams(params);


        GlideUtil.loadRoundedCorner(getContext(), item.thumbUrl, helper.getView(R.id.iv_thumb), DensityUtil.dip2px(getContext(), 10));

        helper.getView(R.id.video_view).setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                ToastUtil.s(getContext(), "暂无视频");
                return true;
            }
        });

    }
}
