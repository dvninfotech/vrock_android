package com.vrockk.models.profile.profile_page

data class ProfilePageResponse(
    var `data`: Data = Data(),
    var message: String = "",
    var success: Boolean = false
)