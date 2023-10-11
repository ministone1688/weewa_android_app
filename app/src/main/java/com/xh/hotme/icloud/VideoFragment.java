package com.xh.hotme.icloud;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xh.hotme.R;
import com.xh.hotme.base.BaseFragment;
import com.xh.hotme.bean.IcloudVideoBean;
import com.xh.hotme.bean.IcloudVideoResultBean;
import com.xh.hotme.http.OkHttpCallbackDecode;
import com.xh.hotme.http.SdkApi;
import com.xh.hotme.icloud.item.DateItem;
import com.xh.hotme.icloud.item.ListItem;
import com.xh.hotme.icloud.item.VideoListItem;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.OkHttpUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by GJK on 2018/11/9.
 */

public class VideoFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = VideoFragment.class.getSimpleName();

    RecyclerView _recyclerView;

    VideoListAdapter _adapter;

    int _videoCategory;
    List<ListItem> _dataList;

    public static VideoFragment newInstance(int type) {
        VideoFragment fragment = new VideoFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.VIDEO_CATEGORY, type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.icloud_fragment_video, container, false);

        _recyclerView = rootView.findViewById(R.id.recyclerView);

        Bundle bundle = getArguments();
        if (bundle != null) {
            _videoCategory = bundle.getInt(Constants.VIDEO_CATEGORY, 0);
        }

        _dataList = new ArrayList<>();

        initFakeData();

        _adapter = new VideoListAdapter(getContext(), _dataList);

        _recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        _recyclerView.setAdapter(_adapter);

        return rootView;
    }

    @Override
    public void onClick(View v) {

    }

    private void initFakeData() {

        _dataList.add(new DateItem("2023-05-15"));

        IcloudVideoBean videoBean2 = new IcloudVideoBean();
        videoBean2.videoId = "2";
        videoBean2.deviceMac = "123456";
        videoBean2.videoName = "贵州村霸VS香港明星1";
        videoBean2.duration = 30;
        videoBean2.playNumber = 1002;
        videoBean2.path = "";
        videoBean2.shootTime = "2023-05-15 08:00:00";

        IcloudVideoBean videoBean3 = new IcloudVideoBean();
        videoBean3.videoId = "3";
        videoBean3.deviceMac = "123456";
        videoBean3.videoName = "贵州村霸VS香港明星2";
        videoBean3.duration = 30;
        videoBean3.playNumber = 1002;
        videoBean3.path = "";
        videoBean3.shootTime = "2023-05-15 08:00:00";

        List<IcloudVideoBean> videoList2 = new ArrayList<>();
        videoList2.add(videoBean2);
        videoList2.add(videoBean3);

        _dataList.add( new VideoListItem(videoList2));

        _dataList.add(new DateItem("2023-05-14"));

        IcloudVideoBean videoBean1 = new IcloudVideoBean();
        videoBean1.videoId = "1";
        videoBean1.deviceMac = "123456";
        videoBean1.videoName = "/* 克雷塔罗VS蓝十字 */";
        videoBean1.duration = 30;
        videoBean1.playNumber = 1002;
        videoBean1.path = "";
        videoBean1.shootTime = "2023-05-14 08:00:00";

        List<IcloudVideoBean> videoList1 = new ArrayList<>();
        videoList1.add(videoBean1);

        _dataList.add(new VideoListItem(videoList1));

    }


    private int _pageIndex = 0;

    private final int _pageSize = 10;

    private void getVideoList() {
        _pageIndex++;
        String url = SdkApi.getVideoList() + "?classify=" + _videoCategory + "&pageNum= " + _pageIndex + "&pageSize=" + _pageSize;
        OkHttpUtil.get(url, null, new OkHttpCallbackDecode<IcloudVideoResultBean>() {
            @Override
            public void onDataSuccess(IcloudVideoResultBean data) {
                if (data != null) {
                    if (_pageIndex == 1) {
                        _dataList.clear();

                    }
                    if (data.getRows() != null && data.getRows().size() > 0) {
                        for (IcloudVideoBean videoBean : data.getRows()) {
                            String shootTime = videoBean.getShootTime();
                            String shootDate = shootTime.split("T")[0];
                            boolean isExist = false;
                            if (_dataList != null && _dataList.size() > 0) {
                                for (ListItem item: _dataList){
                                    if(item instanceof VideoListItem){
                                        List<IcloudVideoBean> videoBeanList = ((VideoListItem) item).getVideoList();
                                        String date = videoBeanList.get(0).shootTime.split("T")[0];
                                        if(date.equalsIgnoreCase(shootDate)){
                                            videoBeanList.add(videoBean);
                                            isExist = true;
                                            break;
                                        }
                                    }
                                }
                                if(!isExist){
                                    _dataList.add(new DateItem(shootDate));

                                    List<IcloudVideoBean> videoBeanList = new ArrayList<>();
                                    videoBeanList.add(videoBean);

                                    VideoListItem item = new VideoListItem(videoBeanList);
                                    _dataList.add(item);
                                }
                            } else {
                                _dataList.add(new DateItem(shootDate));

                                List<IcloudVideoBean> videoBeanList = new ArrayList<>();
                                videoBeanList.add(videoBean);

                                VideoListItem item = new VideoListItem(videoBeanList);
                                _dataList.add(item);

                            }
                        }
                        _adapter.notifyDataSetChanged();
                    }

                }
            }

            @Override
            public void onFailure(String code, String msg) {

            }
        });
    }
}
