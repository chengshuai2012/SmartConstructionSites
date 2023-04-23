package com.aoecloud.smartconstructionsites.bean

data class ProjectDataX(
    val id: String,
    val image: String,
    val insert_time: String,
    val modularData: List<ModularData>,
    val project_name: String
)