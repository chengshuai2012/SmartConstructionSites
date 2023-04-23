package com.aoecloud.smartconstructionsites.bean

data class CameraListItem(
    val channelId: String,
    val deviceSerial: String,
    val device_id: Int,
    val device_name: String,
    val hls: String,
    val hlsHd: String,
    val id: Int,
    val rtmp: String,
    val rtmpHd: String,
    val state: Int,
    val type: String
)