package com.vrockk.models.notification

data class Data(
    val __v: Int,
    val _id: String,
    val date: Long,
    val message: String,
    val notiData: NotiData,
    val notiType: Int,
    val user: User? = null
)