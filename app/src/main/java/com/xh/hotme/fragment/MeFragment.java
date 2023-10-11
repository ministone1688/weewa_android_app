package com.xh.hotme.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.xh.hotme.R;
import com.xh.hotme.base.BaseFragment;
import com.xh.hotme.me.MeHomeAdapter;
import com.xh.hotme.me.MeModuleBean;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.widget.InterceptRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MeFragment extends BaseFragment {
    // views
    private InterceptRecyclerView _recyclerView;
    private SwipeRefreshLayout _refreshLayout;
    View _rootView;



    ViewGroup _adContainer;

    MeHomeAdapter _meHomeAdapter;
    Context _context;

    List<MeModuleBean> _moduleBeanList =new ArrayList<>();

    @Keep
    public static MeFragment newInstance() {
        MeFragment f = new MeFragment();
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _context = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // load view
        Context ctx = getActivity();
        _rootView = inflater.inflate(R.layout.tab_fragment_me, container, false);
        _refreshLayout = _rootView.findViewById(R.id.refreshLayout);
        _recyclerView = _rootView.findViewById(R.id.recyclerView);
        _adContainer = _rootView.findViewById(R.id.ad_container);

        _refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (_meHomeAdapter != null) {
                    // reload
                    _meHomeAdapter.notifyDataSetChanged();
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        _refreshLayout.setRefreshing(false);
                    }
                });

            }
        });

//        if (!EventBus.getDefault().isRegistered(this)) {
//            EventBus.getDefault().register(this);
//        }

        _meHomeAdapter = new MeHomeAdapter(getActivity());

        initModules();

        _recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        _recyclerView.setAdapter(_meHomeAdapter);

        _recyclerView.setTouchListener(new InterceptRecyclerView.onTouchListener() {
            @Override
            public void onTouch() {
//                if (guideViewFragment != null && guideViewFragment.getShowsDialog()) {
//                    guideViewFragment.dismiss();
//                    return;
//                }
            }
        });
        // return
        return _rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        EventBus.getDefault().unregister(this);
        for (MeModuleBean moduleBean: _moduleBeanList){
            moduleBean.onDestroy();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        // update
//        if(!hidden && isLoginInfoUpdated(_loginInfoVersion)) {
//            _meHomeAdapter.notifyDataSetChanged();
//        }
    }

    private void initModules() {
        _moduleBeanList.add(new MeModuleBean(Constants.ME_MODULE_PROFILE));
        _moduleBeanList.add(new MeModuleBean(Constants.ME_MODULE_SETTING));
        _moduleBeanList.add(new MeModuleBean(Constants.ME_MODULE_OUT));
        _meHomeAdapter.setModels(_moduleBeanList);
    }
}
