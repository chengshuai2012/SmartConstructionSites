package com.aoecloud.smartconstructionsites.network


import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


class ChangeUrlInterceptor: Interceptor{
    @RequiresApi(Build.VERSION_CODES.O)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()
        val oldHttpUrl = originalRequest.url()
        val tag = originalRequest.header("URLTAG")
        if (TextUtils.isEmpty(tag)){
            return chain.proceed(originalRequest)
        }else{
            builder.removeHeader("URLTAG")
            builder.addHeader("X-Ca-Key","204048858")
            builder.addHeader("X-CaTimestamp",System.currentTimeMillis().toString())
            builder.addHeader("X-CaSignatureHeaders","040488581")
            try {
                var newURL: HttpUrl? =  HttpUrl.parse("https://open.ys7.com")
                newURL?.run {
                    var new = oldHttpUrl.newBuilder().scheme(scheme()).host(host()).port(port()).build()
                    return  chain.proceed(builder.url(new).build())
                }
            } catch (e: Exception) {

                return chain.proceed(originalRequest)
            }
        }

        return chain.proceed(originalRequest)
    }

}