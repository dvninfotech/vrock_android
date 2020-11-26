package com.vrockk.models.like.like_post

data class LIkeResponse(
    var `data`: Data = Data(),
    var message: String = "",
    var success: Boolean = false
)