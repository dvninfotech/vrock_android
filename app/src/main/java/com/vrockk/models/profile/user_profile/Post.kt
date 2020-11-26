package com.vrockk.models.profile.user_profile

data class Post(
    var __v: Int = 0,
    var _id: String = "",
    var createdAt: String = "",
    var description: String = "",
    var hashtags: List<Any> = listOf(),
    var isDeleted: Boolean = false,
    var originalName: String = "",
    var post: String = "",
    var size: Int = 0,
    var status: Int = 0,
    var totalComments: Int = 0,
    var totalLikes: Int = 0,
    var type: String = "",
    var updatedAt: String = "",
    var userId: String = "",
    var views: Int = 0
)