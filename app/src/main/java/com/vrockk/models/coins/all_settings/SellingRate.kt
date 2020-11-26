package com.vrockk.models.coins.all_settings

data class SellingRate(
    val currency: String,
    val minimumCoins: Int,
    val price: Double
)