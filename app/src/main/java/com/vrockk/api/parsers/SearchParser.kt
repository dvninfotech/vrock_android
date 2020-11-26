package com.vrockk.api.parsers

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.vrockk.api.MESSAGE
import com.vrockk.api.SUCCESS
import com.vrockk.models.search.*
import org.json.JSONObject

class SearchParser {
    companion object {
        private fun parseHashTagsDataModel(jsonObject: JSONObject): SearchHashTagDataModel {
            return SearchHashTagDataModel(
                jsonObject.optString("_id"),
                jsonObject.optInt("__v"),
                jsonObject.optString("name")
            )
        }

        private fun parsePostDataModel(jsonObject: JSONObject): SearchPostDataModel {
            return SearchPostDataModel(
                jsonObject.optInt("__v"),
                jsonObject.optString("_id"),
                jsonObject.optString("originalName"),
                jsonObject.optString("post"),
                jsonObject.optString("thumbnail")
            )
        }

        private fun parseSongDataModel(jsonObject: JSONObject): SearchSongDataModel {
            var descriptionObject = jsonObject.optJSONObject("description")
            if (descriptionObject == null)
                descriptionObject = JSONObject()
            return SearchSongDataModel(
                jsonObject.optString("_id"),
                jsonObject.optString("originalName"),
                jsonObject.optString("song"),
                descriptionObject.optString("name", ""),
                jsonObject.optString("uploadedBy")
            )
        }

        private fun parseUserDataModel(jsonObject: JSONObject): SearchUserDataModel {
            var username = jsonObject.optString("userName")
            if (username.isNullOrEmpty())
                username = jsonObject.optString("firstName")
            return SearchUserDataModel(
                jsonObject.optInt("__v"),
                jsonObject.optString("_id"),
                /*jsonObject.optString("userName")*/ username,
                jsonObject.optString("profilePic")
            )
        }

        fun parseSearchHashTags(jsonElement: JsonElement): SearchHashTagModel? {
            try {
                val stringJsonElement = Gson().toJson(jsonElement)
                if (stringJsonElement != null) {
                    val responseObject = JSONObject(stringJsonElement)
                    val hashTagModel = SearchHashTagModel()

                    hashTagModel.success = responseObject.optBoolean(SUCCESS)
                    hashTagModel.message = responseObject.optString(MESSAGE)
                    hashTagModel.total = responseObject.optInt("total")

                    val tagsArray = responseObject.optJSONArray("hashTags")
                    val dataModels = arrayListOf<SearchHashTagDataModel>()
                    for (i in 0 until tagsArray!!.length()) {
                        dataModels.add(parseHashTagsDataModel(tagsArray.optJSONObject(i)))
                    }

                    hashTagModel.dataModels.addAll(dataModels)
                    return hashTagModel
                }
            } catch (e : Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun parseSearchPosts(jsonElement: JsonElement): SearchPostModel? {
            try {
                val stringJsonElement = Gson().toJson(jsonElement)
                if (stringJsonElement != null) {
                    val responseObject = JSONObject(stringJsonElement)
                    val postModel = SearchPostModel()

                    postModel.success = responseObject.optBoolean(SUCCESS)
                    postModel.message = responseObject.optString(MESSAGE)
                    postModel.total = responseObject.optInt("total")

                    val postsArray = responseObject.optJSONArray("posts")
                    val dataModels = arrayListOf<SearchPostDataModel>()
                    for (i in 0 until postsArray!!.length()) {
                        dataModels.add(parsePostDataModel(postsArray.optJSONObject(i)))
                    }

                    postModel.dataModels.addAll(dataModels)
                    return postModel
                }
            } catch (e : Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun parseSearchSongs(jsonElement: JsonElement): SearchSongModel? {
            try {
                val stringJsonElement = Gson().toJson(jsonElement)
                if (stringJsonElement != null) {
                    val responseObject = JSONObject(stringJsonElement)
                    val songModel = SearchSongModel()

                    songModel.success = responseObject.optBoolean(SUCCESS)
                    songModel.message = responseObject.optString(MESSAGE)
                    songModel.total = responseObject.optInt("total")

                    val songsArray = responseObject.optJSONArray("songs")
                    val dataModels = arrayListOf<SearchSongDataModel>()
                    for (i in 0 until songsArray!!.length()) {
                        dataModels.add(parseSongDataModel(songsArray.optJSONObject(i)))
                    }

                    songModel.dataModels.addAll(dataModels)
                    return songModel
                }
            } catch (e : Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun parseSearchUsers(jsonElement: JsonElement): SearchUserModel? {
            try {
                val stringJsonElement = Gson().toJson(jsonElement)
                if (stringJsonElement != null) {
                    val responseObject = JSONObject(stringJsonElement)
                    val userModel = SearchUserModel()

                    userModel.success = responseObject.optBoolean(SUCCESS)
                    userModel.message = responseObject.optString(MESSAGE)
                    userModel.total = responseObject.optInt("total")

                    val usersArray = responseObject.optJSONArray("users")
                    val dataModels = arrayListOf<SearchUserDataModel>()
                    for (i in 0 until usersArray!!.length()) {
                        dataModels.add(parseUserDataModel(usersArray.optJSONObject(i)))
                    }

                    userModel.dataModels.addAll(dataModels)
                    return userModel
                }
            } catch (e : Exception) {
                e.printStackTrace()
            }
            return null
        }
    }
}