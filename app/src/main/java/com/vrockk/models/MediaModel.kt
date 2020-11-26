package com.vrockk.models

abstract class MediaModel {
    companion object {
        const val MEDIA_STATE_UNPREPARED = 12
        const val MEDIA_STATE_PREPARING = 13
        const val MEDIA_STATE_PREPARED = 14
//        const val MEDIA_STATE_PREPARED_AND_NOT_PLAYING = 15
    }
    var mediaState = MEDIA_STATE_UNPREPARED
    var isPlayRequested: Boolean = false
    var playingIndex: Int = 0
    var mute: Boolean = true
}