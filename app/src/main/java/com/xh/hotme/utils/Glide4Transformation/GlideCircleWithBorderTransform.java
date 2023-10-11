package com.xh.hotme.utils.Glide4Transformation;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

/**
 * Create by zhaozhihui on 2019-05-24
 * 加载圆形头像带白色边框
 */
public class GlideCircleWithBorderTransform extends BitmapTransformation {

    private static final String ID = GlideCircleWithBorderTransform.class.getName();
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

    private Paint mBorderPaint;
    private int mBorderWidth;
    private int mBorderColor;


    public GlideCircleWithBorderTransform(){
        super();
    }

    public GlideCircleWithBorderTransform(Context context, int borderWidth, int borderColor) {
        super();
        mBorderWidth = (int)Resources.getSystem().getDisplayMetrics().density * borderWidth;
        mBorderColor = borderColor;

        mBorderPaint = new Paint();
        mBorderPaint.setDither(true);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(borderColor);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mBorderWidth);
    }
    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        return circleCrop(pool, toTransform);
    }

    private Bitmap circleCrop(BitmapPool pool, Bitmap source) {
        if (source == null) {
            return null;
        }
        int size = Math.min(source.getWidth(), source.getHeight()) - (mBorderWidth / 2);
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;
        Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);
        Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
        if (result == null) {
            result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        }
        //创建画笔 画布 手动描绘边框
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        paint.setAntiAlias(true);
        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);
        if (mBorderPaint != null) {
            float borderRadius = r - mBorderWidth / 2;
            canvas.drawCircle(r, r, borderRadius, mBorderPaint);
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GlideCircleWithBorderTransform
                && ((GlideCircleWithBorderTransform)obj).mBorderColor==mBorderColor
                && ((GlideCircleWithBorderTransform)obj).mBorderWidth==mBorderWidth
                ;
    }

    @Override
    public int hashCode() {
        return ID.hashCode() + mBorderWidth *100 + mBorderColor +10 ;
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
//        messageDigest.update(ID_BYTES);

        messageDigest.update((ID + mBorderWidth + mBorderColor).getBytes(CHARSET));
    }
}
