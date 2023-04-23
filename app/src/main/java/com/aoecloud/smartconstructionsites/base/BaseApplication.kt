
package com.aoecloud.smartconstructionsites.base

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.alibaba.android.arouter.launcher.ARouter
import com.aoecloud.smartconstructionsites.BuildConfig
import com.videogo.openapi.EZOpenSDK


class BaseApplication : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        sInstance = this
        ARouter.init(this)
        if (BuildConfig.DEBUG) {           // 这两行必须写在init之前，否则这些配置在init过程中将无效
            ARouter.openLog()    // 打印日志
            ARouter.openDebug()   // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
        }
        EZOpenSDK.showSDKLog(true);
        /** * 设置是否支持P2P取流,详见api */
        EZOpenSDK.enableP2P(false);

        /** * APP_KEY请替换成自己申请的 */
        EZOpenSDK.initLib(this, "fc3a1f6f7bb94d77b71f55873f08e027");
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        @SuppressLint("StaticFieldLeak")
        private var sInstance: BaseApplication? = null
        fun getInstance(): BaseApplication? {
            return sInstance
        }
    }

}