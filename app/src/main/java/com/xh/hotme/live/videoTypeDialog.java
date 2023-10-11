package com.xh.hotme.live;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xh.hotme.R;
import com.xh.hotme.bean.VideoTypeBean;
import com.xh.hotme.listener.IVideoModelListener;
import com.xh.hotme.listener.IVideoTypeListener;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.DeviceInfo;
import com.xh.hotme.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;


@Keep
public class videoTypeDialog extends Dialog {
    // views
    private final TextView _okButton;
    private final TextView _selectView;
    private final ImageView _cancelButton;

    // listener
    private IVideoTypeListener _listener;

    List<VideoTypeBean> _dataList = new ArrayList<>();

    RecyclerView _recyclerView;

    VideoTypeBean _selectBean;

    VideoModelAdapter _adapter;

    Context _ctx;


    public videoTypeDialog(@NonNull final Context context) {
        super(context, R.style.hotme_custom_dialog);

        _ctx = context;

        // load content view
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_video_type, null);

        // views
        _okButton = view.findViewById(R.id.btn_next);
        _cancelButton = view.findViewById(R.id.iv_close);

        _recyclerView = view.findViewById(R.id.recyclerView);
        _selectView = view.findViewById(R.id.tv_select);

        _cancelButton.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                if (_listener != null) {
                    _listener.onCancel();
                }
                return true;
            }
        });

        // ok button
        _okButton.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                if (_selectBean == null) {
                    ToastUtil.s(_ctx, getContext().getString(R.string.info_please_select_mode));
                    return true;
                }
                if (_listener != null) {
                    _listener.onSelect(_selectBean);
                }
                dismiss();
                return true;
            }
        });

        initData();

        // set content view
        setContentView(view);

        setCancelable(false);
        setCanceledOnTouchOutside(false);

        Window window = getWindow();
        window.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams windowparams = window.getAttributes();
        windowparams.width = DeviceInfo.getWidth(context);
    }

    public void setOnClickListener(IVideoTypeListener listener) {
        _listener = listener;
    }


    public void initData() {

        VideoTypeBean football = new VideoTypeBean();
        football.setName(getContext().getString(R.string.type_football));
        football.setType(Constants.LIVE_TYPE_FOOTBALL);
        football.setIconResSelect(R.mipmap.type_football_selected);
        football.setIconResSelect(R.mipmap.type_football_selected);

        VideoTypeBean basketball = new VideoTypeBean();
        basketball.setName(getContext().getString(R.string.type_basketball));
        basketball.setType(Constants.LIVE_TYPE_BASKETBALL);
        basketball.setIconResSelect(R.mipmap.type_basketball);
        basketball.setIconResSelect(R.mipmap.type_basketball);


        VideoTypeBean training = new VideoTypeBean();
        training.setName(getContext().getString(R.string.type_training));
        training.setType(Constants.LIVE_TYPE_TRAINING);
        training.setIconResSelect(R.mipmap.type_training);
        training.setIconResSelect(R.mipmap.type_training);

        _dataList.add(football);
        _dataList.add(basketball);
        _dataList.add(training);

        _adapter = new VideoModelAdapter(_ctx, _dataList);
        _adapter.setLiseter(new IVideoModelListener() {
            @Override
            public void onSelect(int position) {
                _selectBean = _dataList.get(position);

                for (int i = 0; i < _dataList.size(); i++) {
                    _dataList.get(i).setSelect(i == position);
                }
                _selectView.setText(String.format("%s : %s", R.string.last_select_mode, _selectBean.getName()));
                _adapter.notifyDataSetChanged();
            }
        });
        _recyclerView.setAdapter(_adapter);

        _recyclerView.setLayoutManager(new GridLayoutManager(_ctx, 3));

    }
}
