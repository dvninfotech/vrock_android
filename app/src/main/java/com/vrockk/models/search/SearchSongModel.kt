package com.vrockk.models.search

data class SearchSongModel(
var success: Boolean = false,
var message: String = "",
var dataModels: ArrayList<SearchSongDataModel> = ArrayList(),
var total: Int = 0
)

data class SearchSongDataModel(
    var _id: String = "",
    var originalName: String = "",
    var song: String = "",
    var description: String = "",
    var uploadedBy: String = ""
)