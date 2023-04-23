package com.aoecloud.smartconstructionsites.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import com.aoecloud.smartconstructionsites.base.BaseApplication
import java.math.RoundingMode
import java.text.DecimalFormat

object DimensionUtils {

    /**
     * px转sp
     */
    fun px2sp(pxVal: Float): Float {
        return pxVal / BaseApplication.context.resources.displayMetrics.scaledDensity
    }
    /**
     * px转sp
     */
    fun sp2px(spVal: Float): Float {
        return spVal * BaseApplication.context.resources.displayMetrics.scaledDensity
    }

    fun getWindowWidth(activity: Activity): Int {
        return activity.resources.displayMetrics.widthPixels
    }

    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    fun pxToDp(px: Int): Int {
        return (px / Resources.getSystem().displayMetrics.density).toInt()
    }

    fun getStatusBarHeight(context: Context): Int {
        var statusBarHeight = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = context.resources.getDimensionPixelSize(resourceId)
        }
        return statusBarHeight
    }

    /**
     * 仅保留1位有效小数
     * @param num
     * @return
     */
    fun transferNum(num: Long): String {
        var str = num.toString()
        if (num > 10000) {
            val df = DecimalFormat("#.0")
            df.roundingMode = RoundingMode.HALF_UP
            str = df.format((num / 10000f).toDouble()) + "万"
        }
        return str
    }

    /**
     * @param num
     * @return
     */
    fun transferNum(num: Long, pattern: String?): String {
        var str = num.toString()
        if (num > 10000) {
            val df = DecimalFormat(pattern)
            df.roundingMode = RoundingMode.HALF_UP
            str = df.format((num / 10000f).toDouble()) + "万"
        }
        return str
    }
}