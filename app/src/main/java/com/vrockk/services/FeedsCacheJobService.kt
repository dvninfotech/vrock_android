package com.vrockk.services

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.cache.CacheWriter
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.vrockk.player.cache.CacheUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FeedsCacheJobService : JobIntentService() {
    companion object {
        const val TAG = "FeedsCacheJobService"
        const val EXTRA_VIDEOS = "VIDEO_URLS"
        val BYTES = ByteArray(1024 * 1024)

        fun startCaching(context: Context, jobId: Int, intent: Intent) {
            enqueueWork(context, FeedsCacheJobService::class.java, jobId, intent)
        }
    }

    private lateinit var context: Context
    private var simpleCache: SimpleCache? = null
    private var cachingJobs: ArrayList<Job>? = null
    private var currentJobPosition: Int = 0

    //    private var cachingJob: Job? = null
    private var videoUrls: ArrayList<String>? = null

    override fun onHandleWork(intent: Intent) {
        context = applicationContext
        simpleCache = CacheUtils.myInstance().simpleCache

        currentJobPosition = 0
        videoUrls = intent.getStringArrayListExtra(EXTRA_VIDEOS)
        Log.i(TAG, "onHandleIntent: $videoUrls")

        if (!videoUrls.isNullOrEmpty()) {
//            for (i in 0 until videoUrls.size) {
            preCacheVideo(videoUrls!![currentJobPosition], currentJobPosition)
//            }
        } else
            stopSelf()
    }

    private fun preCacheVideo(videoUrl: String?, position: Int) {
        var videoUrlTemp = videoUrl

        if (!videoUrlTemp.isNullOrBlank()) {
            if (videoUrlTemp.contains("#"))
                videoUrlTemp = videoUrlTemp.replace("#", "")

            val videoUri = Uri.parse(videoUrlTemp)
            val dataSpec = DataSpec(videoUri)
            val progressListener =
                CacheWriter.ProgressListener { requestLength, bytesCached, newBytesCached ->
                    run {
                        val downloadPercentage: Double = (bytesCached * 100.0 / requestLength)
//                        Log.i(TAG, "preCacheVideo: $videoUrl $downloadPercentage")
                        if (downloadPercentage >= 100) {
                            ++currentJobPosition
                            if (currentJobPosition < videoUrls!!.size)
                                preCacheVideo(videoUrls!![currentJobPosition], currentJobPosition)
                        }
                    }
                }

            val job = GlobalScope.launch(Dispatchers.Unconfined) {
                cacheVideo(dataSpec, progressListener)
            }
            job.start()
            cachingJobs?.add(position, job)
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
            BYTES,
            progressListener
        )
        try {
            cacheWriter.cache()
        } catch (e: Exception) {
            cacheWriter.cancel()
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (!cachingJobs.isNullOrEmpty()) {
            for (i in 0 until cachingJobs!!.size) {
                cachingJobs!![i].cancel()
            }
        }
    }
}