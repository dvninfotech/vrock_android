package com.vrockk.models.coins.order_complete

data class FullOrderResponse(
    val `data`: Data,
    val message: String,
    val success: Boolean
)