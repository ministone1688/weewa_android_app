package com.xh.hotme.camera;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.xh.hotme.R;
import com.xh.hotme.bean.CameraVideoDateListBean;
import com.xh.hotme.listener.IVideoModelListener;
import com.xh.hotme.utils.TimeUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 *
 */

public class VideoFilterGroupAdapter extends BaseQuickAdapter<CameraVideoDateListBean, BaseViewHolder> {
    private final static String TAG = "VideoItemAdapter";
    IVideoModelListener _listener;

    boolean enableEdit = false;
    boolean _playMode = false;

    Context _context;

    VideoFilterItemAdapter _adapter;


    public VideoFilterGroupAdapter(Context context, List data, boolean isPlayMode) {
        super(R.layout.icloud_video_filter_layout_list_item, data);
        _playMode = isPlayMode;
        _context = context;
    }

    public VideoFilterGroupAdapter(Context context, List data) {
        super(R.layout.icloud_video_filter_layout_list_item, data);

        _context = context;
        _playMode = false;
    }

    public void setEnableEdit(boolean enableEdit) {
        this.enableEdit = enableEdit;

        if (_adapter != null) {
            _adapter.setEnableEdit(enableEdit);
            _adapter.notifyDataSetChanged();
        }
    }

    public void selectAll() {
//        this.enableEdit = true;

        if (_adapter != null) {
//            _adapter.setEnableEdit(enableEdit);
            _adapter.selectAll();
            _adapter.notifyDataSetChanged();
        }
    }

    public void cleanAllSelect() {
        if (_adapter != null) {
            _adapter.cleanAllSelect();
            _adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, CameraVideoDateListBean videoBean) {
        TextView tvDay = (TextView) baseViewHolder.findView(R.id.tv_day);
        TextView tvDate = (TextView) baseViewHolder.findView(R.id.tv_date);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        Date uploadDate = null;
        try {
            String date = videoBean.date;
            uploadDate = simpleDateFormat.parse(videoBean.date);
            int day = (int) TimeUtil.fromToday(uploadDate);
            if (day == 1) {
                tvDay.setVisibility(View.VISIBLE);
                tvDay.setText(_context.getText(R.string.today));
                tvDate.setText(date);
            } else if (day == 2) {
                tvDay.setVisibility(View.VISIBLE);
                tvDay.setText(_context.getText(R.string.yesterday));
                tvDate.setText(date);
            } else {
                tvDay.setVisibility(View.GONE);
                tvDay.setText("");
                tvDate.setText(date);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        RecyclerView recyclerView = (RecyclerView) baseViewHolder.findView(R.id.recyclerView);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        if (recyclerView.getAdapter() == null) {
            _adapter = new VideoFilterItemAdapter(_context, videoBean.groups, _playMode);
            _adapter.setEnableEdit(enableEdit);
            recyclerView.setAdapter(_adapter);
        } else {
            _adapter.setEnableEdit(enableEdit);
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }


    @Override
    public long getItemId(int position) {
        return position;
    }


    public List<CameraVideoDateListBean.CameraVideoDateBean> getSelectFile(){

        if(_adapter!=null){
          return  _adapter.getSelectFile();
        }

        return null;

    }

}
