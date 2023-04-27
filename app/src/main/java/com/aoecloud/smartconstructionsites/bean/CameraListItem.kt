package com.aoecloud.smartconstructionsites.bean

data class CameraListItem(
    val channelId: String,
    val deviceSerial: String,
    val device_id: Int,
    val sn_id: String,
    val device_name: String,
    val device_type_ico: String,
    val hls: String,
    val hlsHd: String,
    val id: Int,
    val rtmp: String,
    val rtmpHd: String,
    val online: Int,
    val channelNo: Int,
    val state: Int,
    val type: String
)