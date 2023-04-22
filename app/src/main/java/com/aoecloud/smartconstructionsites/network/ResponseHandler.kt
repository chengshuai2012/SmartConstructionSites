

package com.aoecloud.smartconstructionsites.network


import com.aoecloud.smartconstructionsites.R
import com.aoecloud.smartconstructionsites.utils.GlobalUtil
import com.google.gson.JsonSyntaxException

import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


object ResponseHandler {

    /**
     * 当网络请求没有正常响应的时候，根据异常类型进行相应的处理。
     * @param e 异常实体类
     */
    fun getFailureTips(e: Throwable?) = when (e) {
        is ConnectException -> GlobalUtil.getString(R.string.network_connect_error)
        is SocketTimeoutException -> GlobalUtil.getString(R.string.network_connect_timeout)
        is ResponseCodeException -> e.mes
        is ResponseNullException -> ""
        is NoRouteToHostException -> GlobalUtil.getString(R.string.no_route_to_host)
        is UnknownHostException -> GlobalUtil.getString(R.string.network_error)
        is JsonSyntaxException -> GlobalUtil.getString(R.string.json_data_error)
        else -> {
            GlobalUtil.getString(R.string.unknown_error)
        }
    }
}