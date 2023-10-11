package com.xh.hotme.camera;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.xh.hotme.R;
import com.xh.hotme.bean.CameraVideoDetailListBean;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.utils.GlideUtil;
import com.xh.hotme.utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GJK on 2018/11/9.
 */

public class CameraVideoDetailAdapter extends BaseQuickAdapter<CameraVideoDetailListBean.VideosBean, BaseViewHolder> {
    private final static String TAG = "CameraVideoDetailAdapter";
    private ArrayList<CameraVideoDetailListBean.VideosBean> mDevices;

    Context _context;

    public CameraVideoDetailAdapter(Context context, List data) {
        super(R.layout.video_detail_item_content, data);
        _context = context;
    }


    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, CameraVideoDetailListBean.VideosBean videosBean) {

        ((TextView) baseViewHolder.findView(R.id.tvTitle)).setText(videosBean.name);
       ImageView thumbView =  baseViewHolder.findView(R.id.ivLogo);

        TextView timeView = (TextView)baseViewHolder.findView(R.id.tvTime);
        if (videosBean.duration>0){
            timeView.setText(TimeUtil.intToTimeMin(videosBean.duration)+"");
        } else {
            timeView.setText("0");
        }

        String imageUrl = BluetoothManager.getRealFileImageUrl(videosBean.thumb);
        GlideUtil.loadRoundedCorner(timeView.getContext(), imageUrl, thumbView, 12);
    }

    public ArrayList<CameraVideoDetailListBean.VideosBean> getVideoList() {
        return mDevices;
    }

    public void setVideoList(ArrayList<CameraVideoDetailListBean.VideosBean> devices) {
        this.mDevices = devices;
    }


    @Override
    public long getItemId(int position) {
        return position;
    }
}
