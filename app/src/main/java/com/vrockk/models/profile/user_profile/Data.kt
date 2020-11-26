package com.vrockk.models.profile.user_profile

data class Data(
    var posts: List<Post> = listOf(),
    var profile: List<Profile> = listOf(),
    var total: Int = 0
)