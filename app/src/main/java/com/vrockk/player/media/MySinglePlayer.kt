package com.vrockk.player.media

import android.content.Context
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.util.MimeTypes

class MySinglePlayer(val context: Context) {
    companion object {
        const val TAG = "MySinglePlayer"

        private val instancesMap = HashMap<String, MySinglePlayer>()

        fun getInstance(tag: String): MySinglePlayer? {
            return instancesMap[tag]
        }

        fun createInstance(tag: String, context: Context): MySinglePlayer {
            if (instancesMap.containsKey(tag))
                return instancesMap[tag]!!

            val myPlayer = MySinglePlayer(context)
            instancesMap[tag] = myPlayer
            return myPlayer
        }

        fun destroy() {
            for(tag in instancesMap.keys) {
                instancesMap[tag]?.resetAndRelease()
            }
            instancesMap.clear()
        }
    }

    private val videoUrls = ArrayList<String>()
    private var totalElements = 0
    private var currentPlaying = -1

    private var singlePlayer: SimpleExoPlayer? = null

    init {
        initExoplayer()
    }

    /** Player tools implementation begins here *///////////////////////////////////////////////////
    fun appendToUrls(videoUrls: ArrayList<String>) {
        this.videoUrls.addAll(videoUrls)
        totalElements = this.videoUrls.size
    }

    fun getCurrentPlaying(): Int {
        return currentPlaying
    }

    private fun assignMediaItem(exoPlayer: SimpleExoPlayer?, videoUrl: String) {
        assignMediaItem(exoPlayer, videoUrl, true)
    }

    private fun assignMediaItem(exoPlayer: SimpleExoPlayer?, videoUrl: String, playWhenReady: Boolean) {
        stopPlayer(exoPlayer)

        val mediaItem = MediaItem.Builder()
            .setUri(videoUrl)
            .setMimeType(MimeTypes.APPLICATION_MP4)
            .build()
        exoPlayer?.setMediaSource(PlayerUtils.PROGRESSIVE_MEDIA_RESOURCE_FACTORY_CACHED.createMediaSource(mediaItem))
        exoPlayer?.seekTo(0)
        exoPlayer?.volume = 0f
        exoPlayer?.playWhenReady = playWhenReady
        exoPlayer?.prepare()
    }

    private fun pausePlayer(exoPlayer: SimpleExoPlayer?) {
        try {
            exoPlayer!!.pause()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playPlayer(exoPlayer: SimpleExoPlayer?) {
        try {
            exoPlayer!!.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopPlayer(exoPlayer: SimpleExoPlayer?) {
        try {
            exoPlayer!!.stop(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releasePlayer(exoPlayer: SimpleExoPlayer?) {
        try {
            exoPlayer!!.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun clear() {
        videoUrls.clear()
        currentPlaying = -1
    }
    /** Player tools implementation ends here */////////////////////////////////////////////////////


    /** Single-Player implementation begins here *//////////////////////////////////////////////////
    private fun initExoplayer() {
        if (singlePlayer == null) {
            singlePlayer = SimpleExoPlayer.Builder(context)
                .setTrackSelector(PlayerUtils.TRACK_SELECTOR)
                .setLoadControl(PlayerUtils.LOAD_CONTROL)
//                .setMediaSourceFactory(DEFAULT_MEDIA_RESOURCE_FACTORY_CACHED)
                .build()
            singlePlayer?.repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    fun changeItem(requestedPosition: Int) {
        changeItem(requestedPosition, true)
    }

    fun changeItem(requestedPosition: Int, playWhenReady: Boolean) {
        if (requestedPosition < 0 || requestedPosition > totalElements - 1)
            return

        if (isPlaying())
            stop()

        currentPlaying = requestedPosition
        assignMediaItem(singlePlayer, videoUrls[requestedPosition], playWhenReady)
        singlePlayer?.volume = 1f
    }

    fun getPlayer(): SimpleExoPlayer? {
        return singlePlayer
    }

    fun pause() {
        pausePlayer(singlePlayer)
    }

    fun isPlaying(): Boolean {
        return singlePlayer!!.isPlaying
    }

    fun resume() {
        playPlayer(singlePlayer)
    }

    fun restart() {
        try {
            singlePlayer?.stop(true)
            singlePlayer?.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        stopPlayer(singlePlayer)
    }

    fun release() {
        releasePlayer(singlePlayer)
    }

    fun reset() {
        stop()
        clear()
    }

    fun resetAndRelease() {
        reset()
        release()
    }
    /** Single-Player implementation ends here *////////////////////////////////////////////////////
}