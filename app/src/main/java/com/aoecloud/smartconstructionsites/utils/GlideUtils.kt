package com.aoecloud.smartconstructionsites.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.aoecloud.smartconstructionsites.base.BaseApplication
import com.aoecloud.smartconstructionsites.wedgit.GlideRoundBorderTransform
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget


object GlideUtils {

    fun loadHead(url: String?, view: ImageView) {
        Glide.with(BaseApplication.context)
            .load(url)
            .circleCrop()
            .into(view)

    }
    fun loadRoundImage(url: String?, radius: Int, view: ImageView) {
        Glide.with(BaseApplication.context)
            .load(url)
            .transform(MultiTransformation(CenterCrop(),
                RoundedCorners(DimensionUtils.dpToPx(radius))))
            .into(view)
    }
    /**
     * 加载头像并设置错误和占位图
     */
    fun loadHead(url: String?, @DrawableRes defRes: Int, view: ImageView) {
        val option = RequestOptions
            .circleCropTransform()
            .error(defRes)
            .placeholder(defRes)
        val transform = Glide.with(BaseApplication.context).load(defRes)
            .apply(RequestOptions().circleCrop())
        Glide.with(BaseApplication.context)
            .load(url)
            .apply(option)
            .thumbnail(transform)
            .into(view)
    }

    fun load(url: String, view: ImageView) {
        Glide.with(BaseApplication.context)
            .load(url)
            .transform(MultiTransformation(CenterCrop()))
            .into(view)
    }
    fun loadWithCorner(
        res: String?,
        view: ImageView,
        radius: Int = 6,
        borderWidth: Int = 0,
        @DrawableRes defRes: Int = -1,
        strokeColor: Int = Color.WHITE
    ) {
        val option = RequestOptions
            .bitmapTransform(
                GlideRoundBorderTransform(
                    DimensionUtils.dpToPx(borderWidth).toFloat(),
                    DimensionUtils.dpToPx(radius).toFloat(),
                    strokeColor
                )
            )
            .error(defRes)
            .placeholder(defRes)
        if (defRes == -1) {
            Glide.with(BaseApplication.context)
                .load(res)
                .apply(option)
                .into(view)
        } else {
            val transform = Glide.with(BaseApplication.context).load(defRes)
                .apply(
                    RequestOptions().transform(
                        GlideRoundBorderTransform(
                            DimensionUtils.dpToPx(borderWidth).toFloat(),
                            DimensionUtils.dpToPx(radius).toFloat(),
                            strokeColor
                        )
                    )
                )
            Glide.with(BaseApplication.context)
                .load(res)
                .apply(option)
                .thumbnail(transform)
                .into(view)
        }

    }

    fun loadNormal(resId: Int, view: ImageView) {
        Glide.with(BaseApplication.context)
            .load(resId)
            .into(view)
    }
    fun loadNormal(url: String, view: ImageView) {
        Glide.with(BaseApplication.context)
            .load(url)
            .into(view)
    }

    fun loadFitCenter(url: String, view: ImageView) {
        Glide.with(BaseApplication.context)
            .load(url)
            .transform(MultiTransformation(FitCenter()))
            .into(view)
    }

    fun loadGif(resId: Int, view: ImageView) {
        Glide.with(BaseApplication.context)
            .asGif()
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .load(resId)
            .into(view)
    }

    /**
     * @param url 获取 通过URL得到 圆角Drawable
     */
    fun getRoundsDrawableGlide(url:String, customTarget:CustomTarget<Drawable>) {
        Glide.with(BaseApplication.context).load(url)
            .circleCrop()
            .into(customTarget)
    }
    fun getDrawableGlide(url:String, customTarget:CustomTarget<Drawable>) {
        Glide.with(BaseApplication.context).load(url)
            .into(customTarget)
    }

}