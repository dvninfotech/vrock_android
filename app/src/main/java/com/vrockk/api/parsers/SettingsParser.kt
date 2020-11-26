package com.vrockk.api.parsers

import com.google.gson.JsonElement
import com.vrockk.VrockkApplication
import com.vrockk.api.MESSAGE
import com.vrockk.api.SUCCESS
import com.vrockk.models.settings.ShortUrlData
import org.json.JSONObject

class SettingsParser {
    companion object {
        fun parseShortUrlResponse(jsonElement: JsonElement?): ShortUrlData? {
            try {
                val stringJsonElement = VrockkApplication.gson?.toJson(jsonElement)
                if (stringJsonElement != null) {
                    val responseObject = JSONObject(stringJsonElement)
                    val dataObject = responseObject.optJSONObject("data")

                    val shortUrlData = ShortUrlData()
                    if (dataObject != null) {
                        shortUrlData.success = responseObject.optBoolean(SUCCESS)
                        shortUrlData.message = responseObject.optString(MESSAGE)
                        shortUrlData._id = dataObject.optString("_id")
                        shortUrlData.longUrl = dataObject.optString("longUrl")
                        shortUrlData.shortUrl = dataObject.optString("shortUrl")
                        shortUrlData.shortKey = dataObject.optString("shortKey")
                        shortUrlData.createdAt = dataObject.optString("createdAt")
                        shortUrlData.updatedAt = dataObject.optString("updatedAt")
                        shortUrlData._v = dataObject.optInt("__v")
                    }

                    return shortUrlData
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
    }
}