package com.vrockk.models.upload_song

import com.google.gson.annotations.SerializedName

data class UploadSongModel(

	@field:SerializedName("data")
	val data: Data? = null,

	@field:SerializedName("success")
	val success: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class Data(

	@field:SerializedName("song")
	val song: String? = null,

	@field:SerializedName("description")
	val description: Description? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("userId")
	val userId: String? = null,

	@field:SerializedName("originalName")
	val originalName: String? = null,

	@field:SerializedName("createdAt")
	val createdAt: String? = null,

	@field:SerializedName("isDeleted")
	val isDeleted: Boolean? = null,

	@field:SerializedName("size")
	val size: Int? = null,

	@field:SerializedName("__v")
	val V: Int? = null,

	@field:SerializedName("_id")
	val id: String? = null,

	@field:SerializedName("uploadedBy")
	val uploadedBy: String? = null,

	@field:SerializedName("status")
	val status: Int? = null,

	@field:SerializedName("updatedAt")
	val updatedAt: String? = null
)

data class Description(

	@field:SerializedName("artist")
	val artist: String? = null,

	@field:SerializedName("name")
	val name: String? = null
)
