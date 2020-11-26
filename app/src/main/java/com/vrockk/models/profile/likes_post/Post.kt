package com.vrockk.models.profile.likes_post

data class Post(
    var __v: Int = 0,
    var _id: String = "",
    var createdAt: String = "",
    var description: String = "",
    var hashtags: List<String> = listOf(),
    var isDeleted: Boolean = false,
    var originalName: String = "",
    var post: String = "",
    var size: Int = 0,
    var status: Int = 0,
    var type: String = "",
    var updatedAt: String = "",
    var userId: String = "",
    var views: Int = 0
)