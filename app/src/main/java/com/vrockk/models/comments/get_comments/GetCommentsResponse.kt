package com.vrockk.models.comments.get_comments

data class GetCommentsResponse(
    var `data`: ArrayList<Data> = ArrayList(),
    var message: String = "",
    var success: Boolean = false,
    var total: Int = 0
)