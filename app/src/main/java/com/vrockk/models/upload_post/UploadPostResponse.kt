package com.vrockk.models.upload_post

import com.google.gson.annotations.SerializedName

data class UploadPostResponse(

	@field:SerializedName("data")
	val data: Data? = null,

	@field:SerializedName("success")
	val success: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class Data(

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

	@field:SerializedName("__v")
	val V: Int? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("_id")
	val id: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("userId")
	val userId: String? = null,

	@field:SerializedName("status")
	val status: Int? = null,

	@field:SerializedName("updatedAt")
	val updatedAt: String? = null
)
