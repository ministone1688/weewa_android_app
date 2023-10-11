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
import com.xh.hotme.bean.TextModelBean;
import com.xh.hotme.listener.IPlaceTypeListener;
import com.xh.hotme.listener.ITextModelListener;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.DeviceInfo;
import com.xh.hotme.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;


@Keep
public class PlaceTypeDialog extends Dialog {
    // views
    private final TextView _okButton;
    private final TextView _selectView;
    private final ImageView _cancelButton;

    // listener
    private IPlaceTypeListener _listener;

    List<TextModelBean> _placeList = new ArrayList<>();
    List<TextModelBean> _ageList = new ArrayList<>();

    RecyclerView _placeRecyclerView, _ageRecyclerView;

    TextModelBean _placeSelectBean, _ageSelectBean;

    TextAdapter _placeAdapter, _ageAdapter;

    Context _ctx;


    public PlaceTypeDialog(@NonNull final Context context) {
        super(context, R.style.hotme_custom_dialog);

        _ctx = context;

        // load content view
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_place_type, null);

        // views
        _okButton = view.findViewById(R.id.btn_next);
        _cancelButton = view.findViewById(R.id.iv_close);

        _placeRecyclerView = view.findViewById(R.id.recyclerView_place);

        _ageRecyclerView = view.findViewById(R.id.recyclerView_age);

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

                if (_placeSelectBean == null) {
                    ToastUtil.s(_ctx, getContext().getString(R.string.info_please_select_place));
                    return true;
                }
                if (_ageSelectBean == null) {
                    ToastUtil.s(_ctx, getContext().getString(R.string.info_please_select_age));
                    return true;
                }

                if (_listener != null) {
                    _listener.onSelect(_placeSelectBean, _ageSelectBean);
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

    public void setOnClickListener(IPlaceTypeListener listener) {
        _listener = listener;
    }


    public void initData() {

        _placeList.add(new TextModelBean(Constants.PLACE_TYPE_FIVE, getContext().getString(R.string.place_type_0)));
        _placeList.add(new TextModelBean(Constants.PLACE_TYPE_EIGHT, getContext().getString(R.string.place_type_8)));
        _placeList.add(new TextModelBean(Constants.PLACE_TYPE_ELEVEN, getContext().getString(R.string.place_type_11)));

        _placeAdapter = new TextAdapter(_ctx, _placeList);
        _placeAdapter.setLiseter(new ITextModelListener() {
            @Override
            public void onSelect(int position) {
                _placeSelectBean = _placeList.get(position);

                for (int i = 0; i < _placeList.size(); i++) {
                    _placeList.get(i).setSelect(i == position);
                }
                _placeAdapter.notifyDataSetChanged();
            }
        });
        _placeRecyclerView.setAdapter(_placeAdapter);
        _placeRecyclerView.setLayoutManager(new GridLayoutManager(_ctx, 3));

        _ageList.add(new TextModelBean(Constants.AGE_TYPE_FORTY_LOW, getContext().getString(R.string.age_type_forty_low)));
        _ageList.add(new TextModelBean(Constants.AGE_TYPE_FORTY_OVER, getContext().getString(R.string.age_type_forty_over)));

        _ageAdapter = new TextAdapter(_ctx, _ageList);
        _ageAdapter.setLiseter(new ITextModelListener() {
            @Override
            public void onSelect(int position) {
                _ageSelectBean = _ageList.get(position);

                for (int i = 0; i < _ageList.size(); i++) {
                    _ageList.get(i).setSelect(i == position);
                }
                _ageAdapter.notifyDataSetChanged();
            }
        });
        _ageRecyclerView.setAdapter(_ageAdapter);

        _ageRecyclerView.setLayoutManager(new GridLayoutManager(_ctx, 3));

    }
}
