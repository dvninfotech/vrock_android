package com.vrockk.models.coins.in_app_purchase

data class NameValuePairs(
    val description: String,
    val price: String,
    val price_amount_micros: Int,
    val price_currency_code: String,
    val productId: String,
    val skuDetailsToken: String,
    val title: String,
    val type: String
)