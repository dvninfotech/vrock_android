package com.vrockk.models.coins.get_all_coins

data class GetAllCoinsResponse(
    val `data`: List<Data>,
    val message: String,
    val purchasedCoins: Int,
    val success: Boolean,
    val total: Int
)