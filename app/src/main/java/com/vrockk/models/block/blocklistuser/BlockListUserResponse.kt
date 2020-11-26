package com.vrockk.models.block.blocklistuser

data class BlockListUserResponse(
    var `data`: ArrayList<Data> = ArrayList(),
    var message: String = "",
    var success: Boolean = false,
    var total: Int = 0
)