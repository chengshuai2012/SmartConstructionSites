
package com.aoecloud.smartconstructionsites.network

import android.util.Log
import com.aoecloud.smartconstructionsites.BuildConfig
import com.aoecloud.smartconstructionsites.utils.GlobalUtil
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import okio.BufferedSource
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.nio.charset.Charset

object ServiceCreator {
    val gson =  Gson()
    const val BASE_URL = "http://114.115.218.94:8081/"
    private val UTF8 = Charset.forName("UTF-8")
    val httpClient = OkHttpClient.Builder()
        .addInterceptor(ResponseInterceptor())
        .addInterceptor(HeaderInterceptor())
        .addInterceptor(BasicParamsInterceptor())
        .addInterceptor(ChangeUrlInterceptor())
        .addInterceptor(HttpLoggingInterceptor().apply {
            if (BuildConfig.DEBUG){
                level = HttpLoggingInterceptor.Level.BODY
            }else{
                level = HttpLoggingInterceptor.Level.HEADERS
            }

        })
        .build()


    private val builder = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().registerTypeAdapterFactory(GsonTypeAdapterFactory()).registerTypeAdapterFactory(MyTypeAdapterFactory()).create()))
        .addConverterFactory(ScalarsConverterFactory.create())


    private val retrofit = builder.build()

    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)


    class HeaderInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val request = originalRequest.newBuilder().apply {
                header("userToken", GlobalUtil.token)
            }.build()
            return chain.proceed(request)
        }
    }

    class BasicParamsInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val originalHttpUrl = originalRequest.url()
            val url = originalHttpUrl.newBuilder().apply {

            }.build()
            val request = originalRequest.newBuilder().url(url).build()
            return chain.proceed(request)
        }
    }
    class ResponseInterceptor : Interceptor{

        @Throws(ResponseCodeException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
                .newBuilder() //.addHeader("Connection", "close")
                .build()
            val response = chain.proceed(request)
            val responseBody = response.body()
            // 输出返回结果
            val source: BufferedSource? = responseBody?.source()
            source?.request(Long.MAX_VALUE) // Buffer the entire body.

            val status = response.code()
            if (status!=200){
                throw ResponseCodeException(status,"服务器开小差了")
            }
            val jsonObject = JSONObject(response.body()?.string())

            val code = if ( jsonObject.get("code") is String){
                (jsonObject.get("code") as String).toInt()
            }else{
                jsonObject.get("code") as Int
            }

            val msg = jsonObject.get("msg") as String

            if (code!=0&&code!=200){
                throw ResponseCodeException(code,msg)
            }else{

                if (jsonObject.get("data").toString()=="null"){
                    throw ResponseNullException(code,msg)
                }else{
                    jsonObject.get("data").toString().run{
                        val create = ResponseBody.create(
                            MediaType.get("application/json"),
                            this
                        )
                        Log.d("okhttp: ",this )
                        return  response.newBuilder().body(create).build()
                    }
                }
               return response

            }
        }

    }
}

