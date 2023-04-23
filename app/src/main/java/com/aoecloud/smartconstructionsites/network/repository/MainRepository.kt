package com.aoecloud.smartconstructionsites.network.repository

import android.text.TextUtils
import android.util.Log
import com.aoecloud.smartconstructionsites.network.Network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


class MainRepository(private val network: Network) {


    suspend fun login(account: String, passWord: String) = requestLogin(account, passWord)
    suspend fun bind( id: String) = requestBind(id)
    suspend fun getProjectData( id: String) = requestProjectData(id)

    private suspend fun requestLogin(account: String, passWord: String) =
        withContext(Dispatchers.IO) {
            val params = hashMapOf<String, Any>(
                "username" to account,
                "password" to passWord,
                "type" to "admin",
                "captcha" to "rxbs",
                "key" to System.currentTimeMillis()
            )
            val data = async { network.mainPageService.login(params) }
            val await = data.await()
            await
        }
    private suspend fun requestBind(id: String) =
        withContext(Dispatchers.IO) {
            val params = hashMapOf<String, Any>(
                "bind_id" to id,
            )
            val data = async { network.mainPageService.getAccountInfo(params) }
            val await = data.await()
            await
        }
    private suspend fun requestProjectData(id: String) =
        withContext(Dispatchers.IO) {
            val params = hashMapOf<String, Any>(
                "project_id" to id,
            )
            val data = async { network.mainPageService.getModuleData(params) }
            val await = data.await()
            await
        }
    companion object {

        @Volatile
        private var INSTANCE: MainRepository? = null

        fun getInstance(network: Network): MainRepository = INSTANCE ?: synchronized(this) {
            INSTANCE ?: MainRepository(network)
        }
    }

}