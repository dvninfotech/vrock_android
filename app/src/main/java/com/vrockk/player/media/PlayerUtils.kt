package com.vrockk.player.media

import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.vrockk.player.cache.CacheUtils

class PlayerUtils {
    companion object {
        val TRACK_SELECTOR = DefaultTrackSelector(
            DefaultTrackSelector.ParametersBuilder(CacheUtils.myInstance().context)
                .setForceHighestSupportedBitrate(false)
                .setViewportSizeToPhysicalDisplaySize(CacheUtils.myInstance().context, false)
                .build(), AdaptiveTrackSelection.Factory()
        )

        val LOAD_CONTROL = DefaultLoadControl.Builder()
//            .setAllocator(DefaultAllocator(true, 16, 10))
            .setBufferDurationsMs(
                1 * 1000,
                90 * 1000,
                1 * 1000,
                1 *1000
            )
//            .setTargetBufferBytes(-1)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()


        val DEFAULT_MEDIA_RESOURCE_FACTORY = DefaultMediaSourceFactory(
            CacheUtils.myInstance().context
        )
        val DEFAULT_MEDIA_RESOURCE_FACTORY_CACHED = DefaultMediaSourceFactory(
            CacheUtils.myInstance().cacheDataSourceFactory
        )
        val PROGRESSIVE_MEDIA_RESOURCE_FACTORY = ProgressiveMediaSource.Factory(
            CacheUtils.myInstance().httpDataSourceFactory
        )
        val PROGRESSIVE_MEDIA_RESOURCE_FACTORY_CACHED = ProgressiveMediaSource.Factory(
            CacheUtils.myInstance().cacheDataSourceFactory
        )
    }
}