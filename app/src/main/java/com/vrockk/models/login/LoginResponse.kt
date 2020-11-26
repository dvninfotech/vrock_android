package com.vrockk.models.login

data class LoginResponse(
    var `data`: Data = Data(),
    var message: String = "",
    var success: Boolean = false
)