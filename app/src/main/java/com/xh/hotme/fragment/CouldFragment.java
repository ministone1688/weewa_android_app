package com.xh.hotme.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.SlidingTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.xh.hotme.R;
import com.xh.hotme.base.BaseFragment;
import com.xh.hotme.camera.CameraVideoListFragment;
import com.xh.hotme.icloud.ICouldStorageFragment;
import com.xh.hotme.icloud.ILocalStorageFragment;
import com.xh.hotme.utils.BaseAppUtil;
import com.xh.hotme.utils.DensityUtil;

import java.util.ArrayList;
import java.util.List;


public class CouldFragment extends BaseFragment implements View.OnClickListener , OnTabSelectListener, ViewPager.OnPageChangeListener{
    private static final String TAG = CouldFragment.class.getSimpleName();

    List<Fragment> mFragmentList = new ArrayList<>();
    SlidingTabLayout _tabLayout;

    ViewPager _viewPager;

    int _activeCatIndex = 0;

    String[] titleName = new String[]{"云存储", "相机存储","手机存储"};

    public static CouldFragment newInstance() {
        CouldFragment fragment = new CouldFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.home_fragment_icloud, container, false);
        _tabLayout = rootView.findViewById(R.id.tabs);
        _viewPager = rootView.findViewById(R.id.view_pager);

        mFragmentList.add(ICouldStorageFragment.newInstance());
        mFragmentList.add(ILocalStorageFragment.newInstance());
        mFragmentList.add(ILocalStorageFragment.newInstance());

        _viewPager.setOffscreenPageLimit(3);
        _viewPager.addOnPageChangeListener(this);

        // setup pager
        _viewPager.setAdapter(new VideoPagerAdapter(getFragmentManager()));


        // set to init cat index
        if (_viewPager.getCurrentItem() != _activeCatIndex) {
            _viewPager.setCurrentItem(_activeCatIndex, false);
        }

        // setup tabs
        _tabLayout.setOnTabSelectListener(this);

       // add tab
        ArrayList<CustomTabEntity> tabEntities = new ArrayList<>();
        for(int i=0; i< titleName.length; i++){
            tabEntities.add(new TabEntity(titleName[i], 0, 0));
        }

        // set tabs
        _tabLayout.setViewPager(_viewPager);
//        _tabLayout.setTabData(tabEntities);
        _tabLayout.setCurrentTab(0);

        return rootView;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        _tabLayout.setCurrentTab(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onTabSelect(int position) {

        _viewPager.setCurrentItem(position);
    }

    @Override
    public void onTabReselect(int position) {

    }

    public class TabEntity implements CustomTabEntity {
        public String title;
        public int selectedIcon;
        public int unSelectedIcon;

        public TabEntity(String title, int selectedIcon, int unSelectedIcon) {
            this.title = title;
            this.selectedIcon = selectedIcon;
            this.unSelectedIcon = unSelectedIcon;
        }

        @Override
        public String getTabTitle() {
            return title;
        }

        @Override
        public int getTabSelectedIcon() {
            return selectedIcon;
        }

        @Override
        public int getTabUnselectedIcon() {
            return unSelectedIcon;
        }
    }



    private class VideoPagerAdapter extends FragmentPagerAdapter {
        public VideoPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return titleName.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titleName[position];
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

    }
}
