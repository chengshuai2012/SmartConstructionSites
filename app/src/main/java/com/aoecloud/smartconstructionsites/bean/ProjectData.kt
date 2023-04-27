package com.aoecloud.smartconstructionsites.bean

data class ProjectData(
    val attendance_count: Int,
    val camera_count: Int,
    val car_count: Int,
    val device_count: Int,
    val knowledge_count: Int,
    val floor: String,
    val temperature: Double,
    val loor_area: Double,
    val total_power_consumption: Double,
    val project_address: String,
    val complete_date:String,
    val project_name: String,
    val user_count: Int,
    val violation_count: Int
)