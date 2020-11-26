package com.vrockk.models.login

data class SocailLoginRequest(
    var deviceId: String = "",
    var deviceType: String = "",
    var email: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var provider: String = "",
    var providerId: String = ""
)