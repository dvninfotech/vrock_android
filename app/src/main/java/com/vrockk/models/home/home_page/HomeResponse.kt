package com.vrockk.models.home.home_page

import com.vrockk.models.comments.get_comments.User

data class HomeResponse(
    var `data`: ArrayList<Data> = ArrayList(),
    var message: String = "",
    var success: Boolean = false,
    var total: Int = 0,
    var user: User = User()
)