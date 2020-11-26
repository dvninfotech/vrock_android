package com.vrockk.models.notification

data class NotiData(
    val __v: Int,
    val _id: String,
    val comment: String,
    val createdAt: String,
    val following: String,
    val isFollowed: Boolean,
    val isLiked: Boolean,
    val post: String,
    val updatedAt: String,
    val user: String
)