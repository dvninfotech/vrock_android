package com.vrockk.models.login

data class Data(
    var __v: Int = 0,
    var _id: String = "",
    var authToken: String = "",
    var bio: String = "",
    var countryCode: String = "",
    var createdAt: String = "",
    var dob: String = "",
    var email: String = "",
    var firstName: String = "",
    var id: String = "",
    var instagram: String = "",
    var isEmailVerified: Boolean = false,
    var isPhoneVerified: Boolean = false,
    var lastName: String = "",
    var phone: String = "",
    var profilePic: String = "",
    var profileStatus: Int = 0,

    var provider: String = "",
    var providerId: String = "",
    var referralCode: String = "",
    var updatedAt: String = "",
    var userName: String = "",
    var loginType: String = "",
    var address: String = "",
    var gender: String = "",
    var level: Int = 0,
    var facebook: String = "",
    var youtube: String = ""
)