package com.xh.hotme.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.annotation.Keep;

import android.view.View;
import android.widget.ImageView;


import java.io.File;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.xh.hotme.R;
import com.xh.hotme.listener.IGlideLoadListener;
import com.xh.hotme.listener.IImageLoadListener;
import com.xh.hotme.utils.Glide4Transformation.GlideCircleWithBorderTransform;
import com.xh.hotme.utils.Glide4Transformation.RoundedCornersTransformation;
import com.xh.hotme.utils.Glide4Transformation.RoundedCornersWithBorderTransform;

import java.io.File;
import java.lang.reflect.Method;

/**
 * Create by zhaozhihui on 2019-05-23
 **/

@Keep
public class GlideUtil {
    private static final String TAG = GlideUtil.class.getSimpleName();
    public static String version = "4.11.0";

    public static void load(Context context, String url) {
        Glide.with(context).load(url);
    }

    public static void load(Context context, String url, ImageView view) {
        RequestOptions options = new RequestOptions();
        options.diskCacheStrategy(DiskCacheStrategy.DATA);
        Glide.with(context).load(url).apply(options).into(view);
    }

    public static void loadBorder(Context context, String url, ImageView view, int borderWidth, int borderColor) {

        RoundedCornersWithBorderTransform transform = new RoundedCornersWithBorderTransform(context, dip2px(context, 0), dip2px(context, borderWidth), borderColor);
        RequestOptions options = new RequestOptions().transform(transform).diskCacheStrategy(DiskCacheStrategy.DATA);
        Glide.with(context).load(url).apply(options).into(view);
    }


    public static void load(Context context, int resId, ImageView view) {
        RequestOptions options = new RequestOptions();
        options.diskCacheStrategy(DiskCacheStrategy.DATA);
        Glide.with(context).load(resId).apply(options).into(view);
    }

    public static void load(Context context, String url, ImageView view, int placeholderResId) {
        RequestOptions options = new RequestOptions();
        options.diskCacheStrategy(DiskCacheStrategy.DATA);
        options.placeholder(placeholderResId);
        Glide.with(context).load(url).apply(options).into(view);
    }

    public static void load(Context context, String url, ImageView view, int placeholderResId, int errorResId) {
        RequestOptions options = new RequestOptions();
        options.diskCacheStrategy(DiskCacheStrategy.DATA);
        options.placeholder(placeholderResId);
        options.error(errorResId);
        Glide.with(context).load(url).apply(options).into(view);
    }

    public static void loadOrigin(Context context, String url, ImageView view, int placeholderResId) {
        RequestOptions options = new RequestOptions();
        options.diskCacheStrategy(DiskCacheStrategy.DATA);
        options.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
        options.placeholder(placeholderResId);
        Glide.with(context).load(url).apply(options).into(view);
    }

    public static void load(Context context, String url, RequestOptions options, ImageView view) {
        Glide.with(context).load(url).apply(options).into(view);
    }

    /*
     * 加在圆形图片
     */
    public static void loadCircle(Context context, String url, ImageView view) {
        RequestOptions options = new RequestOptions();
        options.diskCacheStrategy(DiskCacheStrategy.DATA);

        Glide.with(context).load(url).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(view);
    }

    public static void loadCircleWithBorder(Context context, String url, ImageView view, int borderWidth, int borderColor) {
        Glide.with(context).load(url)
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA)
                        .transform(new CenterCrop(), new GlideCircleWithBorderTransform(context, borderWidth, borderColor)))
                .into(view);
    }

    public static void loadCircleWithBorder(Context context, int resId, ImageView view, int borderWidth, int borderColor) {
        Glide.with(context).load(resId)
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA)
                        .transform(new CenterCrop(), new GlideCircleWithBorderTransform(context, borderWidth, borderColor)))
                .into(view);
    }


    public static void loadRoundedCorner(Context context, int resId, ImageView view, int cornerDp) {
        int px = dip2px(context, cornerDp);
        RequestOptions options = new RequestOptions();
        options.bitmapTransform(new RoundedCorners(px));

        Glide.with(context).asBitmap()
                .load(resId).apply(options)
                .into(view);

    }

    public static void loadRoundedCorner(Context context, String url, ImageView view, int cornerDp) {
        RequestOptions options = new RequestOptions();
        options.diskCacheStrategy(DiskCacheStrategy.DATA);
        options.bitmapTransform(new RoundedCorners(dip2px(context, cornerDp)));
        Glide.with(context)
                .load(url).apply(options)
                .into(view);
    }


    public static void loadRoundedCorner(Context context, String url, ImageView view, int cornerDp, boolean leftTop, boolean rightTop, boolean leftBottom, boolean rightBottom) {
        RoundedCornersTransformation transform = new RoundedCornersTransformation(context, dip2px(context, cornerDp));
        transform.setNeedCorner(leftTop, rightTop, leftBottom, rightBottom);
        RequestOptions options = new RequestOptions().transform(transform).diskCacheStrategy(DiskCacheStrategy.DATA);
        Glide.with(context).load(url).apply(options).into(view);
    }

    public static void loadImageResource(Context context, String url, final IGlideLoadListener listener) {
        RequestOptions options = new RequestOptions();
        options.diskCacheStrategy(DiskCacheStrategy.DATA);
        Glide.with(context).load(url).apply(options).into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                if (listener != null) {
                    listener.onResourceReady(resource);
                }
            }

        });

    }

    public static void loadPhotoPicker(Context context, File file, ImageView view, int override_with, int override_height, int placeholder, int errorholder, float thumbnail) {
        RequestOptions options = new RequestOptions();
        options.diskCacheStrategy(DiskCacheStrategy.DATA);
        options.dontAnimate();
        options.dontTransform();
        options.override(override_with, override_height);
        options.placeholder(placeholder);
        options.error(errorholder);
        Glide.with(context).load(file).apply(options)
                .thumbnail(thumbnail)
                .into(view);
    }

    public static void loadPhotoPicker(Context context, String url, ImageView view, int override_with, int override_height, int placeholder, int errorholder, float thumbnail) {
        RequestOptions options = new RequestOptions();
        options.diskCacheStrategy(DiskCacheStrategy.DATA);
        options.dontAnimate();
        options.dontTransform();
        options.override(override_with, override_height);
        options.placeholder(placeholder);
        options.error(errorholder);
        Glide.with(context).load(url).apply(options)
                .thumbnail(thumbnail)
                .into(view);

    }

    public static void loadPhotoPicker(Context context, Uri uri, ImageView view, int override_with, int override_height, int placeholder, int errorholder, float thumbnail) {
        RequestOptions options = new RequestOptions();
        options.diskCacheStrategy(DiskCacheStrategy.DATA);
        options.dontAnimate();
        options.dontTransform();
        options.override(override_with, override_height);
        options.placeholder(placeholder);
        options.error(errorholder);
        Glide.with(context).load(uri).apply(options)
                .thumbnail(thumbnail)
                .into(view);

    }

    public static void pauseRequests(Context context) {
        Glide.with(context).pauseRequests();
    }

    public static void resumeRequests(Context context) {
        Glide.with(context).resumeRequests();
    }


    public static void clearMemory(Context context) {
        Glide.get(context).clearMemory();
    }

    public static void downloadOnly(Context context, String url, final IImageLoadListener listener) {
        RequestOptions options = new RequestOptions();
        options.diskCacheStrategy(DiskCacheStrategy.DATA);
        Glide.with(context)
                .load(url)
                .apply(options)
                .downloadOnly(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(File resource, Transition<? super File> transition) {
                        if(listener != null) {
                            listener.onComplete(resource);
                        }
                    }
                });
    }

    public static Bitmap getBitmap(Context context, String url) {
        RequestOptions options = new RequestOptions();
        options.diskCacheStrategy(DiskCacheStrategy.DATA);
        FutureTarget<Bitmap> futureTarget = Glide.with(context).asBitmap()
                .load(url)
                .apply(options)
                .submit();
        try {
            return futureTarget.get();
        } catch(Throwable e) {
            return null;
        }
    }


    public static void loadDrawable(View view, Drawable drawable) {
        Glide.with(view)
                .asDrawable()
                .load(drawable)
                .transform(new CenterCrop())
                .override(view.getMeasuredWidth(), view.getMeasuredHeight())
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                            view.setBackgroundDrawable(resource);
                        } else {
                            view.setBackground(resource);
                        }

                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }


    public static void loadDrawable(View view, Drawable drawable, int cornerDp) {
        Glide.with(view)
                .asDrawable()
                .load(drawable)
                .transform(new CenterCrop(), new RoundedCorners(cornerDp))
                .override(view.getMeasuredWidth(), view.getMeasuredHeight())
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                            view.setBackgroundDrawable(resource);
                        } else {
                            view.setBackground(resource);
                        }

                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }


    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }



}
