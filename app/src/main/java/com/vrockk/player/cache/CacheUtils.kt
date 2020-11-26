package com.vrockk.player.cache

import android.app.Application
import android.content.Context
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import com.vrockk.VrockkApplication
import java.util.concurrent.TimeUnit

class CacheUtils(application: Application) {
    companion object {
        const val MAX_FILE_COUNT = 40
        const val MAX_CACHE_SIZE: Long = 500 * 1024 * 1024

        var instance: CacheUtils? = null

        fun myInstance(): CacheUtils {
            return instance!!
        }
    }
    
    var context: Context = application
    lateinit var simpleCache: SimpleCache
    lateinit var leastRecentlyUsedCacheEvictor: LeastRecentlyUsedCacheEvictor
    lateinit var feedsCacheEvictor: FeedsCacheEvictor
    lateinit var exoDatabaseProvider: ExoDatabaseProvider
    lateinit var cacheDataSinkFactory: CacheDataSink.Factory
    lateinit var httpDataSourceFactory: DefaultHttpDataSourceFactory
    lateinit var cacheDataSourceFactory: CacheDataSource.Factory

    init {
        instance = this

        val directory = context.getDir(VrockkApplication.appName, Application.MODE_PRIVATE)

        if (!directory.exists())
            directory.mkdir()

//        leastRecentlyUsedCacheEvictor = LeastRecentlyUsedCacheEvictor(exoPlayerCacheSize)
        feedsCacheEvictor = FeedsCacheEvictor(
            MAX_FILE_COUNT,
            directory!!.listFiles()!!.size
        )
        exoDatabaseProvider = ExoDatabaseProvider(context)

        simpleCache = SimpleCache(/*cacheDir*/ directory,
            /*leastRecentlyUsedCacheEvictor!!*/ feedsCacheEvictor,
            exoDatabaseProvider
        )
        cacheDataSinkFactory = CacheDataSink.Factory().setCache(simpleCache)
        httpDataSourceFactory = DefaultHttpDataSourceFactory(
            VrockkApplication.context.let {
                Util.getUserAgent(it, VrockkApplication.appName)
            },
            TimeUnit.SECONDS.toMillis(60).toInt(),
            TimeUnit.SECONDS.toMillis(60).toInt(),
            true
        )
//        httpDataSourceFactory = DefaultHttpDataSourceFactory(
//            VrockkApplication.context.let {
//                Util.getUserAgent(it, VrockkApplication.appName)
//            }
//        )

        cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setCacheWriteDataSinkFactory(cacheDataSinkFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }
}