package com.xh.hotme.live;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.xh.hotme.R;
import com.xh.hotme.bean.VideoTypeBean;
import com.xh.hotme.listener.IVideoModelListener;

import java.util.List;

/**
 * Created by GJK on 2018/11/9.
 */

public class VideoModelAdapter extends BaseQuickAdapter<VideoTypeBean, BaseViewHolder> {
    private final static String TAG = "BluetoothDeviceAdapter";
    IVideoModelListener _listener;

    public VideoModelAdapter(Context context, List data) {

        super(R.layout.dialog_video_type_list_item, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, VideoTypeBean typeBean) {
        TextView tvName = (TextView) baseViewHolder.findView(R.id.tv_name);
        tvName.setText(typeBean.getName());
        int position = baseViewHolder.getPosition();

        baseViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typeBean.setSelect(true);
                _listener.onSelect(position);

            }
        });

        if (typeBean.isSelect()) {
            ((ImageView) baseViewHolder.findView(R.id.iv_icon)).setImageResource(typeBean.iconResSelect);
            tvName.setTextColor(getContext().getColor(R.color.text_black));
        } else {
            ((ImageView) baseViewHolder.findView(R.id.iv_icon)).setImageResource(typeBean.iconResUnselect);
            tvName.setTextColor(getContext().getColor(R.color.text_gray_76));
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
