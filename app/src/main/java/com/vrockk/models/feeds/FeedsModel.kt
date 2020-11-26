package com.vrockk.models.feeds

import com.vrockk.models.users.UserModel

data class FeedsModel(
    var dataModels: ArrayList<FeedDataModel> = ArrayList(),
    var message: String = "",
    var success: Boolean = false,
    var total: Int = 0,
    var userModel: UserModel = UserModel()
)