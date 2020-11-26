package com.vrockk.models.search

import com.google.gson.annotations.SerializedName
import com.vrockk.models.profile.profile_page.Post
import java.io.Serializable

data class SearchModel(
	@field:SerializedName("total")
	val total: Int? = null,

	@field:SerializedName("data")
    var data: Data? = null,

	@field:SerializedName("success")
	val success: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)


data class Data(
	@field:SerializedName("user")
	var user: ArrayList<DataItem> = ArrayList(),

	@field:SerializedName("hashtags")
	var tags: ArrayList<TagItem> = ArrayList(),

	@field:SerializedName("posts")
	var post: ArrayList<Post> = ArrayList(),

	@field:SerializedName("songs")
	var songs: ArrayList<com.vrockk.models.songs_list.DataItem> = ArrayList()
) : Serializable

data class TagItem(
	@field:SerializedName("_id")
	val id: String? = null
) : Serializable

data class DataItem(
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
	val id: String = "",

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("status")
	val status: Int? = null,

	@field:SerializedName("updatedAt")
	val updatedAt: String? = null,

	@field:SerializedName("deviceType")
	val deviceType: Any? = null,

	@field:SerializedName("deviceId")
	val deviceId: Any? = null
) : Serializable
