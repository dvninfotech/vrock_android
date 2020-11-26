package com.vrockk.models.coins.order_complete

data class Data(
    val __v: Int,
    val _id: String,
    val amount: String,
    val coins: Int,
    val createdAt: String,
    val isDeleted: Boolean,
    val packageId: String,
    val paymentObject: PaymentObject,
    val status: Int,
    val transactionId: String,
    val type: String,
    val updatedAt: String,
    val user: String,
    val currency: String
)