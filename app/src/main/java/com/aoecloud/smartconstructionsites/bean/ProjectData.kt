package com.aoecloud.smartconstructionsites.bean

data class ProjectData(
    val attendance_count: Int,
    val camera_count: Int,
    val car_count: Int,
    val device_count: Int,
    val knowledge_count: Int,
    val temperature: Double,
    val total_power_consumption: Double,
    val user_count: Int,
    val violation_count: Int
)