package com.vrockk.player.cache.trial1

import com.vrockk.player.interfaces.OnDownloadStateListener
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadThread(val videoUrl: String, private val directory: File, private val onDownloadStateListener: OnDownloadStateListener): Thread() {
    val CREATED_AT = System.currentTimeMillis()

    private val buffer = byteArrayOf(1024.toByte())
    private var waiting = false

    fun isWaiting(): Boolean {
        return waiting
    }

    override fun run() {
        val destinationFile = File(directory,
            videoUrl.substring(videoUrl.lastIndexOf('/') + 1))
        try {
            val url = URL(videoUrl);
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.doOutput = true
            connection.connect()

            val streamLength = connection.contentLength
            val inputStream = connection.inputStream

            val outputStream = FileOutputStream(destinationFile)

            onDownloadStateListener.onDownloadStart(videoUrl, destinationFile, streamLength)

            var length = inputStream.read(buffer)
            while (length > 0) {
                if (!isInterrupted) {
                    waiting = false
                    outputStream.write(buffer, 0, length)
                    onDownloadStateListener.onDownloadedChunk(videoUrl, destinationFile,
                        (length * 100/streamLength).toFloat()
                    )
                    length = inputStream.read(buffer)
                } else
                    waiting = true
            }

            outputStream.close()
            inputStream.close()
            connection.disconnect()

            onDownloadStateListener.onDownloaded(videoUrl, destinationFile)
        } catch (e: Exception) {
            e.printStackTrace()
            onDownloadStateListener.onFailed(videoUrl, destinationFile)
        }
    }

    override fun toString(): String {
        return "DownloadThread - $CREATED_AT - $videoUrl"
    }
}