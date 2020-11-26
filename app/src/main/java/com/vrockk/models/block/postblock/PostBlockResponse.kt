package com.vrockk.models.block.postblock

data class PostBlockResponse(
    var `data`: Data = Data(),
    var message: String = "",
    var success: Boolean = false
)