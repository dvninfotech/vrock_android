package com.vrockk.models.coins

import com.google.gson.annotations.SerializedName

data class CoinsResponse(

	@field:SerializedName("total")
	val total: Int? = 0,

@field:SerializedName("purchasedCoins")
val purchasedCoins: Int? = 0,

	@field:SerializedName("data")
	val data: List<DataItem>? = null,

	@field:SerializedName("success")
	val success: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class DataItem(

	@field:SerializedName("image")
	val image: String? = null,

	@field:SerializedName("createdAt")
	val createdAt: String? = null,

	@field:SerializedName("isDeleted")
	val isDeleted: Boolean? = null,

	@field:SerializedName("coins")
	val coins: Int = 0,

	@field:SerializedName("__v")
	val V: Int? = null,

	@field:SerializedName("name")
	val name: String = "",

	@field:SerializedName("_id")
	val id: String? = null,

	@field:SerializedName("updatedAt")
	val updatedAt: String? = null
)
