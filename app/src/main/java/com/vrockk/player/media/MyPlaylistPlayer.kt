package com.vrockk.player.media

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.util.MimeTypes

class MyPlaylistPlayer(val context: Context) {
    companion object {
        const val TAG = "MyPlaylistPlayer"

        private val instancesMap = HashMap<String, MyPlaylistPlayer>()

        fun getInstance(tag: String): MyPlaylistPlayer? {
            return instancesMap[tag]
        }

        fun createInstance(tag: String, context: Context): MyPlaylistPlayer {
            val myPlayer = MyPlaylistPlayer(context)
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

    private var playlistPlayer: SimpleExoPlayer? = null

    init {
        initExoplayer()
    }

    /** Player tools implementation begins here *///////////////////////////////////////////////////
    fun appendToUrls(videoUrls: ArrayList<String>) {
        this.videoUrls.addAll(videoUrls)
        totalElements = this.videoUrls.size

        appendPlaylist(videoUrls)
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


    /** Playlist implementation begins here *///////////////////////////////////////////////////////
    private fun initExoplayer() {
        if (playlistPlayer == null) {
            playlistPlayer = SimpleExoPlayer.Builder(context)
                .setTrackSelector(PlayerUtils.TRACK_SELECTOR)
                .setLoadControl(PlayerUtils.LOAD_CONTROL)
                .build()
            playlistPlayer?.repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    private fun appendPlaylist(videoUrls: ArrayList<String>) {
        val mediaSources = arrayListOf<MediaSource>()
        for (videoUrl in videoUrls) {
            mediaSources.add(
                PlayerUtils.PROGRESSIVE_MEDIA_RESOURCE_FACTORY_CACHED
                .createMediaSource(MediaItem.fromUri(Uri.parse(videoUrl))))
        }
        playlistPlayer?.addMediaSources(mediaSources)

        if (totalElements == videoUrls.size) {
            playlistPlayer?.playWhenReady = true
            playlistPlayer?.prepare()
        }
    }

    fun changeItem(requestedPosition: Int) {
        if (requestedPosition == currentPlaying || requestedPosition < 0 || requestedPosition >= totalElements)
            return
        else if (requestedPosition == 0 && currentPlaying == -1) {
            currentPlaying = requestedPosition
            playlistPlayer?.play()
        } else if (requestedPosition > currentPlaying) {
            currentPlaying = requestedPosition
            if (playlistPlayer!!.hasNext())
                playlistPlayer?.next()
            if (!playlistPlayer!!.isPlaying)
                playlistPlayer?.play()
        } else if (requestedPosition < currentPlaying) {
            currentPlaying = requestedPosition
            if (playlistPlayer!!.hasPrevious())
                playlistPlayer?.previous()
            if (!playlistPlayer!!.isPlaying)
                playlistPlayer?.play()
        }
    }

    fun getPlayer(): SimpleExoPlayer? {
        return playlistPlayer
    }

    fun pause() {
        pausePlayer(playlistPlayer)
    }

    fun isPlaying(): Boolean {
        return playlistPlayer!!.isPlaying
    }

    fun resume() {
        playPlayer(playlistPlayer)
    }

    fun stop() {
        stopPlayer(playlistPlayer)
    }

    fun release() {
        releasePlayer(playlistPlayer)
    }

    fun reset() {
        playlistPlayer?.clearMediaItems()
        stop()
        clear()
    }

    fun resetAndRelease() {
        reset()
        release()
    }
    /** Playlist implementation ends here */////////////////////////////////////////////////////////
}