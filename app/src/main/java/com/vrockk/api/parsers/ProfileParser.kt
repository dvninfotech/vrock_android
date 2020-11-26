package com.vrockk.api.parsers

import android.util.Log
import com.google.gson.JsonElement
import com.vrockk.VrockkApplication
import com.vrockk.api.MESSAGE
import com.vrockk.api.SUCCESS
import com.vrockk.models.hashtags.UserId
import com.vrockk.models.profile.profile_page.Data
import com.vrockk.models.profile.profile_page.Post
import com.vrockk.models.profile.profile_page.Profile
import com.vrockk.models.profile.profile_page.ProfilePageResponse
import com.vrockk.models.songs_list.DataItem
import com.vrockk.models.songs_list.Description
import org.json.JSONArray
import org.json.JSONObject

class ProfileParser {
    companion object {
        const val TAG = "ProfileParser"
//        private fun parseUser(jsonObject: JSONObject) : User {
//            return User(
//                jsonObject.optInt("__v"),
//                jsonObject.optString("_id"),
//                jsonObject.optString("authToken"),
//                jsonObject.optString("bio"),
//                jsonObject.optString("countryCode"),
//                jsonObject.optString("createdAt"),
//                jsonObject.optString("deviceId"),
//                jsonObject.optString("deviceType"),
//                jsonObject.optString("dob"),
//                jsonObject.optString("email"),
//                jsonObject.optString("firstName"),
//                jsonObject.optString("instagram"),
//                jsonObject.optBoolean("isDeleted"),
//                jsonObject.optBoolean("isEmailVerified"),
//                jsonObject.optBoolean("isPhoneVerified"),
//                jsonObject.optString("lastName"),
//                jsonObject.optString("password"),
//                jsonObject.optString("phone"),
//                jsonObject.optString("profilePic"),
//                jsonObject.optInt("profileStatus"),
//                jsonObject.optString("provider"),
//                jsonObject.optString("providerId"),
//                jsonObject.optString("referralCode"),
//                jsonObject.optString("roles"),
//                jsonObject.optInt("sendNoti"),
//                jsonObject.optInt("status"),
//                jsonObject.optString("updatedAt"),
//                jsonObject.optString("userName"),
//                jsonObject.optInt("wallet"),
//                jsonObject.optString("facebook"),
//                jsonObject.optString("youtube")
//            )
//        }
//
//        private fun parseUserId(jsonObject: JSONObject) : UserId {
//            return UserId(
//                jsonObject.optInt("__v"),
//                jsonObject.optString("_id"),
//                jsonObject.optString("authToken"),
//                jsonObject.optString("bio"),
//                jsonObject.optString("countryCode"),
//                jsonObject.optString("createdAt"),
//                jsonObject.optString("deviceId"),
//                jsonObject.optString("deviceType"),
//                jsonObject.optString("email"),
//                jsonObject.optString("firstName"),
//                jsonObject.optString("instagram"),
//                jsonObject.optBoolean("isVerified"),
//                jsonObject.optString("lastName"),
//                jsonObject.optString("password"),
//                jsonObject.optString("phone"),
//                jsonObject.optString("profilePic"),
//                jsonObject.optString("provider"),
//                jsonObject.optString("providerId"),
//                jsonObject.optString("referralCode"),
//                jsonObject.optInt("sendNoti"),
//                jsonObject.optInt("status"),
//                jsonObject.optString("updatedAt"),
//                jsonObject.optString("userName"),
//                jsonObject.optInt("wallet"),
//                jsonObject.optString("facebook"),
//                jsonObject.optString("youtube")
//            )
//        }
//
//        private fun parseSongDescription(jsonObject: JSONObject) : Description {
//            return Description(
//                jsonObject.optString("artist"),
//                jsonObject.optString("name")
//            )
//        }
//
//        private fun parseSong(jsonObject: JSONObject) : DataItem {
//            var descObject = jsonObject.optJSONObject("description")
//            if (descObject == null)
//                descObject = JSONObject()
//
//            return DataItem(
//                jsonObject.optString("originalName"),
//                jsonObject.optString("song"),
//                jsonObject.optString("createdAt"),
//                jsonObject.optBoolean("isDeleted"),
//                jsonObject.optInt("size"),
//                jsonObject.optInt("__v"),
//                parseSongDescription(descObject),
//                jsonObject.optString("id"),
//                jsonObject.optString("type"),
//                jsonObject.optInt("status"),
//                jsonObject.optString("uploadedBy"),
//                jsonObject.optString("updatedAt")
//            )
//        }
//
//        private fun parseHashTags(jsonArray : JSONArray) : ArrayList<String> {
//            val hashTags = arrayListOf<String>()
//
//            for (i in 0 until jsonArray.length())
//                hashTags.add(jsonArray.optString(i))
//
//            return hashTags
//        }
//
//        private fun parseElement(jsonObject: JSONObject?) : Data {
//            var hashTagsArray = jsonObject!!.optJSONArray("countryCode")
//            if (hashTagsArray == null)
//                hashTagsArray = JSONArray()
//
//            var userIdObject = jsonObject.optJSONObject("userId")
//            if (userIdObject == null)
//                userIdObject = JSONObject()
//
//            var songObject = jsonObject.optJSONObject("song")
//            if (songObject == null)
//                songObject = JSONObject()
//
//            return Data(
//                jsonObject.optInt("__v"),
//                jsonObject.optString("_id"),
//                jsonObject.optString("createdAt"),
//                jsonObject.optString("description"),
//                parseHashTags(hashTagsArray),
//                jsonObject.optBoolean("isDeleted"),
//                jsonObject.optString("originalName"),
//                jsonObject.optString("post"),
//                jsonObject.optInt("size"),
//                jsonObject.optInt("status"),
//                jsonObject.optInt("totalComments"),
//                jsonObject.optInt("totalLikes"),
//                jsonObject.optString("type"),
//                jsonObject.optString("updatedAt"),
//                parseUserId(userIdObject),
//                parseSong(songObject),
//                jsonObject.optInt("views"),
//                jsonObject.optBoolean("isLiked"),
//                jsonObject.optBoolean("isFollowing")
//            )
//        }

        private fun parseProfile(jsonObject: JSONObject): Profile {
            return Profile(
                jsonObject.optInt("__v"),
                jsonObject.optString("_id"),
                jsonObject.optString("address"),
                jsonObject.optString("authToken"),
                jsonObject.optString("bio"),
                jsonObject.optString("countryCode"),
                jsonObject.optString("createdAt"),
                jsonObject.optString("deviceId"),
                jsonObject.optString("deviceType"),
                jsonObject.optString("dob"),
                jsonObject.optString("email"),
                jsonObject.optString("firstName"),
                jsonObject.optInt("followers"),
                jsonObject.optInt("following"),
                jsonObject.optString("gender"),
                jsonObject.optString("instagram"),
                jsonObject.optBoolean("isDeleted"),
                jsonObject.optBoolean("isEmailVerified"),
                jsonObject.optBoolean("isPhoneVerified"),
                jsonObject.optBoolean("isSocialRegister"),
                jsonObject.optString("lastName"),
                jsonObject.optString("password"),
                jsonObject.optString("phone"),
                jsonObject.optString("profilePic"),
                jsonObject.optInt("profileStatus"),
                jsonObject.optString("provider"),
                jsonObject.optString("providerId"),
                jsonObject.optString("referingCode"),
                jsonObject.optString("referralCode"),
                jsonObject.optString("roles"),
                jsonObject.optInt("sendNoti"),
                jsonObject.optInt("status"),
                jsonObject.optInt("totalLikes"),
                jsonObject.optInt("totalPosts"),
                jsonObject.optString("updatedAt"),
                jsonObject.optString("userName"),
                jsonObject.optInt("wallet"),
                jsonObject.optBoolean("isFollowing"),
                jsonObject.optBoolean("isFavourite"),
                jsonObject.optInt("purchasedCoins"),
                jsonObject.optInt("giftCoins"),
                jsonObject.optInt("remainingCoins"),
                jsonObject.optString("facebook"),
                jsonObject.optString("youtube")
            )
        }

        private fun parseProfiles(jsonArray: JSONArray): ArrayList<Profile> {
            val profiles = arrayListOf<Profile>()

            for (i in 0 until jsonArray.length()) {
                profiles.add(parseProfile(jsonArray.optJSONObject(i)))
            }

            return profiles
        }

        private fun parseUserId(jsonObject: JSONObject) : UserId {
            return UserId(
                jsonObject.optString("deviceType"),
                jsonObject.optString("lastName"),
                jsonObject.optInt("wallet"),
                jsonObject.optBoolean("isVerified"),
                jsonObject.optString("profilePic"),
                jsonObject.optString("authToken"),
                jsonObject.optString("roles"),
                jsonObject.optInt("sendNoti"),
                jsonObject.optString("bio"),
                jsonObject.optString("instagram"),
                jsonObject.optString("userName"),
                jsonObject.optBoolean("deviceId"),
                jsonObject.optString("firstName"),
                jsonObject.optString("createdAt"),
                jsonObject.optString("password"),
                jsonObject.optBoolean("isDeleted"),
                jsonObject.optString("provider"),
                jsonObject.optString("providerId"),
                jsonObject.optString("referralCode"),
                jsonObject.optInt("__v"),
                jsonObject.optString("_id"),
                jsonObject.optString("email"),
                jsonObject.optInt("status"),
                jsonObject.optString("updatedAt")
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
                jsonObject.optString("id"),
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

        private fun parsePost(jsonObject: JSONObject): Post {
            var hashTagsArray = jsonObject.optJSONArray("hashtags")
            if (hashTagsArray == null)
                hashTagsArray = JSONArray()

            var userIdObject = jsonObject.optJSONObject("userId")
            if (userIdObject == null)
                userIdObject = JSONObject()

            var songObject = jsonObject.optJSONObject("song")
            if (songObject == null)
                songObject = JSONObject()

            return Post(
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
                jsonObject.optBoolean("isLiked"),
                parseUserId(userIdObject),
                parseSong(songObject),
                jsonObject.optInt("views")
            )
        }

        private fun parsePosts(jsonArray: JSONArray): ArrayList<Post> {
            val posts = arrayListOf<Post>()

            for (i in 0 until jsonArray.length()) {
                posts.add(parsePost(jsonArray.optJSONObject(i)))
            }

            return posts
        }

        fun parseResponse(jsonElement: JsonElement?): ProfilePageResponse? {
            try {
                val stringJsonElement = VrockkApplication.gson?.toJson(jsonElement)
                if (stringJsonElement != null) {
                    val responseObject = JSONObject(stringJsonElement)
                    val dataObject = responseObject.optJSONObject("data")

                    val pageResponse = ProfilePageResponse()
                    pageResponse.success = responseObject.optBoolean(SUCCESS)
                    pageResponse.message = responseObject.optString(MESSAGE)

                    val data = Data()

                    var profileArray = dataObject.optJSONArray("profile")
                    if (profileArray == null)
                        profileArray = JSONArray()
                    Log.i(TAG, "parseResponse.profileArray: ${profileArray.length()}")
                    data.profile = parseProfiles(profileArray)

                    var postsArray = dataObject.optJSONArray("posts")
                    if (postsArray == null)
                        postsArray = JSONArray()
                    Log.i(TAG, "parseResponse.postsArray: ${postsArray.length()}")
                    data.posts = parsePosts(postsArray)

                    data.total = dataObject.optInt("total")

                    pageResponse.data = data;
                    return pageResponse
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
    }
}