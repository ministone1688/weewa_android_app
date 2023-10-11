package com.xh.hotme.live;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.xh.hotme.R;
import com.xh.hotme.bean.TextModelBean;
import com.xh.hotme.listener.ITextModelListener;

import java.util.List;

/**
 * Created by GJK on 2018/11/9.
 */

public class TextAdapter extends BaseQuickAdapter<TextModelBean, BaseViewHolder> {
    private final static String TAG = "BluetoothDeviceAdapter";
    ITextModelListener _listener;

    public TextAdapter(Context context, List data) {

        super(R.layout.dialog_text_list_item, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, TextModelBean typeBean) {
        TextView tvName = (TextView) baseViewHolder.findView(R.id.tv_name);
        tvName.setText(typeBean.getName());
        int position = baseViewHolder.getPosition();

        baseViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typeBean.setSelect(true);
                if (_listener != null) {
                    _listener.onSelect(position);
                }

            }
        });

        if (typeBean.isSelect()) {
            tvName.setTextColor(getContext().getColor(R.color.white));
            baseViewHolder.itemView.setBackgroundResource(R.drawable.text_list_bg_selected);
        } else {
            tvName.setTextColor(getContext().getColor(R.color.text_black_3D));
            baseViewHolder.itemView.setBackgroundResource(R.drawable.text_list_bg_unselect);
        }
    }

    private void setStatus() {


    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setLiseter(ITextModelListener l) {
        _listener = l;
    }

}
