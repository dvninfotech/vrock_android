package com.vrockk.models.search

data class SearchPostModel(
var success: Boolean = false,
var message: String = "",
var dataModels: ArrayList<SearchPostDataModel> = ArrayList(),
var total: Int = 0
)

data class SearchPostDataModel(
    var __v: Int = 0,
    var _id: String = "",
    var originalName: String = "",
    var post: String = "",
    var thumbnail:String = ""
)