package com.vrockk.player.interfaces

import java.io.File

interface OnDownloadStateListener {
    fun onDownloadStart(videoUrl: String, file: File, fileLength: Int)
    
    fun onDownloadedChunk(videoUrl: String, file: File, percentDownloaded: Float)
    
    fun onDownloaded(videoUrl: String, file: File)

    fun onFailed(videoUrl: String, file: File)
}