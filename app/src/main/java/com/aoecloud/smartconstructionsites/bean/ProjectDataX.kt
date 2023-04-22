package com.aoecloud.smartconstructionsites.bean

data class ProjectDataX(
    val id: Int,
    val image: String,
    val modularData: List<ModularData>,
    val project_name: String
)