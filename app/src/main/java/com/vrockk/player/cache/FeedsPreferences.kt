package com.vrockk.player.cache

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken

class FeedsPreferences {
    companion object {
        const val PREFS_NAME = "VrockkFeedsPrefs"
        private const val KEY_TRENDING_FEEDS = "prefs_key_trending_feeds"
        private const val KEY_FOR_YOU_FEEDS = "prefs_for_you_key_feeds"

        private fun getPrefs(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }

        fun saveTrendingJson(context: Context, jsonElement: JsonElement) {
            getPrefs(context).edit().putString(KEY_TRENDING_FEEDS, jsonElement.toString()).apply()
        }

        fun getTrendingFeeds(context: Context): JsonElement {
            return Gson().fromJson<JsonElement>(
                getPrefs(context).getString(KEY_TRENDING_FEEDS, "{}"),
                object : TypeToken<JsonElement>(){}.type)
        }

        fun saveForYouJson(context: Context, jsonElement: JsonElement) {
            getPrefs(context).edit().putString(KEY_FOR_YOU_FEEDS, jsonElement.asString).apply()
        }

        fun getForYouFeeds(context: Context): JsonElement {
            return Gson().fromJson<JsonElement>(
                getPrefs(context).getString(KEY_FOR_YOU_FEEDS, "{}"),
                object : TypeToken<JsonElement>(){}.type)
        }
    }
}