package com.vrockk.api.parsers

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.vrockk.api.MESSAGE
import com.vrockk.api.SUCCESS
import com.vrockk.models.feeds.*
import com.vrockk.models.users.UserModel
import com.vrockk.utils.Variables
import org.json.JSONArray
import org.json.JSONObject

class FeedsParser {
    companion object {
        private fun parseUserModel(jsonObject: JSONObject) : UserModel {
            return UserModel(
                jsonObject.optInt("__v"),
                jsonObject.optString("_id"),
                jsonObject.optString("authToken"),
                jsonObject.optString("bio"),
                jsonObject.optString("countryCode"),
                jsonObject.optString("createdAt"),
                jsonObject.optString("deviceId"),
                jsonObject.optString("deviceType"),
                jsonObject.optString("dob"),
                jsonObject.optString("email"),
                jsonObject.optString("firstName"),
                jsonObject.optString("instagram"),
                jsonObject.optBoolean("isDeleted"),
                jsonObject.optBoolean("isEmailVerified"),
                jsonObject.optBoolean("isPhoneVerified"),
                jsonObject.optString("lastName"),
                jsonObject.optString("password"),
                jsonObject.optString("phone"),
                jsonObject.optString("profilePic"),
                jsonObject.optInt("profileStatus"),
                jsonObject.optString("provider"),
                jsonObject.optString("providerId"),
                jsonObject.optString("referralCode"),
                jsonObject.optString("roles"),
                jsonObject.optInt("sendNoti"),
                jsonObject.optInt("status"),
                jsonObject.optString("updatedAt"),
                jsonObject.optString("userName"),
                jsonObject.optInt("wallet"),
                jsonObject.optString("facebook"),
                jsonObject.optString("youtube")
            )
        }

        private fun parseFeedDataUserModel(jsonObject: JSONObject) : FeedUserModel {
            return FeedUserModel(
                jsonObject.optInt("__v"),
                jsonObject.optString("_id"),
                jsonObject.optString("authToken"),
                jsonObject.optString("bio"),
                jsonObject.optString("countryCode"),
                jsonObject.optString("createdAt"),
                jsonObject.optString("deviceId"),
                jsonObject.optString("deviceType"),
                jsonObject.optString("email"),
                jsonObject.optString("firstName"),
                jsonObject.optString("instagram"),
                jsonObject.optBoolean("isVerified"),
                jsonObject.optString("lastName"),
                jsonObject.optString("password"),
                jsonObject.optString("phone"),
                jsonObject.optString("profilePic"),
                jsonObject.optString("provider"),
                jsonObject.optString("providerId"),
                jsonObject.optString("referralCode"),
                jsonObject.optInt("sendNoti"),
                jsonObject.optInt("status"),
                jsonObject.optString("updatedAt"),
                jsonObject.optString("userName"),
                jsonObject.optInt("wallet"),
                jsonObject.optString("facebook"),
                jsonObject.optString("youtube")
            )
        }

        private fun parseSongDescriptionModel(jsonObject: JSONObject) : SongDescriptionModel {
            return SongDescriptionModel(
                jsonObject.optString("artist"),
                jsonObject.optString("name")
            )
        }

        private fun parseSongModel(jsonObject: JSONObject) : SongModel {
            var descObject = jsonObject.optJSONObject("description")
            if (descObject == null)
                descObject = JSONObject()

            return SongModel(
                jsonObject.optString("originalName"),
                jsonObject.optString("song"),
                jsonObject.optString("createdAt"),
                jsonObject.optBoolean("isDeleted"),
                jsonObject.optInt("size"),
                jsonObject.optInt("__v"),
                parseSongDescriptionModel(descObject),
                jsonObject.optString("_id"),
                jsonObject.optString("type"),
                jsonObject.optInt("status"),
                jsonObject.optString("uploadedBy"),
                jsonObject.optString("updatedAt")
            )
        }

        private fun parseHashTags(jsonArray : JSONArray) : ArrayList<String> {
            val hashTags = arrayListOf<String>()

            for (i in 0 until jsonArray.length())
                hashTags.add(jsonArray.optString(i))

            return hashTags
        }

        private fun parseFeedDataModel(jsonObject: JSONObject?) : FeedDataModel {
            var hashTagsArray = jsonObject!!.optJSONArray("countryCode")
            if (hashTagsArray == null)
                hashTagsArray = JSONArray()

            var userIdObject = jsonObject.optJSONObject("userId")
            if (userIdObject == null)
                userIdObject = JSONObject()

            var songObject = jsonObject.optJSONObject("song")
            if (songObject == null)
                songObject = JSONObject()

            return FeedDataModel(
                jsonObject.optInt("__v"),
                jsonObject.optString("_id"),
                jsonObject.optString("createdAt"),
                jsonObject.optString("description"),
                parseHashTags(hashTagsArray),
                jsonObject.optBoolean("isDeleted"),
                jsonObject.optString("originalName"),
                jsonObject.optString("post"),
                jsonObject.optString("thumbnail"),
                jsonObject.optInt("size"),
                jsonObject.optInt("status"),
                jsonObject.optInt("totalComments"),
                jsonObject.optInt("totalLikes"),
                jsonObject.optString("type"),
                jsonObject.optString("updatedAt"),
                parseFeedDataUserModel(userIdObject),
                parseSongModel(songObject),
                jsonObject.optInt("views"),
                jsonObject.optBoolean("isLiked"),
                jsonObject.optBoolean("isFollowing")
            )
        }

        fun parseFeeds(jsonElement: JsonElement) : FeedsModel? {
            try {
                val stringJsonElement = Gson().toJson(jsonElement)
                if (stringJsonElement != null) {
                    val responseObject = JSONObject(stringJsonElement)
                    val feedsModel = FeedsModel()

                    feedsModel.success = responseObject.optBoolean(SUCCESS)
                    feedsModel.message = responseObject.optString(MESSAGE)
                    feedsModel.total = responseObject.optInt("total")

                    var userObject = responseObject.optJSONObject("user")
                    if (userObject == null)
                        userObject = JSONObject()
                    feedsModel.userModel = parseUserModel(userObject)

                    val feedsArray = responseObject.optJSONArray("data")
                    val feeds = arrayListOf<FeedDataModel>()
                    for (i in 0 until feedsArray!!.length()) {
                        feeds.add(parseFeedDataModel(feedsArray.optJSONObject(i)))
                    }

                    feedsModel.dataModels = feeds
                    Variables.HOME_PAGE_RADIX_POST_KEY = responseObject.optString("postKey", "")
                    return feedsModel
                }
            } catch (e : Exception) {
                e.printStackTrace()
            }
            return null
        }
    }
}