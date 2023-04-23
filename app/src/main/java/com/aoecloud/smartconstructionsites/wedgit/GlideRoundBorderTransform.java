package com.aoecloud.smartconstructionsites.wedgit;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

/**
 * <p>
 * Created by android_ls on 2020/4/2 11:23 AM.
 *
 * @author android_ls
 * @version 1.0
 */
public class GlideRoundBorderTransform extends BitmapTransformation {

    private final String ID = getClass().getName();
    private Paint mBorderPaint;
    private float borderWidth;
    private int borderColor;
    private float radius;

    public GlideRoundBorderTransform(float borderWidth, float radius, int borderColor) {
        this.borderWidth = borderWidth;
        this.borderColor = borderColor;
        this.radius = radius;
        mBorderPaint = new Paint();
        mBorderPaint.setColor(borderColor);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setStrokeWidth(borderWidth);
        mBorderPaint.setDither(true);

    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool bitmapPool, @NonNull Bitmap bitmap, int outWidth, int outHeight) {
        return circleCrop(bitmapPool, bitmap);
    }

    private Bitmap circleCrop(BitmapPool bitmapPool, Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;
        Bitmap square = Bitmap.createBitmap(source, x, y, size, size);
        Bitmap result = bitmapPool.get(size, size, Bitmap.Config.ARGB_8888);
        if (result == null) {
            result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        }
        //画图
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        //设置 Shader
        paint.setShader(new BitmapShader(square, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        paint.setAntiAlias(true);
        canvas.drawRoundRect(new RectF(0f, 0f, size, size), radius, radius, paint);
        /************************描边*********************/
        //画边框
        canvas.drawRoundRect(new RectF(0f, 0f, size, size), radius, radius, mBorderPaint);
        return result;
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(ID.getBytes(CHARSET));
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof GlideRoundBorderTransform;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }

}
