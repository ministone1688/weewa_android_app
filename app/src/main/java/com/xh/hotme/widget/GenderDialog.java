package com.xh.hotme.widget;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.xh.hotme.R;
import com.xh.hotme.me.holder.CommonViewHolder;
import com.xh.hotme.utils.BaseAppUtil;

import java.util.Arrays;
import java.util.List;

/**
 * 性别选择对话框
 */
@Keep
public class GenderDialog extends Dialog {

    private final List<String> _genders = Arrays.asList(
            "保密",
            "男",
            "女"
    );

    RecyclerView listView;
    GenderAdapter _adapter;

    // listener
    private final onGenderListener _listener;

    public GenderDialog(@NonNull Context context) {
        this(context, null);
    }


    public GenderDialog(@NonNull Context context, onGenderListener listener) {
        super(context, R.style.customDialog);

        // load content view
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_userinfo_gender, null);

        listView = view.findViewById(R.id.list);

        listView.addItemDecoration(new RecycleViewDivider(getContext(), LinearLayoutManager.VERTICAL, getContext().getResources().getColor(R.color.bg_gray)));

        listView.setLayoutManager(new LinearLayoutManager(getContext()));
        _adapter = new GenderAdapter();

        listView.setAdapter(_adapter);

        // init
        _listener = listener;

        // set content view
        setContentView(view);

        Window window = getWindow();
        WindowManager.LayoutParams windowparams = window.getAttributes();
        windowparams.width = BaseAppUtil.getDeviceWidth(context);
        windowparams.gravity= Gravity.BOTTOM;
    }


    @Override
    public void onBackPressed() {
        if (_listener != null) {
            _listener.onCancel();
        }
        super.onBackPressed();
    }

    private class GenderAdapter extends RecyclerView.Adapter<CommonViewHolder<String>> {
        @NonNull
        @Override
        public CommonViewHolder<String> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_gender, parent, false);

            return new GenderHolder(view);

        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @SuppressLint("RecyclerView")
        @Override
        public void onBindViewHolder(@NonNull CommonViewHolder<String> holder, final int position) {
            holder.onBind(_genders.get(position), position);

        }

        @Override
        public int getItemCount() {
            return _genders.size();
        }
    }

    public class GenderHolder extends CommonViewHolder<String> {
        private final TextView _nameLabel;

        public GenderHolder(View view) {
            super(view);
            _nameLabel = view.findViewById(R.id.name);
        }

        @Override
        public void onBind(String model, final int position) {
            // name
            _nameLabel.setText(model);
            _nameLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (_listener != null) {
                        _listener.onSelected(position);
                    }
                    dismiss();
                }
            });
        }
    }

    public interface onGenderListener {
        void onSelected(int index);

        void onCancel();
    }
}
