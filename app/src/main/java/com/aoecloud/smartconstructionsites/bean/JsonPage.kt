package com.aoecloud.smartconstructionsites.bean

data class JsonPage(
    val children: List<Children>,
    val icon: String,
    val id: Int,
    val path: String,
    val pid: Int,
    val title: String
)