package com.vrockk.models.coins.order_complete

data class CompleteOrderRequest(
    val amount: String,
    val coins: Int?,
    val packageId: String,
    val paymentObject: PaymentObject,
    val transactionId: String,
    val coinType: String,
    val currency: String
)