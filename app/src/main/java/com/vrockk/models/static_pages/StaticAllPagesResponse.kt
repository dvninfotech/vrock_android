package com.vrockk.models.static_pages

data class StaticAllPagesResponse(
    val `data`: List<Data>,
    val message: String,
    val success: Boolean,
    val total: Int
)