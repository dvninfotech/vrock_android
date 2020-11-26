package com.vrockk.player.media

import android.content.Context
import android.util.Log
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.util.MimeTypes

class MyMultiPlayer(val context: Context/*, val handler: Handler*/) {
    companion object {
        const val TAG = "MyMultiPlayer"

        private val instancesMap = HashMap<String, MyMultiPlayer>()

        const val PLAYER_NOT_ASSIGNED = 0
        const val PLAYER_1 = 1
        const val PLAYER_2 = 2
        const val PLAYER_3 = 3

        fun getInstance(tag: String): MyMultiPlayer? {
            return instancesMap[tag]
        }

        fun createInstance(tag: String, context: Context): MyMultiPlayer {
            if (instancesMap.containsKey(tag))
                return instancesMap[tag]!!

            val myPlayer = MyMultiPlayer(context)
            instancesMap[tag] = myPlayer
            return myPlayer
        }

        fun destroy() {
            for (tag in instancesMap.keys) {
                instancesMap[tag]?.resetAndRelease()
            }
            instancesMap.clear()
        }
    }

    private val videoUrls = ArrayList<String>()
    private var totalElements = 0
    private var currentPlaying = -1

    /*players are always assigned -1,0,+1 in order of their number in a loop as in 1232123...*/
    private var exoPlayer1: SimpleExoPlayer? = null
    private var exoPlayer2: SimpleExoPlayer? = null
    private var exoPlayer3: SimpleExoPlayer? = null
    private var currentPlayer = PLAYER_NOT_ASSIGNED

    private var werePlayersReleased = false

    init {
        initExoplayers()
    }

    private fun initExoplayer(player: Int): SimpleExoPlayer? {
        return when (player) {
            PLAYER_1 -> {
                exoPlayer1
            }
            PLAYER_2 -> {
                exoPlayer2
            }
            else -> {
                exoPlayer3
            }
        }
    }

    /** Player tools implementation begins here *///////////////////////////////////////////////////
    fun appendToUrls(videoUrls: ArrayList<String>) {
        this.videoUrls.addAll(videoUrls)
        totalElements = this.videoUrls.size
    }

    fun getCurrentPlaying(): Int {
        return currentPlaying
    }

    private fun assignMediaItem(player: Int, videoUrl: String) {
        assignMediaItem(player, videoUrl, true)
    }

    private fun assignMediaItem(
        player: Int,
        videoUrl: String,
        playWhenReady: Boolean
    ) {
//        releasePlayer(if (player == PLAYER_1) exoPlayer1 else if (player == PLAYER_2) exoPlayer2 else exoPlayer3)
        val exoPlayer = initExoplayer(player)

        val mediaItem = MediaItem.Builder()
            .setUri(videoUrl)
            .setMimeType(MimeTypes.APPLICATION_MP4)
            .build()
        exoPlayer?.setMediaSource(
            PlayerUtils.PROGRESSIVE_MEDIA_RESOURCE_FACTORY/*_CACHED*/.createMediaSource(
                mediaItem
            )
        )
//        exoPlayer?.seekTo(0)
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


    /** Multi-Players implementation begins here *//////////////////////////////////////////////////
    private fun initExoplayers() {
//        PROGRESSIVE_MEDIA_RESOURCE_FACTORY_CACHED.setContinueLoadingCheckIntervalBytes(10 * 1024 * 1024)

        if (exoPlayer1 == null) {
            exoPlayer1 = SimpleExoPlayer.Builder(context)
                .setTrackSelector(PlayerUtils.TRACK_SELECTOR)
                .setLoadControl(PlayerUtils.LOAD_CONTROL)
                .build()
            exoPlayer1?.repeatMode = Player.REPEAT_MODE_ONE
        }
        if (exoPlayer2 == null) {
            exoPlayer2 = SimpleExoPlayer.Builder(context)
                .setTrackSelector(PlayerUtils.TRACK_SELECTOR)
                .setLoadControl(PlayerUtils.LOAD_CONTROL)
                .build()
            exoPlayer2?.repeatMode = Player.REPEAT_MODE_ONE
        }
        if (exoPlayer3 == null) {
            exoPlayer3 = SimpleExoPlayer.Builder(context)
                .setTrackSelector(PlayerUtils.TRACK_SELECTOR)
                .setLoadControl(PlayerUtils.LOAD_CONTROL)
                .build()
            exoPlayer3?.repeatMode = Player.REPEAT_MODE_ONE
        }

        werePlayersReleased = false
    }

    fun changeItem(requestedPosition: Int) {
        changeItem(requestedPosition, true)
    }

    fun changeItem(requestedPosition: Int, playWhenReady: Boolean) {
        if (werePlayersReleased)
            initExoplayers()

        if (totalElements == 0) {
            Log.e(TAG, "prepare: no elements to start playback")
            return
        }

        val currentPrevious = currentPlaying - 1
        val currentNext = currentPlaying + 1

        val requestedPrevious = requestedPosition - 1
        val requestedNext = requestedPosition + 1

        if (currentPlaying == requestedPosition) {
            return
        }

        if (requestedPosition < currentPlaying) {
            if (requestedPosition < 0)
                return // trying to play from index less than min-index

            requestedPlayPreviousBy1(requestedPrevious, requestedPosition)
        } else {
            if (requestedPosition > totalElements - 1)
                return // trying to play from index greater than max-index

            requestedPlayNextBy1(requestedPosition, requestedNext, playWhenReady)
        }
    }

    private fun requestedPlayPreviousBy1(
        requestedPrevious: Int,
        requestedPosition: Int
    ) {
        // order of players is ...231231232.....
        if (currentPlayer == PLAYER_1) {
            /** we don't need to assign next to player 1 because it already points to the same,
            hence, we'll just turn it's volume off*/
            // assign next video to next-in-line player(i.e.1)
//            assignMediaItem(exoPlayer1, videoUrls[currentPlaying + 1])

            exoPlayer1?.volume = 0f

            // switch to player3 as it holds the previous video
            exoPlayer3?.volume = 1f
            currentPlaying = requestedPosition
            currentPlayer = PLAYER_3

            if (requestedPrevious >= 0) {
                // assign previous video to previous-in-line player(i.e.2)
                assignMediaItem(PLAYER_2, videoUrls[requestedPrevious], false)
            }
        } else if (currentPlayer == PLAYER_2) {
            /** we don't need to assign next to player 2 because it already points to the same,
            hence, we'll just turn it's volume off*/
            // assign next video to next-in-line player(i.e.2)
//            assignMediaItem(exoPlayer2, videoUrls[currentPlaying + 1])

            exoPlayer2?.volume = 0f

            // switch to player1 as it holds the previous video
            exoPlayer1?.volume = 1f
            currentPlaying = requestedPosition
            currentPlayer = PLAYER_1

            if (requestedPrevious >= 0) {
                // assign previous video to previous-in-line player(i.e.3)
                assignMediaItem(PLAYER_3, videoUrls[requestedPrevious], false)
            }
        } else if (currentPlayer == PLAYER_3) {
            /** we don't need to assign next to player 3 because it already points to the same,
            hence, we'll just turn it's volume off*/
            // assign next video to next-in-line player(i.e.3)
//            assignMediaItem(exoPlayer3, videoUrls[currentPlaying + 1])

            exoPlayer3?.volume = 0f

            // switch to player2 as it holds the previous video
            exoPlayer2?.volume = 1f
            currentPlaying = requestedPosition
            currentPlayer = PLAYER_2

            if (requestedPrevious >= 0) {
                // assign previous video to previous-in-line player(i.e.1)
                assignMediaItem(PLAYER_1, videoUrls[requestedPrevious], false)
            }
        } else if (currentPlayer == PLAYER_NOT_ASSIGNED) {
            Log.e(TAG, "requestedPlayPreviousBy1: currentPlayer == PLAYER_NOT_ASSIGNED")
        }
    }

    private fun requestedPlayNextBy1(
        requestedPosition: Int,
        requestedNext: Int,
        playWhenReady: Boolean
    ) {
        // order of players is ...231231232.....
        if (currentPlayer == PLAYER_1) {
            /** we don't need to assign previous to player 1 because it already points to the same,
            hence, we'll just turn it's volume off*/
            // assign previous video to previous-in-line player(i.e.1)
//            assignMediaItem(exoPlayer1, videoUrls[currentPlaying - 1])

            exoPlayer1?.volume = 0f

            // switch to player2 as it holds the next video
            exoPlayer2?.volume = 1f
            currentPlaying = requestedPosition
            currentPlayer = PLAYER_2

            if (requestedNext < totalElements) {
                // assign next video to next-in-line player(i.e.3)
                assignMediaItem(PLAYER_3, videoUrls[requestedNext], false)
            }
        } else if (currentPlayer == PLAYER_2) {
            /** we don't need to assign previous to player 2 because it already points to the same,
            hence, we'll just turn it's volume off*/
            // assign previous video to previous-in-line player(i.e.2)
//            assignMediaItem(exoPlayer2, videoUrls[currentPlaying - 1])

            exoPlayer2?.volume = 0f

            // switch to player3 as it holds the next video
            exoPlayer3?.volume = 1f
            currentPlaying = requestedPosition
            currentPlayer = PLAYER_3

            if (requestedNext < totalElements) {
                // assign next video to next-in-line player(i.e.1)
                assignMediaItem(PLAYER_1, videoUrls[requestedNext], false)
            }
        } else if (currentPlayer == PLAYER_3) {
            /** we don't need to assign previous to player 3 because it already points to the same,
            hence, we'll just turn it's volume off*/
            // assign previous video to previous-in-line player(i.e.3)
//            assignMediaItem(exoPlayer3, videoUrls[currentPlaying - 1])

            exoPlayer3?.volume = 0f

            // switch to player1 as it holds the next video
            exoPlayer1?.volume = 1f
            currentPlaying = requestedPosition
            currentPlayer = PLAYER_1

            if (requestedNext < totalElements) {
                // assign next video to next-in-line player(i.e.2)
                assignMediaItem(PLAYER_2, videoUrls[requestedNext], false)
            }
        } else if (currentPlayer == PLAYER_NOT_ASSIGNED) {
//            if (requestedPosition == 0) {
            // no videos at index 0-1, hence, no previous video assignment

            // assign requested/current video to player 1
            currentPlaying = requestedPosition
            currentPlayer = PLAYER_1
            assignMediaItem(PLAYER_1, videoUrls[currentPlaying], playWhenReady)
            exoPlayer1?.volume = 1f

            if (requestedNext < totalElements) {
                // assign next video to next player, i.e.2
                assignMediaItem(PLAYER_2, videoUrls[requestedNext], false)
            }
        }
    }

    fun getPreviousPlayer(): SimpleExoPlayer? {
        return when (currentPlayer) {
            PLAYER_1 -> exoPlayer3
            PLAYER_2 -> exoPlayer1
            else -> exoPlayer2
        }
    }

    fun getCurrentPlayer(): SimpleExoPlayer? {
        return when (currentPlayer) {
            PLAYER_1 -> exoPlayer1
            PLAYER_2 -> exoPlayer2
            else -> exoPlayer3
        }
    }

    fun getNextPlayer(): SimpleExoPlayer? {
        return when (currentPlayer) {
            PLAYER_1 -> exoPlayer2
            PLAYER_2 -> exoPlayer3
            else -> exoPlayer1
        }
    }

    fun pause() {
        pausePlayer(exoPlayer1)
        pausePlayer(exoPlayer2)
        pausePlayer(exoPlayer3)
    }

    fun resumeCurrentPlayer() {
//        getCurrentPlayer()?.seekTo(0)
        playPlayer(getCurrentPlayer())
    }

    fun resume() {
        playPlayer(exoPlayer1)
        playPlayer(exoPlayer2)
        playPlayer(exoPlayer3)
    }

    fun isPlaying(): Boolean {
        return getCurrentPlayer()!!.isPlaying
    }

    fun stop() {
        stopPlayer(exoPlayer1)
        stopPlayer(exoPlayer2)
        stopPlayer(exoPlayer3)
    }

    fun release() {
        releasePlayer(exoPlayer1)
        releasePlayer(exoPlayer2)
        releasePlayer(exoPlayer3)
    }

    fun reset() {
        stop()
        clear()
        currentPlayer = PLAYER_NOT_ASSIGNED
    }

    fun resetAndRelease() {
        reset()
        release()

        werePlayersReleased = true
    }
    /** Multi-Players implementation ends here *////////////////////////////////////////////////////
}