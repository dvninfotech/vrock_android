package com.vrockk.models.comments.get_comments

data class Data(
    var __v: Int = 0,
    var _id: String = "",
    var comment: String = "",
    var createdAt: String = "",
    var post: Post = Post(),
    var updatedAt: String = "",
    var user: User = User()
)