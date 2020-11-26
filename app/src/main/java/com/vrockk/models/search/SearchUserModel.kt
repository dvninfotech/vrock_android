package com.vrockk.models.search

data class SearchUserModel(
var success: Boolean = false,
var message: String = "",
var dataModels: ArrayList<SearchUserDataModel> = ArrayList(),
var total: Int = 0
)

data class SearchUserDataModel(
    val v: Int = 0,
    val id: String = "",
    val userName: String = "",
    val profilePic: String = ""
)