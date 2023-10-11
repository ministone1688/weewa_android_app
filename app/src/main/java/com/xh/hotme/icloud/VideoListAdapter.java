package com.xh.hotme.icloud;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.xh.hotme.R;
import com.xh.hotme.icloud.item.DateItem;
import com.xh.hotme.icloud.item.ListItem;
import com.xh.hotme.icloud.item.VideoListItem;
import com.xh.hotme.listener.IVideoModelListener;
import com.xh.hotme.utils.TimeUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by GJK on 2018/11/9.
 */

public class VideoListAdapter extends BaseMultiItemQuickAdapter<ListItem, BaseViewHolder> {
    private final static String TAG = "VideoListAdapter";
    IVideoModelListener _listener;

    Context _context;

    public VideoListAdapter(Context context, List data) {
        super(data);
        addItemType(ListItem.TYPE_DATE, R.layout.icloud_video_list_item_head);
        addItemType(ListItem.TYPE_VIDEO_LIST, R.layout.icloud_video_layout_list_content);
        _context = context;
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, ListItem dateItem) {

        switch (baseViewHolder.getItemViewType()) {
            case ListItem.TYPE_DATE:
                setHeaderView(baseViewHolder, (DateItem) dateItem);
                break;
            case ListItem.TYPE_VIDEO_LIST:

                setContentView(baseViewHolder, (VideoListItem) dateItem);
                break;
        }
    }


    public void setHeaderView(BaseViewHolder helper, DateItem homeDataBean) {
        TextView tvDay = (TextView) helper.findView(R.id.tv_day);
        TextView tvDate = (TextView) helper.findView(R.id.tv_date);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date uploadDate = null;
        try {
            String date = homeDataBean.getDate();
            uploadDate = simpleDateFormat.parse(date);
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
    }


    public void setContentView(BaseViewHolder helper, VideoListItem homeDataBean) {

        RecyclerView recyclerView = helper.getView(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(_context, 2));

        if (recyclerView.getAdapter() == null) {
            VideoItemAdapter adapter = new VideoItemAdapter(_context, homeDataBean.getVideoList());
            recyclerView.setAdapter(adapter);
        } else {
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }



    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setListener(IVideoModelListener l) {
        _listener = l;
    }

}
