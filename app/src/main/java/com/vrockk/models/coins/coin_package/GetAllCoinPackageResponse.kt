package com.vrockk.models.coins.coin_package

data class GetAllCoinPackageResponse(
    val `data`: List<Data>,
    val message: String,
    val success: Boolean,
    val total: Int
)