package com.xh.hotme.icloud;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.flyco.tablayout.SegmentTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.xh.hotme.R;
import com.xh.hotme.base.BaseFragment;
import com.xh.hotme.camera.LocalVideoFilterListFragment;
import com.xh.hotme.utils.Constants;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by GJK on 2018/11/9.
 */

public class ILocalStorageFragment extends BaseFragment implements View.OnClickListener {


    SegmentTabLayout _tabLayout;
    ViewPager _viewPager;

    List<LocalVideoFilterListFragment> mFragmentList = new ArrayList<>();

    // title of tabs
    private final String [] _titles =  new String[]{"精彩集锦", "整场回放"};

    public static ILocalStorageFragment newInstance() {
        ILocalStorageFragment fragment = new ILocalStorageFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.icloud_fragment_local_storage, container, false);


        _tabLayout = rootView.findViewById(R.id.tabs);
        _viewPager = rootView.findViewById(R.id.view_pager);

//        _viewPager.setOffscreenPageLimit(2);
//        _viewPager.addOnPageChangeListener(this);

        mFragmentList.add(LocalVideoFilterListFragment.newInstance(Constants.VIDEO_CATEGORY_TOP, true));
        mFragmentList.add(LocalVideoFilterListFragment.newInstance(Constants.VIDEO_CATEGORY_GALLERY, true));

        // setup view pager
        _viewPager.setAdapter(new TabPagerAdapter(getChildFragmentManager()));


        _tabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                _viewPager.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {

            }
        });

        //滑动、点击切换页面
        _viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                _tabLayout.setCurrentTab(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        // set tabs
        _tabLayout.setTabData(_titles);
        _tabLayout.setCurrentTab(0);

        return rootView;
    }

    @Override
    public void onClick(View v) {

    }


    private class TabPagerAdapter extends FragmentStatePagerAdapter {
        public TabPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return _titles[position];
        }

        @Override
        public Fragment getItem(int position) {

            return mFragmentList.get(position);
        }
    }
}
