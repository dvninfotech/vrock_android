package com.vrockk.api.parsers

import com.google.gson.JsonElement
import com.vrockk.VrockkApplication
import com.vrockk.api.MESSAGE
import com.vrockk.api.SUCCESS
import com.vrockk.models.comments.get_comments.User
import com.vrockk.models.home.home_page.Data
import com.vrockk.models.home.home_page.HomeResponse
import com.vrockk.models.home.home_page.UserId
import com.vrockk.models.songs_list.DataItem
import com.vrockk.models.songs_list.Description
import com.vrockk.utils.Variables
import org.json.JSONArray
import org.json.JSONObject

class HomeParser {
    companion object {
        private fun parseUser(jsonObject: JSONObject) : User {
            return User(
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

        private fun parseUserId(jsonObject: JSONObject) : UserId {
            return UserId(
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

        private fun parseSongDescription(jsonObject: JSONObject) : Description {
            return Description(
                jsonObject.optString("artist"),
                jsonObject.optString("name")
            )
        }

        private fun parseSong(jsonObject: JSONObject) : DataItem {
            var descObject = jsonObject.optJSONObject("description")
            if (descObject == null)
                descObject = JSONObject()

            return DataItem(
                jsonObject.optString("originalName"),
                jsonObject.optString("song"),
                jsonObject.optString("createdAt"),
                jsonObject.optBoolean("isDeleted"),
                jsonObject.optInt("size"),
                jsonObject.optInt("__v"),
                parseSongDescription(descObject),
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

        private fun parseElement(jsonObject: JSONObject?) : Data {
            var hashTagsArray = jsonObject!!.optJSONArray("countryCode")
            if (hashTagsArray == null)
                hashTagsArray = JSONArray()

            var userIdObject = jsonObject.optJSONObject("userId")
            if (userIdObject == null)
                userIdObject = JSONObject()

            var songObject = jsonObject.optJSONObject("song")
            if (songObject == null)
                songObject = JSONObject()

            return Data(
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
                parseUserId(userIdObject),
                parseSong(songObject),
                jsonObject.optInt("views"),
                jsonObject.optBoolean("isLiked"),
                jsonObject.optBoolean("isFollowing")
            )
        }

        fun parseResponse(jsonElement: JsonElement) : HomeResponse? {
            try {
                val stringJsonElement = VrockkApplication.gson?.toJson(jsonElement)
                if (stringJsonElement != null) {
                    val responseObject = JSONObject(stringJsonElement)
                    val homeResponse = HomeResponse()

                    homeResponse.success = responseObject.optBoolean(SUCCESS)
                    homeResponse.message = responseObject.optString(MESSAGE)
                    homeResponse.total = responseObject.optInt("total")

                    var userObject = responseObject.optJSONObject("user")
                    if (userObject == null)
                        userObject = JSONObject()
                    homeResponse.user = parseUser(userObject)

                    val feedsArray = responseObject.optJSONArray("data")
                    val feeds = arrayListOf<Data>()
                    for (i in 0 until feedsArray!!.length()) {
                        feeds.add(parseElement(feedsArray.optJSONObject(i)))
                    }

                    homeResponse.data = feeds
                    Variables.HOME_PAGE_RADIX_POST_KEY = responseObject.optString("postKey", "")
                    return homeResponse
                }
            } catch (e : Exception) {
                e.printStackTrace()
            }
            return null
        }
    }
}