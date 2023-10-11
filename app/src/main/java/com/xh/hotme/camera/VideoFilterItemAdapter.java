package com.xh.hotme.camera;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import com.xh.hotme.R;
import com.xh.hotme.bean.CameraVideoDateListBean;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.http.ConnectLogic;
import com.xh.hotme.listener.IVideoModelListener;
import com.xh.hotme.utils.AppFileUtil;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.DensityUtil;
import com.xh.hotme.utils.GlideUtil;
import com.xh.hotme.utils.ToastUtil;
import com.xh.hotme.widget.roundedimageview.RoundedImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * date   : 2023/6/19
 * desc   :
 */
public class VideoFilterItemAdapter extends BaseQuickAdapter<CameraVideoDateListBean.CameraVideoDateBean, BaseViewHolder> {
    private final static String TAG = "VideoItemAdapter";
    IVideoModelListener _listener;

    boolean enableEdit = false;

    Context _context;

    ArrayList<CameraVideoDateListBean.CameraVideoDateBean> datasSelect = new ArrayList();

    ArrayList<String> paths = new ArrayList();

    long allSize;

    boolean _playMode = false;

    public VideoFilterItemAdapter(Context context, List data, boolean playMode) {
        super(R.layout.icloud_video_list_item_video, data);

        _context = context;

        _playMode = playMode;
    }

    public void setEnableEdit(boolean enableEdit) {
        this.enableEdit = enableEdit;
    }

    public boolean isEnableEdit() {
        return this.enableEdit;
    }

    public void selectAll() {
        datasSelect.clear();
        datasSelect.addAll(getData());
    }

    public void cleanAllSelect() {
        datasSelect.clear();

    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, final CameraVideoDateListBean.CameraVideoDateBean videoBean) {

        int position = baseViewHolder.getPosition();

        TextView tvName = (TextView) baseViewHolder.findView(R.id.name);
        TextView tvDuration = (TextView) baseViewHolder.findView(R.id.duration);

        int cornerDp = (int) _context.getResources().getDimension(R.dimen.video_corner_dp);

        RoundedImageView ivThumb = baseViewHolder.findView(R.id.thumb);
        ivThumb.setCornerRadius(cornerDp);
        CheckBox checkBox = baseViewHolder.findView(R.id.edit);
        checkBox.setVisibility(enableEdit ? View.VISIBLE : View.GONE);
        checkBox.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                if (enableEdit) {
                    if (datasSelect.contains(videoBean)) {
                        datasSelect.remove(videoBean);
                        paths.remove(ConnectLogic.getInstance().getBasePath() + "/" + videoBean.link);
                        allSize -= videoBean.size;
                        if (!TextUtils.isEmpty(videoBean.thumb) && (videoBean.thumb.endsWith("jpg") || videoBean.thumb.endsWith("png"))) {
                            paths.remove(ConnectLogic.getInstance().getBasePath() + "/" + videoBean.thumb);
                        }

                    } else {
                        datasSelect.add(videoBean);
                        allSize += videoBean.size;
                        paths.add(ConnectLogic.getInstance().getBasePath() + "/" + videoBean.link);
                        if (!TextUtils.isEmpty(videoBean.thumb) && (videoBean.thumb.endsWith("jpg") || videoBean.thumb.endsWith("png"))) {
                            paths.add(ConnectLogic.getInstance().getBasePath()+"/" + videoBean.thumb);
                        }
                    }
                    notifyItemChanged(position);
                }
                return true;
            }
        });

        if (enableEdit) {
            checkBox.setChecked(datasSelect.contains(videoBean) ? true : false);
        }

        tvName.setText(videoBean.name);
        tvDuration.setText(String.valueOf(videoBean.duration));

        if (TextUtils.isEmpty(videoBean.thumb) || (!videoBean.thumb.endsWith("jpg") && !videoBean.thumb.endsWith("png"))) {
            GlideUtil.loadRoundedCorner(_context, _context.getResources().getIdentifier("camera_cover_default", "mipmap",
                    _context.getPackageName()), ivThumb, DensityUtil.px2dip(_context, cornerDp));

        } else {
            String thumbUrl = BluetoothManager.getRealFileImageUrl(videoBean.thumb);

            GlideUtil.loadRoundedCorner(_context, thumbUrl, ivThumb, DensityUtil.px2dip(_context, cornerDp));

//            GlideUrl path = new GlideUrl(thumbUrl, new LazyHeaders.Builder().addHeader("device-type", "android").build());
//            RequestOptions options = new RequestOptions();
//            options.diskCacheStrategy(DiskCacheStrategy.DATA);
//            options.bitmapTransform(new RoundedCorners(GlideUtil.dip2px(_context, cornerDp)));
//
//            Glide.with(_context)
//                    .load(path).apply(options)
//
//                    .into(ivThumb);
        }

        baseViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (_playMode) {
                    File videoDir = AppFileUtil.getVideoDir(_context);
                    File videoFile = new File(videoDir, videoBean.link);
                    if (videoFile.exists()) {
                        String fileUrl = videoFile.getPath();
                        VideoPlayActivity.start(_context, fileUrl);
                    } else {
                        ToastUtil.s(_context, "文件不存在或已被删除");
                    }

                } else {

                    if (enableEdit) {
                        if (datasSelect.contains(videoBean)) {
                            datasSelect.remove(videoBean);
                            paths.remove(ConnectLogic.getInstance().getBasePath() + videoBean.link);
                            allSize -= videoBean.size;
                            paths.remove(ConnectLogic.getInstance().getBasePath() + videoBean.thumb);

                        } else {
                            datasSelect.add(videoBean);
                            allSize += videoBean.size;
                            paths.add(ConnectLogic.getInstance().getBasePath() + videoBean.thumb);
                            paths.add(ConnectLogic.getInstance().getBasePath() + videoBean.link);

                        }

                        notifyItemChanged(position);

                    } else {

                    }
                }

            }
        });

    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setListener(IVideoModelListener l) {
        _listener = l;
    }


    public List<CameraVideoDateListBean.CameraVideoDateBean> getSelectFile() {

        return datasSelect;
    }


    public long getSelectFileSize() {
        return allSize;
    }
}
