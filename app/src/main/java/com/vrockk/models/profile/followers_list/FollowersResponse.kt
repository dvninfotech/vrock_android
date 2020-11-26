package com.vrockk.models.profile.followers_list

data class FollowersResponse(
    var `data`: ArrayList<Data> = ArrayList(),
    var message: String = "",
    var success: Boolean = false,
    var total: Int = 0
)