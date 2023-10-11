package com.xh.hotme.camera;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;


import com.xh.hotme.R;
import com.xh.hotme.bean.CameraVideoListBean;
import com.xh.hotme.utils.GlideUtil;
import com.xh.hotme.utils.TimeUtil;

import java.util.ArrayList;

/**
 * date   : 2023/6/19
 * desc   :
 */
public class StickyTopAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<CameraVideoListBean.VideosBean> items;
    private OnItemClick onItemClick;

    public interface OnItemClick {
        void onItemClick(int position);
    }

    public StickyTopAdapter(ArrayList<CameraVideoListBean.VideosBean> items) {
        this.items = items;
    }

    public void setOnItemClickListener(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 1) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_list_item_head, parent, false);
            return new MyHeadViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_list_item_content, parent, false);
            return new MyViewHolder(view);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {
            ((MyViewHolder) holder).bind(position, items.get(position));
        } else if (holder instanceof MyHeadViewHolder) {
            ((MyHeadViewHolder) holder).bind(items.get(position));
        }
    }

    /**
     * Description: HeadViewHolder 标题ViewHolder
     */
    static class MyHeadViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        MyHeadViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tvTopTitle);
        }

        void bind(CameraVideoListBean.VideosBean data) {

//            textView.setText(data.title);
        }
    }

    /**
     * Description: 普通ViewHolder
     */
    class MyViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView tvTopTitle;
        ImageView ivLogo;
        TextView tvVideoNum;
        TextView tvTitle;
        TextView tvGoodNum;
        TextView tvGoalNum;
        View convertView;

        MyViewHolder(View itemView) {
            super(itemView);
            convertView = itemView;
            tvTopTitle = convertView.findViewById(R.id.tvTopTitle);
            ivLogo = convertView.findViewById(R.id.ivLogo);
            tvTitle = convertView.findViewById(R.id.tvTitle);
            tvVideoNum = convertView.findViewById(R.id.tvVideoNum);
            tvGoodNum = convertView.findViewById(R.id.tvGoodNum);
            tvGoalNum = convertView.findViewById(R.id.tvGoalNum);

        }

        void bind(int position, CameraVideoListBean.VideosBean data) {
            tvTopTitle.setVisibility(View.GONE);
            if (isHeadItem(position, position + 1)) {
                tvTopTitle.setText(getHeadTitle(position));
                tvTopTitle.setVisibility(View.VISIBLE);
            }

            if (!TextUtils.isEmpty(data.path)){
               // GlideUtils.showImg(ivLogo, ConnectLogic.getInstance().getConnectBaseurl()+ConnectLogic.getInstance().getBasePath()+data.path+"/"+data.thumb);
                GlideUtil.loadRoundedCorner(ivLogo.getContext(),data.path+"/"+data.thumb, ivLogo, 12);

                tvTitle.setText(data.competition_name);
                //全场视频: 2     精彩集锦: 6     进球视频: 3
                tvVideoNum.setText("全场视频:" + data.full_video_number);
                tvGoodNum.setText("精彩集锦:" + data.highlights_number);
                tvGoalNum.setText("进球视频:" + data.playback_number);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != onItemClick) {
                            onItemClick.onItemClick(position);
                        }
                    }
                });
                tvVideoNum.setBackgroundColor(Color.TRANSPARENT);
                tvGoodNum.setBackgroundColor(Color.TRANSPARENT);
                tvGoalNum.setBackgroundColor(Color.TRANSPARENT);
                tvTitle.setBackgroundColor(Color.TRANSPARENT);
            }


        }


    }

    public boolean isHeadItem(int curPosition, int nextposition) {
        if (curPosition == 0) {
            return true;
        }
        if (nextposition != 0 && nextposition >= items.size()) {
            return false;
        }
        CameraVideoListBean.VideosBean cur = items.get(curPosition);
        CameraVideoListBean.VideosBean next = items.get(nextposition);
        try {
            if (!TextUtils.isEmpty(cur.time) && !TextUtils.isEmpty(next.time) && cur.time.split("T")[0].equals(next.time.split("T")[0]))
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public String getHeadTitle(int position) {
        try {
            CameraVideoListBean.VideosBean cur = items.get(position);
            String time = cur.time.split("T")[0];
            if (TimeUtil.isToday(time))
                return "今天";
            else
                return time;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
