
package com.aoecloud.smartconstructionsites.network

import com.aoecloud.smartconstructionsites.bean.*
import retrofit2.http.*
import kotlin.collections.HashMap


interface MainPageService {
    /**
     * 登录
     */
    @POST("zuul/admin/login")
    suspend fun login(@Body map: HashMap<String, Any>): LoginData

    /**
     * 获取用户信息
     */
    @POST("zuul/api-system/wx/getAccountInfo")
    suspend fun getAccountInfo(@Body map: HashMap<String, Any>): AccountBean

    /**
     * 获取项目信息
     */
    @POST("zuul/api-system/wxModule/getModuleData")
    suspend fun getModuleData(@Body map: HashMap<String, Any>): ProjectData

    /**
     * 获取摄像头信息
     */
    @POST("zuul/api-device/camera/findList")
    suspend fun findList(@Body map: HashMap<String, Any>): ArrayList<CameraListItem>

    /**
     * 获取萤石云信息
     */
    @Headers("URLTAG:YINGSHI")
    @FormUrlEncoded
    @POST("api/lapp/token/get")
    suspend fun getToken(@Field("appKey") appKey:String,@Field("appSecret")appSecret:String): TokenBean
}