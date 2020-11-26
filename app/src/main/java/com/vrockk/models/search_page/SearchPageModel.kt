package com.vrockk.models.search_page

import com.google.gson.annotations.SerializedName

data class SearchPageModel(

	@field:SerializedName("total")
	val total: Int? = null,

	@field:SerializedName("data")
	val data: Data? = null,

	@field:SerializedName("success")
	val success: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class HashtagsItem(

	@field:SerializedName("_id")
	val id: String? = null,

	@field:SerializedName("posts")
	val posts: List<PostsItem?>? = null
)

data class UserId(

	@field:SerializedName("deviceType")
	val deviceType: Any? = null,

	@field:SerializedName("lastName")
	val lastName: String? = null,

	@field:SerializedName("wallet")
	val wallet: Int? = null,

	@field:SerializedName("isVerified")
	val isVerified: Boolean? = null,

	@field:SerializedName("profilePic")
	val profilePic: String? = null,

	@field:SerializedName("authToken")
	val authToken: String? = null,

	@field:SerializedName("roles")
	val roles: String? = null,

	@field:SerializedName("sendNoti")
	val sendNoti: Int? = null,

	@field:SerializedName("bio")
	val bio: String? = null,

	@field:SerializedName("instagram")
	val instagram: String? = null,

	@field:SerializedName("userName")
	val userName: String? = null,

	@field:SerializedName("deviceId")
	val deviceId: Any? = null,

	@field:SerializedName("firstName")
	val firstName: String? = null,

	@field:SerializedName("createdAt")
	val createdAt: String? = null,

	@field:SerializedName("password")
	val password: String? = null,

	@field:SerializedName("isDeleted")
	val isDeleted: Boolean? = null,

	@field:SerializedName("provider")
	val provider: String? = null,

	@field:SerializedName("providerId")
	val providerId: String? = null,

	@field:SerializedName("referralCode")
	val referralCode: String? = null,

	@field:SerializedName("__v")
	val V: Int? = null,

	@field:SerializedName("_id")
	val id: String? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("status")
	val status: Int? = null,

	@field:SerializedName("updatedAt")
	val updatedAt: String? = null
)

data class Location(

	@field:SerializedName("coordinates")
	val coordinates: List<Double?>? = null,

	@field:SerializedName("type")
	val type: String? = null
)

data class Data(

	@field:SerializedName("slider")
	val slider: List<SliderItem>? = null,

	@field:SerializedName("hashtags")
	val hashtags: ArrayList<HashtagsItem>? = null
)

data class PostsItem(

	@field:SerializedName("hashtags")
	val hashtags: List<String?>? = null,

	@field:SerializedName("latitude")
	val latitude: Double? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("userId")
	val userId: UserId? = null,

	@field:SerializedName("originalName")
	val originalName: String? = null,

	@field:SerializedName("createdAt")
	val createdAt: String? = null,

	@field:SerializedName("isDeleted")
	val isDeleted: Boolean? = null,

	@field:SerializedName("size")
	val size: Int? = null,

	@field:SerializedName("post")
	val post: String? = null,

	@field:SerializedName("thumbnail")
	val thumbnail: String? = null,

	@field:SerializedName("__v")
	val V: Int? = null,

	@field:SerializedName("location")
	val location: Location? = null,

	@field:SerializedName("_id")
	val id: String? = null,

	@field:SerializedName("views")
	val views: Int? = null,

	@field:SerializedName("status")
	val status: Int? = null,

	@field:SerializedName("updatedAt")
	val updatedAt: String? = null,

	@field:SerializedName("longitude")
	val longitude: Double? = null
)

data class SliderItem(

	@field:SerializedName("hashtags")
	val hashtags: List<String?>? = null,

	@field:SerializedName("latitude")
	val latitude: Double? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("userId")
	val userId: UserId? = null,

	@field:SerializedName("originalName")
	val originalName: String? = null,

	@field:SerializedName("createdAt")
	val createdAt: String? = null,

	@field:SerializedName("isDeleted")
	val isDeleted: Boolean? = null,

	@field:SerializedName("size")
	val size: Int? = null,

	@field:SerializedName("post")
	val post: String? = null,

	@field:SerializedName("thumbnail")
	val thumbnail: String? = null,

	@field:SerializedName("__v")
	val V: Int? = null,

	@field:SerializedName("location")
	val location: Location? = null,

	@field:SerializedName("_id")
	val id: String? = null,

	@field:SerializedName("views")
	val views: Int? = null,

	@field:SerializedName("status")
	val status: Int? = null,

	@field:SerializedName("updatedAt")
	val updatedAt: String? = null,

	@field:SerializedName("longitude")
	val longitude: Double? = null
)
