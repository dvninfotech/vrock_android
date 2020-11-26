package com.vrockk.models.report

data class ReportResponse(
    val `data`: Data,
    val message: String,
    val success: Boolean
)