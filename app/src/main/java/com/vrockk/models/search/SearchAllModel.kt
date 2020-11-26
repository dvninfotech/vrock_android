package com.vrockk.models.search

data class SearchAllModel(
    var success: Boolean = false,
    var message: String = "",
    var hashTagsDataModels: ArrayList<SearchHashTagDataModel> = ArrayList(),
    var postsDataModels: ArrayList<SearchPostDataModel> = ArrayList(),
    var songsDataModels: ArrayList<SearchSongDataModel> = ArrayList(),
    var userDataModels: ArrayList<SearchUserDataModel> = ArrayList(),
    var total: Int = 0
)