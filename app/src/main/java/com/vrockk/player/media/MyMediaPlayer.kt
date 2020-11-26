package com.vrockk.player.media

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.vrockk.VrockkApplication
import com.vrockk.player.cache.trial1.Downloader

class MyMediaPlayer(val context: Context) {
    companion object {
        const val TAG = "MyMediaPlayer"

        private val instancesMap = HashMap<String, MyMediaPlayer>()

        const val PLAYER_NOT_ASSIGNED = 0
        const val PLAYER_1 = 1
        const val PLAYER_2 = 2
        const val PLAYER_3 = 3

        fun getInstance(tag: String): MyMediaPlayer? {
            return instancesMap[tag]
        }

        fun createInstance(tag: String, context: Context): MyMediaPlayer {
            val myPlayer = MyMediaPlayer(context)
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
    private var mediaPlayer1: MediaPlayer? = null
    private var mediaPlayer2: MediaPlayer? = null
    private var mediaPlayer3: MediaPlayer? = null
    private var currentPlayer = PLAYER_NOT_ASSIGNED

    private var downloader: Downloader? = null

    init {
        downloader = Downloader(context.getDir(VrockkApplication.appName, Context.MODE_PRIVATE))
        initPlayers()
    }

    /** Player tools implementation begins here *///////////////////////////////////////////////////
    fun appendToUrls(videoUrls: ArrayList<String>) {
        this.videoUrls.addAll(videoUrls)
        totalElements = this.videoUrls.size

        downloader?.downloadAll(videoUrls)
    }

    fun getCurrentPlaying(): Int {
        return currentPlaying
    }

    private fun assignMediaItem(
        mediaPlayer: MediaPlayer?,
        videoUrl: String
    ) {
        mediaPlayer?.stop()
        mediaPlayer?.reset()

        if (downloader!!.hasCache(videoUrl))
            mediaPlayer?.setDataSource(context, Uri.parse(downloader?.getCachePath(videoUrl)))
        else
            mediaPlayer?.setDataSource(context, Uri.parse(videoUrl))
        mediaPlayer?.prepareAsync()
    }

    private fun pausePlayer(mediaPlayer: MediaPlayer?) {
        try {
            mediaPlayer?.pause()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playPlayer(mediaPlayer: MediaPlayer?) {
        try {
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopPlayer(mediaPlayer: MediaPlayer?) {
        try {
            mediaPlayer?.stop()
//            mediaPlayer?.reset()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releasePlayer(mediaPlayer: MediaPlayer?) {
        try {
            mediaPlayer?.release()
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
    private fun initPlayers() {
        if (mediaPlayer1 == null) {
            mediaPlayer1 = MediaPlayer()
            mediaPlayer1?.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            mediaPlayer1?.isLooping = true
            mediaPlayer1?.setOnPreparedListener {
                it.start()
                if (currentPlayer != PLAYER_1)
                    it.pause()
            }
        }
        if (mediaPlayer2 == null) {
            mediaPlayer2 = MediaPlayer()
            mediaPlayer2?.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            mediaPlayer2?.isLooping = true
            mediaPlayer2?.setOnPreparedListener {
                it.start()
                if (currentPlayer != PLAYER_2)
                    it.pause()
            }
        }
        if (mediaPlayer3 == null) {
            mediaPlayer3 = MediaPlayer()
            mediaPlayer3?.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            mediaPlayer3?.isLooping = true
            mediaPlayer3?.setOnPreparedListener {
                it.start()
                if (currentPlayer != PLAYER_3)
                    it.pause()
            }
        }
    }

    fun changeItem(requestedPosition: Int) {
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

            if (requestedPosition == currentPrevious) {
                requestedPlayPreviousBy1(requestedPrevious, requestedPosition)
                return
            } else {
                Log.i(TAG, "prepare: requestedPosition < currentPrevious")
            }
        } else {
            if (requestedPosition > totalElements - 1)
                return // trying to play from index greater than max-index

            if (requestedPosition == currentNext) {
                requestedPlayNextBy1(requestedPosition, requestedNext)
                return
            } else {
                Log.i(TAG, "prepare: requestedPosition > currentNext")
            }
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
            pausePlayer(mediaPlayer1)

            // switch to player3 as it holds the previous video
            mediaPlayer3?.start()
            currentPlaying = requestedPosition
            currentPlayer = PLAYER_3

            if (requestedPrevious >= 0) {
                // assign previous video to previous-in-line player(i.e.2)
                assignMediaItem(mediaPlayer2, videoUrls[requestedPrevious])
            }
        } else if (currentPlayer == PLAYER_2) {
            /** we don't need to assign next to player 2 because it already points to the same,
            hence, we'll just turn it's volume off*/
            // assign next video to next-in-line player(i.e.2)
//            assignMediaItem(mediaPlayer2, videoUrls[currentPlaying + 1])

            pausePlayer(mediaPlayer2)

            // switch to player1 as it holds the previous video
            mediaPlayer1?.start()
            currentPlaying = requestedPosition
            currentPlayer = PLAYER_1

            if (requestedPrevious >= 0) {
                // assign previous video to previous-in-line player(i.e.3)
                assignMediaItem(mediaPlayer3, videoUrls[requestedPrevious])
            }
        } else if (currentPlayer == PLAYER_3) {
            /** we don't need to assign next to player 3 because it already points to the same,
            hence, we'll just turn it's volume off*/
            // assign next video to next-in-line player(i.e.3)
//            assignMediaItem(mediaPlayer3, videoUrls[currentPlaying + 1])

            pausePlayer(mediaPlayer3)

            // switch to player2 as it holds the previous video
            mediaPlayer2?.start()
            currentPlaying = requestedPosition
            currentPlayer = PLAYER_2

            if (requestedPrevious >= 0) {
                // assign previous video to previous-in-line player(i.e.1)
                assignMediaItem(mediaPlayer1, videoUrls[requestedPrevious])
            }
        } else if (currentPlayer == PLAYER_NOT_ASSIGNED) {
            Log.e(TAG, "requestedPlayPreviousBy1: currentPlayer == PLAYER_NOT_ASSIGNED")
        }
    }

    private fun requestedPlayNextBy1(
        requestedPosition: Int,
        requestedNext: Int
    ) {
        // order of players is ...231231232.....
        if (currentPlayer == PLAYER_1) {
            /** we don't need to assign previous to player 1 because it already points to the same,
            hence, we'll just turn it's volume off*/
            // assign previous video to previous-in-line player(i.e.1)
//            assignMediaItem(mediaPlayer1, videoUrls[currentPlaying - 1])

            pausePlayer(mediaPlayer1)

            // switch to player2 as it holds the next video
            mediaPlayer2?.start()
            currentPlaying = requestedPosition
            currentPlayer = PLAYER_2

            if (requestedNext < totalElements) {
                // assign next video to next-in-line player(i.e.3)
                assignMediaItem(mediaPlayer3, videoUrls[requestedNext])
            }
        } else if (currentPlayer == PLAYER_2) {
            /** we don't need to assign previous to player 2 because it already points to the same,
            hence, we'll just turn it's volume off*/
            // assign previous video to previous-in-line player(i.e.2)
//            assignMediaItem(mediaPlayer2, videoUrls[currentPlaying - 1])

            pausePlayer(mediaPlayer2)

            // switch to player3 as it holds the next video
            mediaPlayer3?.start()
            currentPlaying = requestedPosition
            currentPlayer = PLAYER_3

            if (requestedNext < totalElements) {
                // assign next video to next-in-line player(i.e.1)
                assignMediaItem(mediaPlayer1, videoUrls[requestedNext])
            }
        } else if (currentPlayer == PLAYER_3) {
            /** we don't need to assign previous to player 3 because it already points to the same,
            hence, we'll just turn it's volume off*/
            // assign previous video to previous-in-line player(i.e.3)
//            assignMediaItem(mediaPlayer3, videoUrls[currentPlaying - 1])

            pausePlayer(mediaPlayer3)

            // switch to player1 as it holds the next video
            mediaPlayer1?.start()
            currentPlaying = requestedPosition
            currentPlayer = PLAYER_1

            if (requestedNext < totalElements) {
                // assign next video to next-in-line player(i.e.2)
                assignMediaItem(mediaPlayer2, videoUrls[requestedNext])
            }
        } else if (currentPlayer == PLAYER_NOT_ASSIGNED) {
            if (requestedPosition == 0) {
                // no videos at index 0-1, hence, no previous video assignment

                // assign requested/current video to player 1
                currentPlaying = requestedPosition
                currentPlayer = PLAYER_1
                assignMediaItem(mediaPlayer1, videoUrls[currentPlaying])
                pausePlayer(mediaPlayer1)

                if (requestedNext < totalElements) {
                    // assign next video to next player, i.e.2
                    assignMediaItem(mediaPlayer2, videoUrls[requestedNext])
                }
            } else
                Log.e(
                    TAG,
                    "requestedPlayNextBy1: currentPlayer == PLAYER_NOT_ASSIGNED requestedPosition != 0"
                )
        }
    }

    fun getPreviousPlayer(): MediaPlayer? {
        return when (currentPlayer) {
            PLAYER_1 -> mediaPlayer3
            PLAYER_2 -> mediaPlayer1
            else -> mediaPlayer2
        }
    }

    fun getCurrentPlayer(): MediaPlayer? {
        return when (currentPlayer) {
            PLAYER_1 -> mediaPlayer1
            PLAYER_2 -> mediaPlayer2
            else -> mediaPlayer3
        }
    }

    fun getNextPlayer(): MediaPlayer? {
        return when (currentPlayer) {
            PLAYER_1 -> mediaPlayer2
            PLAYER_2 -> mediaPlayer3
            else -> mediaPlayer1
        }
    }

    fun pause() {
        pausePlayer(mediaPlayer1)
        pausePlayer(mediaPlayer2)
        pausePlayer(mediaPlayer3)
    }

    fun resumeCurrentPlayer() {
        playPlayer(getCurrentPlayer())
    }

    fun resume() {
        playPlayer(mediaPlayer1)
        playPlayer(mediaPlayer2)
        playPlayer(mediaPlayer3)
    }

    fun isPlaying(): Boolean {
        return getCurrentPlayer()!!.isPlaying
    }

    fun stop() {
        stopPlayer(mediaPlayer1)
        stopPlayer(mediaPlayer2)
        stopPlayer(mediaPlayer3)
    }

    fun release() {
        releasePlayer(mediaPlayer1)
        releasePlayer(mediaPlayer2)
        releasePlayer(mediaPlayer3)
    }

    fun reset() {
        stop()
        clear()
        currentPlayer = PLAYER_NOT_ASSIGNED
    }

    fun resetAndRelease() {
        reset()
        release()
    }
    /** Multi-Players implementation ends here *////////////////////////////////////////////////////
}