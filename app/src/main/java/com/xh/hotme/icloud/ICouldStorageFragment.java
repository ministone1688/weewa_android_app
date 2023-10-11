package com.xh.hotme.icloud;

import static com.bun.miitmdid.content.ContextKeeper.getApplicationContext;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.alibaba.fastjson.JSON;
import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.SegmentTabLayout;
import com.flyco.tablayout.SlidingTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.xh.hotme.MainActivity;
import com.xh.hotme.R;
import com.xh.hotme.account.LoginManager;
import com.xh.hotme.account.MobileLoginActivity;
import com.xh.hotme.base.BaseFragment;
import com.xh.hotme.lay.IcoldVideoActivity;
import com.xh.hotme.lay.adaper.BaseRecyclerAdapter;
import com.xh.hotme.lay.adaper.BaseViewHolder;
import com.xh.hotme.lay.adaper.MyGridLayoutManager;
import com.xh.hotme.lay.javabean.IcouldVideoBean;
import com.xh.hotme.lay.utils.MyToolUtils;
import com.xh.hotme.utils.Constants;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by GJK on 2018/11/9.
 */

public class ICouldStorageFragment extends BaseFragment {
    private static final String TAG = ICouldStorageFragment.class.getSimpleName();

    SegmentTabLayout _tabLayout;
    ViewPager _viewPager;
    LinearLayout is_login;
    RelativeLayout no_login;
    Button go_to_login;

    RecyclerView _icould_rev;

    private List<IcouldVideoBean.Lists> mDataList = new ArrayList<>();
    private BaseRecyclerAdapter<IcouldVideoBean.Lists> mAdapter;

    public static ICouldStorageFragment newInstance() {
        ICouldStorageFragment fragment = new ICouldStorageFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.icloud_fragment_icloud_storage, container, false);
        return rootView;
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        is_login = getActivity().findViewById(R.id.is_login);
        no_login = getActivity().findViewById(R.id.no_login);
        go_to_login = getActivity().findViewById(R.id.go_to_login);

        go_to_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MobileLoginActivity.startActivityByRequestCode(getActivity(), Constants.REQUEST_CODE_LOGIN_HOME_DEVICE);
            }
        });


        //initData();
    }

    public void onStart() {
        super.onStart();

        if (!LoginManager.isSignedIn(getActivity())) {
            no_login.setVisibility(View.VISIBLE);
            is_login.setVisibility(View.GONE);
        }else{
            is_login.setVisibility(View.VISIBLE);
            no_login.setVisibility(View.GONE);
        }
    }

    private void initData() {
        _icould_rev = getActivity().findViewById(R.id.icould_rev);
        MyGridLayoutManager LineViewx=new MyGridLayoutManager(getContext(),1);
        _icould_rev.setLayoutManager(LineViewx);
       // mAdapter = new CouldListAdaper(getApplicationContext(),R.layout.item_could_video_list, mDataList);
        mAdapter.setEmptyView(LayoutInflater.from(getContext()).inflate(R.layout.layout_empty_view, null, false));
        _icould_rev.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();

        //mDataList.clear();
        //  String result = "";
        // IcouldVideoBean reponse = JSON.parseObject(result,IcouldVideoBean.class);
        // mDataList.addAll(reponse.getLists());
        //mAdapter.notifyDataSetChanged();
    }

    public class CouldListAdaper extends BaseRecyclerAdapter<IcouldVideoBean.Lists> {
        public CouldListAdaper(Context context, int layoutResId, List<IcouldVideoBean.Lists> data) {
            super(context, layoutResId, data);
        }

        @Override
        protected void convert(final BaseViewHolder holder, final IcouldVideoBean.Lists item) {
            holder.setText(R.id.item_title,item.getTitle());
            holder.setText(R.id.item_title1,item.getTitle());
            holder.setText(R.id.item_title2,item.getTitle());
            holder.setText(R.id.item_title3,item.getTitle());
            ImageView goods_img = holder.getView(R.id.video_img);
            MyToolUtils.glideShowImg(getApplicationContext(),item.getLitpic(),goods_img);

            holder.getView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intGoods = new Intent(getApplicationContext(), IcoldVideoActivity.class);
                    intGoods.putExtra("goods_id",item.getTitle());
                    intGoods.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MyToolUtils.goActivity(getActivity(),intGoods);
                }
            });
        }
    }

}
