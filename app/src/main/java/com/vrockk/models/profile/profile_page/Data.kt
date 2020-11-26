package com.vrockk.models.profile.profile_page

data class Data(
    var posts: ArrayList<Post> = ArrayList(),
    var profile: ArrayList<Profile> = ArrayList(),
    var total: Int = 0
)