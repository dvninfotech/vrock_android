package com.vrockk.player.trial1

import android.content.Context
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.util.MimeTypes
import com.vrockk.player.cache.CacheUtils

class PlayerUtils {
    companion object {
        const val MEDIA_UNPREPARED = 12
        const val MEDIA_READY = 13
        const val MEDIA_PLAYING = 14

        private var exoPlayer: SimpleExoPlayer? = null
        var mediaState: Int = MEDIA_UNPREPARED
        var shouldPlay: Boolean = true

        private val TRACK_SELECTOR = DefaultTrackSelector(
            DefaultTrackSelector.ParametersBuilder(CacheUtils.myInstance().context)
                .setForceHighestSupportedBitrate(false)
                .setViewportSizeToPhysicalDisplaySize(CacheUtils.myInstance().context, false)
                .build(), AdaptiveTrackSelection.Factory()
        )

        private val LOAD_CONTROL = DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, 16))
            .setBufferDurationsMs(
                500,
                60000,
                500,
                500
            )
            .setTargetBufferBytes(-1)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        private val MEDIA_RESOURCE_FACTORY = DefaultMediaSourceFactory(
            CacheUtils.myInstance().cacheDataSourceFactory
        )

        fun getMyExoplayer(context: Context): SimpleExoPlayer? {
            if (exoPlayer == null) {
                exoPlayer = SimpleExoPlayer.Builder(context)
                    .setTrackSelector(TRACK_SELECTOR)
                    .setLoadControl(LOAD_CONTROL)
                    .setMediaSourceFactory(MEDIA_RESOURCE_FACTORY)
                    .build()
                exoPlayer?.repeatMode = Player.REPEAT_MODE_ONE
            }

            return exoPlayer
        }

        fun assignMediaItemToMyExoplayer(exoPlayer: SimpleExoPlayer?, videoUrl: String) {
            stop()

            val mediaItem = MediaItem.Builder()
                .setUri(videoUrl)
                .setMimeType(MimeTypes.APPLICATION_MP4)
                .build()
            exoPlayer?.setMediaSource(MEDIA_RESOURCE_FACTORY.createMediaSource(mediaItem))
        }

        private fun assignMediaItem(videoUrl: String) {
            val mediaItem = MediaItem.Builder()
                .setUri(videoUrl)
                .setMimeType(MimeTypes.APPLICATION_MP4)
                .build()
            exoPlayer?.setMediaSource(MEDIA_RESOURCE_FACTORY.createMediaSource(mediaItem))
//            exoPlayer!!.playWhenReady = false
        }

        fun assignMediaItemAndPrepare(videoUrl: String) {
            stop()
            assignMediaItem(videoUrl)
            prepare()
        }

        fun prepare() {
            if (exoPlayer != null) {
                exoPlayer!!.playWhenReady = true
                exoPlayer!!.prepare()
            }
        }

        fun start() {
            resume()
        }

        fun resume() {
            if (exoPlayer != null) {
                mediaState = MEDIA_PLAYING
                exoPlayer!!.play()
            }
        }

        fun isPlaying():Boolean {
            if (exoPlayer != null)
                return exoPlayer!!.isPlaying
            return false
        }

        fun pause() {
            if (exoPlayer != null) {
                mediaState = MEDIA_READY
                exoPlayer!!.pause()
            }
        }

        fun stop() {
            if (exoPlayer != null) {
                mediaState = MEDIA_UNPREPARED
                exoPlayer!!.stop()
            }
        }

        fun release() {
            stop()
            if (exoPlayer != null)
                exoPlayer!!.release()
        }
    }
}