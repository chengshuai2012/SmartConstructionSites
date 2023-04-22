package com.aoecloud.smartconstructionsites.wedgit;

import static com.youth.banner.util.BannerUtils.dp2px;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;

import com.youth.banner.indicator.BaseIndicator;

/**
 * 圆形指示器
 * 如果想要大小一样，可以将选中和默认设置成同样大小
 */
public class BannerCircleIndicator extends BaseIndicator {
    private int mNormalRadius;
    private int mSelectedRadius;
    private int maxRadius;

    int configSelectedWidth = dp2px(6f);
    int configSpace = dp2px(4f);

    public BannerCircleIndicator(Context context) {
        this(context, null);
    }

    public BannerCircleIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BannerCircleIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        config= new IndicatorConfig();

        config.setNormalWidth(configSelectedWidth);
        config.setSelectedWidth(configSelectedWidth);
        config.setIndicatorSpace(configSpace);
        mNormalRadius = config.getNormalWidth() / 2;
        mSelectedRadius = config.getSelectedWidth() / 2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        config.setNormalWidth(configSelectedWidth);
        config.setSelectedWidth(configSelectedWidth);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int count = config.getIndicatorSize();
        if (count <= 1) {
            return;
        }

        mNormalRadius = config.getNormalWidth() / 2;
        mSelectedRadius = config.getSelectedWidth() / 2;
        //考虑当 选中和默认 的大小不一样的情况
        maxRadius = Math.max(mSelectedRadius, mNormalRadius);
        //间距*（总数-1）+选中宽度+默认宽度*（总数-1）
        int width = (count - 1) * config.getIndicatorSpace() + config.getSelectedWidth() + config.getNormalWidth() * (count - 1)+dp2px(14f);
        setMeasuredDimension(width, Math.max(config.getNormalWidth(), config.getSelectedWidth()));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        config.setNormalWidth(configSelectedWidth);
        config.setSelectedWidth(configSelectedWidth);
        super.onDraw(canvas);
        int count = config.getIndicatorSize();
        if (count <= 1) {
            return;
        }
        float left = 0;
        for (int i = 0; i < count; i++) {
            mPaint.setColor(config.getCurrentPosition() == i ? Color.parseColor("#FFFFFF"):Color.parseColor("#80FFFFFF"));
            int indicatorWidth = config.getCurrentPosition() == i ? dp2px(14f) : config.getNormalWidth();
            int radius = config.getCurrentPosition() == i ? mSelectedRadius : mNormalRadius;
            if (config.getCurrentPosition() == i){
                canvas.drawRoundRect (left , 0, left+ dp2px(14f), 0+radius*2, radius, radius, mPaint);
            }else{
                canvas.drawCircle(left + radius, maxRadius, radius, mPaint);
            }

            left += indicatorWidth + config.getIndicatorSpace();
        }
//        mPaint.setColor(config.getNormalColor());
//        for (int i = 0; i < count; i++) {
//            canvas.drawCircle(left + maxRadius, maxRadius, mNormalRadius, mPaint);
//            left += config.getNormalWidth() + config.getIndicatorSpace();
//        }
//        mPaint.setColor(config.getSelectedColor());
//        left = maxRadius + (config.getNormalWidth() + config.getIndicatorSpace()) * config.getCurrentPosition();
//        canvas.drawCircle(left, maxRadius, mSelectedRadius, mPaint);
    }

}
