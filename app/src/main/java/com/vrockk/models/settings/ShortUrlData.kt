package com.vrockk.models.settings

data class ShortUrlData (
    var success: Boolean = false,
    var message: String = "",
    var _id: String = "",
    var longUrl: String = "",
    var shortUrl: String = "",
    var shortKey: String = "",
    var createdAt: String = "",
    var updatedAt: String = "",
    var _v: Int = 0
)