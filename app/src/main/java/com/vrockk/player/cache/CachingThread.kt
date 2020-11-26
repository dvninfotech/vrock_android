package com.vrockk.player.cache

import android.net.Uri
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.cache.CacheWriter
import com.vrockk.services.FeedsCacheJobService

class CachingThread(val videoUrl: String?): Thread() {
    companion object {
        const val TAG = "CachingThread"
    }

    override fun run() {
        var videoUrlTemp = videoUrl!!

        if (!videoUrlTemp.isBlank()) {
            if (videoUrlTemp.contains("#"))
                videoUrlTemp = videoUrlTemp.replace("#", "")

            val videoUri = Uri.parse(videoUrlTemp)
            val dataSpec = DataSpec(videoUri,0, 400*1024)
            val progressListener =
                CacheWriter.ProgressListener { requestLength, bytesCached, newBytesCached ->
                    run {
//                        val downloadPercentage: Double = (bytesCached * 100.0 / requestLength)
//                        Log.i(TAG, "preCacheVideo: $videoUrl $downloadPercentage")
                    }
                }

            cacheVideo(dataSpec, progressListener)
        }
    }

    private fun cacheVideo(
        dataSpec: DataSpec,
        progressListener: CacheWriter.ProgressListener
    ) {
        val cacheWriter = CacheWriter(
            CacheUtils.myInstance().cacheDataSourceFactory.createDataSource(),
            dataSpec,
            true,
            FeedsCacheJobService.BYTES,
            progressListener
        )
        try {
            cacheWriter.cache()
        } catch (e: Exception) {
            cacheWriter.cancel()
            e.printStackTrace()
        }
    }
}