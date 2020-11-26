package com.vrockk.models.notification

data class NotificationResponse(
    val `data`: ArrayList<Data>,
    val message: String,
    val success: Boolean,
    val total: Int
)