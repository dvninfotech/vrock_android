package com.vrockk.models.profile.following_list

data class FollowingResponse(
    var `data`: ArrayList<Data> = ArrayList(),
    var message: String = "",
    var success: Boolean = false,
    var total: Int = 0
)