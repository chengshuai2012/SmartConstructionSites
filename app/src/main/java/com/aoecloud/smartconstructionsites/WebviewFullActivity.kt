package com.aoecloud.smartconstructionsites

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.webkit.JavascriptInterface
import android.webkit.WebViewClient
import androidx.lifecycle.ViewModelProvider
import com.aoecloud.smartconstructionsites.base.BaseActivity
import com.aoecloud.smartconstructionsites.bean.TokenBean
import com.aoecloud.smartconstructionsites.camera.CameraListActivity
import com.aoecloud.smartconstructionsites.databinding.ActivityWebviewFullBinding
import com.aoecloud.smartconstructionsites.utils.GlobalUtil
import com.aoecloud.smartconstructionsites.utils.InjectorUtil
import com.aoecloud.smartconstructionsites.utils.SpUtils
import com.aoecloud.smartconstructionsites.viewmodel.MainViewModel
import com.google.gson.Gson
import com.videogo.openapi.EZOpenSDK

class WebviewFullActivity: BaseActivity() {
    private var _binding:ActivityWebviewFullBinding ? = null
    private val binding: ActivityWebviewFullBinding
        get() {
            return _binding!!
        }
    private val mainViewModel by lazy {
        ViewModelProvider(
            this,
            InjectorUtil.getMainViewModelFactory()
        )[MainViewModel::class.java]
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityWebviewFullBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 设置出现缩放工具
        binding.webview.settings?.javaScriptEnabled = true
        binding.webview.settings?.domStorageEnabled = true
        binding.webview.settings?.javaScriptCanOpenWindowsAutomatically = true
        binding.webview.webViewClient = WebViewClient()
//            binding.webview.webViewClient = object : WebViewClient() {
//                override fun onReceivedSslError(
//                    view: WebView?,
//                    handler: SslErrorHandler?,
//                    error: SslError?,
//                ) {
//                    handler?.proceed()
//                }
//
//            }
        binding.webview.addJavascriptInterface(JsObject(),"android")
        binding.webview.loadUrl("http://114.115.152.25/works/app/")
        val tokenString = SpUtils.getString(this@WebviewFullActivity, "token")
        if (TextUtils.isEmpty(tokenString)){
            mainViewModel.tokenParam.value=""
        }else{
            val tokenBean = Gson().fromJson(tokenString, TokenBean::class.java)
            if (System.currentTimeMillis()>tokenBean.expireTime){
                mainViewModel.tokenParam.value=""
            }else{
                EZOpenSDK.getInstance().setAccessToken(tokenBean.accessToken)
            }
        }
        mainViewModel.tokenData.observe(this){
            it.onSuccess {
                SpUtils.putString(this@WebviewFullActivity, "token", Gson().toJson(it))
                EZOpenSDK.getInstance().setAccessToken(it.accessToken)
            }
        }
    }

    inner class JsObject(){

        @JavascriptInterface
        fun toCamera(token:String,project_id:String){
            GlobalUtil.projectId = project_id
            GlobalUtil.token = token
            startActivity(Intent(this@WebviewFullActivity, CameraListActivity::class.java))
        }
    }


}