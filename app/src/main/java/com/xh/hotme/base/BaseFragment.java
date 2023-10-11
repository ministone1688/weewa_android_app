package com.xh.hotme.base;

import android.os.Bundle;
import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.View;

import com.xh.hotme.R;
import com.xh.hotme.bluetooth.IBleScanRequestListener;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.widget.LoadingDialog;


/**
 * Created by liu hong liang on 2017/4/27.
 */

@Keep
public class BaseFragment extends Fragment {
    private static final String TAG = BaseFragment.class.getSimpleName();
    private View titleView;

    LoadingDialog mDialog;

    public IBleScanRequestListener _bleScanRequestListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDialog = new LoadingDialog(getContext());
    }

    public void setBleScanRequestListener(IBleScanRequestListener l){
       this._bleScanRequestListener = l;
    }

    public IBleScanRequestListener getBleScanRequestListener(){
       return this._bleScanRequestListener;
    }

    /**
     * 改变标题栏显示状态
     *
     * @param show
     */
    public void changeTitleStatus(boolean show) {
        if (titleView == null) {
            AppTrace.e(TAG, "没有设置titleView");
            return;
        }
        if (show) {
            titleView.setVisibility(View.VISIBLE);
        } else {
            titleView.setVisibility(View.GONE);
        }
    }

    /**
     * 设置标题栏view
     *
     * @param titleView
     */
    public void setTitleView(View titleView) {
        this.titleView = titleView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        mDialog = null;
    }

    @Keep
    public void showLoading(String message) {
        if (getActivity() != null && !(getActivity().isDestroyed())) {
            if (mDialog != null && mDialog.isShowing()) {
                return;
            }
            mDialog = new LoadingDialog(getContext());
            mDialog.setCancelable(false);
            mDialog.show(message);
        }
    }

    @Keep
    public void showLoading(Boolean cancelable, String message) {
        if (getActivity() != null && !(getActivity().isDestroyed())) {
            if (mDialog != null && mDialog.isShowing()) {
                return;
            }
            mDialog = new LoadingDialog(getContext());
            mDialog.setCancelable(cancelable);
            mDialog.show(message);
        }
    }

    @Keep
    public void showLoading(Boolean cancelable) {
        if (getActivity() != null && !(getActivity().isDestroyed())) {
            if (mDialog != null && mDialog.isShowing()) {
                return;
            }
            mDialog = new LoadingDialog(getContext());
            mDialog.setCancelable(cancelable);
            mDialog.show(getResources().getString(R.string.loading));
        }
    }

    @Keep
    public void dismissLoading() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }
}
