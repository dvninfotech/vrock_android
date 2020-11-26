package com.vrockk.models.profile.user_profile

data class UserProfileResponse(
    var `data`: Data = Data(),
    var message: String = "",
    var success: Boolean = false
)