package com.xh.hotme.utils.Glide4Transformation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;

import java.security.MessageDigest;


/**
 * Todo 设置图片部分圆角
 */
@Keep
public class RoundedCornersWithBorderTransform implements Transformation<Bitmap> {

    private static final String ID = RoundedCornersWithBorderTransform.class.getName();
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);


    private final BitmapPool mBitmapPool;

    private float radius;

    private boolean isLeftTop, isRightTop, isLeftBottom, isRightBotoom;


    private final Paint mBorderPaint;
    private final int mBorderWidth;

    /**
     * 需要设置圆角的部分
     *
     * @param leftTop     左上角
     * @param rightTop    右上角
     * @param leftBottom  左下角
     * @param rightBottom 右下角
     */
    public void setNeedCorner(boolean leftTop, boolean rightTop, boolean leftBottom, boolean rightBottom) {
        isLeftTop = leftTop;
        isRightTop = rightTop;
        isLeftBottom = leftBottom;
        isRightBotoom = rightBottom;
    }


    /**
     * @param context 上下文
     * @param radius  圆角幅度
     */
    public RoundedCornersWithBorderTransform(Context context, float radius, int borderWidth, int borderColor) {
        this.mBitmapPool = Glide.get(context).getBitmapPool();
        this.radius = radius;


        mBorderWidth = borderWidth;
        mBorderPaint = new Paint();
        mBorderPaint.setDither(true);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(borderColor);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mBorderWidth);
    }

    @NonNull
    @Override
    public Resource<Bitmap> transform(@NonNull Context context, @NonNull Resource<Bitmap> resource, int outWidth, int outHeight) {

        Bitmap source = resource.get();
        int finalWidth, finalHeight;
        //输出目标的宽高或高宽比例
        float scale;
        if (outWidth > outHeight) {
            //如果 输出宽度 > 输出高度 求高宽比

            scale = (float) outHeight / (float) outWidth;
            finalWidth = source.getWidth();
            //固定原图宽度,求最终高度
            finalHeight = (int) ((float) source.getWidth() * scale);
            if (finalHeight > source.getHeight()) {
                //如果 求出的最终高度 > 原图高度 求宽高比

                scale = (float) outWidth / (float) outHeight;
                finalHeight = source.getHeight();
                //固定原图高度,求最终宽度
                finalWidth = (int) ((float) source.getHeight() * scale);
            }
        } else if (outWidth < outHeight) {
            //如果 输出宽度 < 输出高度 求宽高比

            scale = (float) outWidth / (float) outHeight;
            finalHeight = source.getHeight();
            //固定原图高度,求最终宽度
            finalWidth = (int) ((float) source.getHeight() * scale);
            if (finalWidth > source.getWidth()) {
                //如果 求出的最终宽度 > 原图宽度 求高宽比

                scale = (float) outHeight / (float) outWidth;
                finalWidth = source.getWidth();
                finalHeight = (int) ((float) source.getWidth() * scale);
            }
        } else {
            //如果 输出宽度=输出高度
            finalHeight = source.getHeight();
            finalWidth = finalHeight;
        }


        int size = Math.min(source.getWidth(), source.getHeight()) - (mBorderWidth / 2);
        int imageWidth = finalWidth - 2*mBorderWidth;
        int imageHeight = finalHeight -2* mBorderWidth;

        //修正圆角
        this.radius *= (float) finalHeight / (float) outHeight;
        Bitmap outBitmap = this.mBitmapPool.get(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
        if (outBitmap == null) {
            outBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(outBitmap);
        Paint paint = new Paint();
        //关联画笔绘制的原图bitmap
        BitmapShader shader = new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        //计算中心位置,进行偏移
        int width = (source.getWidth() - imageWidth) / 2;
        int height = (source.getHeight() - imageHeight) / 2;
        if (width != 0 || height != 0) {
            Matrix matrix = new Matrix();
            matrix.setTranslate((float) (-width), (float) (-height));
            shader.setLocalMatrix(matrix);
        }

        paint.setShader(shader);
        paint.setAntiAlias(true);
        RectF rectF = new RectF(mBorderWidth, mBorderWidth, (float) canvas.getWidth() - mBorderWidth/2, (float) canvas.getHeight() -  mBorderWidth/2);
        //先绘制圆角矩形
        canvas.drawRoundRect(rectF, this.radius, this.radius, paint);


        RectF insideRectF = new RectF(mBorderWidth/ 2, mBorderWidth/ 2, (float) canvas.getWidth()-mBorderWidth/ 2, (float) canvas.getHeight()-mBorderWidth/ 2);

        canvas.drawRoundRect(insideRectF, this.radius, this.radius, mBorderPaint);


        return BitmapResource.obtain(outBitmap, this.mBitmapPool);
    }


    @Override
    public boolean equals(Object obj) {
        return obj instanceof RoundedCornersWithBorderTransform;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
    }
}

