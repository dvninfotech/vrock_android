package com.vrockk.models.coins.get_all_coins

data class Data(
    val __v: Int,
    val _id: String,
    val coins: Int,
    val createdAt: String,
    val image: String,
    val isDeleted: Boolean,
    val name: String,
    val s3GiftKey: String,
    val status: Int,
    val updatedAt: String
)