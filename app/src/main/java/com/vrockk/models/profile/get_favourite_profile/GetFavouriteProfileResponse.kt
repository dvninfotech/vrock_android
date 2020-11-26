package com.vrockk.models.profile.get_favourite_profile

data class GetFavouriteProfileResponse(
    var `data`: List<Data> = listOf(),
    var message: String = "",
    var success: Boolean = false,
    var total: Int = 0
)