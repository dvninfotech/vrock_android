package com.vrockk.models.search

import com.google.gson.annotations.SerializedName

data class TagSearchModel(

    @field:SerializedName("total")
    val total: Int? = null,

    @field:SerializedName("data")
    val data: List<TagItem>? = null,

    @field:SerializedName("success")
    val success: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null
)

