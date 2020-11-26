package com.vrockk.models.coins.all_settings

data class Data(
    val _id: String,
    val appStoreFee: Int,
    val buyingRate: BuyingRate,
    val createdAt: String,
    val googlePlayFee: Int,
    val platformFee: Int,
    val redeemCoinPrice: Int,
    val redeemPercentage: Int,
    val redeemSlab: Int,
    val sellingRate: SellingRate,
    val updatedAt: String
)