
package com.aoecloud.smartconstructionsites.network

import com.aoecloud.smartconstructionsites.bean.AccountBean
import com.aoecloud.smartconstructionsites.bean.LoginData
import com.aoecloud.smartconstructionsites.bean.ProjectData
import retrofit2.http.*
import kotlin.collections.HashMap


interface MainPageService {
    /**
     * 登录
     */
    @POST("admin/login")
    suspend fun login(@Body map: HashMap<String, Any>): LoginData

    /**
     * 获取用户信息
     */
    @POST("/api-system/wx/getAccountInfo")
    suspend fun getAccountInfo(@Body map: HashMap<String, Any>): AccountBean

    /**
     * 获取项目信息
     */
    @POST("/api-system/wxModule/getModuleData")
    suspend fun getModuleData(@Body map: HashMap<String, Any>): ProjectData
}