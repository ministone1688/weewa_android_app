package com.xh.hotme.icloud;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.xh.hotme.R;
import com.xh.hotme.bean.IcloudVideoBean;
import com.xh.hotme.listener.IVideoModelListener;
import com.xh.hotme.utils.DensityUtil;
import com.xh.hotme.utils.GlideUtil;
import com.xh.hotme.widget.roundedimageview.RoundedImageView;

import java.util.List;

/**
 *
 */

public class VideoItemAdapter extends BaseQuickAdapter<IcloudVideoBean, BaseViewHolder> {
    private final static String TAG = "VideoItemAdapter";
    IVideoModelListener _listener;

    boolean enableEdit = false;

    Context _context;

    public VideoItemAdapter(Context context, List data) {
        super(R.layout.icloud_video_list_item_video, data);

        _context = context;
    }

    private void setEnableEdit(boolean enableEdit) {
        this.enableEdit = enableEdit;
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, IcloudVideoBean videoBean) {
        TextView tvDay = (TextView) baseViewHolder.findView(R.id.tv_day);
        TextView tvDate = (TextView) baseViewHolder.findView(R.id.tv_date);
        TextView tvName = (TextView) baseViewHolder.findView(R.id.name);
        TextView tvDuration = (TextView) baseViewHolder.findView(R.id.duration);

        int cornerDp = (int) _context.getResources().getDimension(R.dimen.video_corner_dp);

        RoundedImageView ivThumb = baseViewHolder.findView(R.id.thumb);
        ivThumb.setCornerRadius(cornerDp);
        CheckBox checkBox = baseViewHolder.findView(R.id.edit);
        checkBox.setVisibility(enableEdit ? View.VISIBLE : View.GONE);

        tvName.setText(videoBean.videoName);
        tvDuration.setText(String.valueOf(videoBean.duration));

        if (TextUtils.isEmpty(videoBean.path)) {
            GlideUtil.loadRoundedCorner(_context, _context.getResources().getIdentifier("camera_cover_default", "mipmap",
                    _context.getPackageName()), ivThumb, DensityUtil.px2dip(_context, cornerDp));

        } else {
            GlideUtil.loadRoundedCorner(_context, videoBean.path, ivThumb, DensityUtil.px2dip(_context, cornerDp));
            ivThumb.setCornerRadius(DensityUtil.px2dip(_context, cornerDp));
        }

    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setLiseter(IVideoModelListener l) {
        _listener = l;
    }
}
