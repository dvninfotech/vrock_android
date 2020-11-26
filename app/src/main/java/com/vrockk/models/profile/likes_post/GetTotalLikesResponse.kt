package com.vrockk.models.profile.likes_post

data class GetTotalLikesResponse(
    var `data`: ArrayList<Data> = ArrayList(),
    var message: String = "",
    var success: Boolean = false,
    var total: Int = 0
)