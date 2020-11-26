package com.vrockk.models.upload_post.gethashtags

data class GetHashTagsResponse(
    val `data`: ArrayList<Data>,
    val message: String,
    val success: Boolean,
    val total: Int
)