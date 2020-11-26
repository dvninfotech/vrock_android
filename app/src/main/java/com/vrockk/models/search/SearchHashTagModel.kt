package com.vrockk.models.search

data class SearchHashTagModel(
    var success: Boolean = false,
    var message: String = "",
    var dataModels: ArrayList<SearchHashTagDataModel> = ArrayList(),
    var total: Int = 0
)

data class SearchHashTagDataModel(
    var _id: String  = "",
    var __v: Int = 0,
    var name: String = ""
)