package com.vrockk.models.coins.order_complete

data class PaymentObject(
    val purchaseToken: String,
    val developerPayload:String,
    val isAcknowledged:Boolean,
    val isAutoRenewing:Boolean,
    val orderId:String,
    val originalJson:String,
    val packageName:String,
    val purchaseState:Int,
    val purchaseTime:Long,
    val signature:String,
    val sku:String
)