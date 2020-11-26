package com.vrockk.models.home.home_page

import com.vrockk.models.songs_list.DataItem

data class Data(
    var __v: Int = 0,
    var _id: String = "",
    var createdAt: String = "",
    var description: String = "",
    var hashtags: ArrayList<String> = ArrayList(),
    var isDeleted: Boolean = false,
    var originalName: String = "",
    var post: String = "",
    var thumbnail: String = "",
    var size: Int = 0,
    var status: Int = 0,
    var totalComments: Int = 0,
    var totalLikes: Int = 0,
    var type: String = "",
    var updatedAt: String = "",
    var userId: UserId = UserId(),
    var song: DataItem ? = null,
    var views: Int = 0,
    var isLiked:Boolean = false,
    var isFollowing:Boolean = false
)