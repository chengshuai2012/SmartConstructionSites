package com.aoecloud.smartconstructionsites.bean

data class LoginData(
    val id: String,
    val jsonPage: List<JsonPage>,
    val logo: String,
    val name: String,
    val projectData: List<ProjectDataX>,
    val time: String,
    val token: String,
    val tokenOverTime: String,
    val type: Int,
    val username: String
)