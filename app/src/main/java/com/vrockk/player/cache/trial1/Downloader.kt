package com.vrockk.player.cache.trial1

import android.util.Log
import com.vrockk.player.interfaces.OnDownloadStateListener
import java.io.File
import java.util.*

class Downloader(private val directory: File) : OnDownloadStateListener {
    companion object {
        const val TAG = "Downloader"
        const val CAPACITY = 100

        private var downloadThreads: TreeMap<String, DownloadThread>? = null

        private fun compare(lhs: String, rhs: String): Int {
            if (downloadThreads == null)
                return 0

            if (downloadThreads!![lhs] == null || downloadThreads!![rhs] == null)
                return 0

            if (downloadThreads!![lhs]!!.CREATED_AT - downloadThreads!![lhs]!!.CREATED_AT == 0L) {
                return 0
            }

            return if (downloadThreads!![lhs]!!.CREATED_AT < downloadThreads!![rhs]!!.CREATED_AT) -1 else 1
        }
    }

    init {
        downloadThreads = TreeMap() /*{o1, o2 -> compare(o1!!, o2!!)}*/
    }

    private val downloadedFilesMap = HashMap<String, String>()
    private var downloadingUrl: String? = null

    fun hasCache(videoUrl: String): Boolean {
        return downloadedFilesMap.containsKey(videoUrl)
    }

    fun getCachePath(videoUrl: String): String? {
        return downloadedFilesMap[videoUrl]
    }

    /** returns true if already downloaded */
    fun changeDownloadPriorities(
        previousVideoUrl: String?,
        currentVideoUrl: String,
        nextVideoUrl: String?
    ): Boolean {
        var fileForUrlExists = false

        if (downloadingUrl != null) {
            downloadThreads!![downloadingUrl!!]?.priority = Thread.NORM_PRIORITY
        }

        if (!downloadedFilesMap.containsKey(currentVideoUrl)) {
            if (downloadThreads!!.containsKey(currentVideoUrl))
                downloadThreads!![currentVideoUrl]?.priority = Thread.MAX_PRIORITY
            else {
                val currentUrlDownloadingThread = createDownloadingThread(currentVideoUrl, Thread.MAX_PRIORITY)
                downloadThreads!![currentVideoUrl] = currentUrlDownloadingThread
                downloadingUrl = currentVideoUrl
                currentUrlDownloadingThread.start()
            }
        } else
            fileForUrlExists = true

        if (nextVideoUrl != null) {
            downloadOrChangePriority(nextVideoUrl)
        }

        if (previousVideoUrl != null) {
            downloadOrChangePriority(previousVideoUrl)
        }

        return fileForUrlExists
    }

    fun downloadAll(videoUrls: ArrayList<String>) {
        for (videoUrl in videoUrls)
            download(videoUrl, Thread.MAX_PRIORITY)
    }

    private fun createDownloadingThread(videoUrl: String, priority: Int): DownloadThread {
        if (downloadThreads!!.size == CAPACITY) {
            Log.e(TAG, "createDownloadingThread: threads have reached capacity")
            try {
                val downloadThread = downloadThreads!!.remove(downloadThreads!!.firstEntry()?.key)
                Log.i(TAG, "createDownloadingThread: removed thread = ${downloadThread.toString()}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val downloadThread = DownloadThread(videoUrl, directory, this)
        downloadThread.priority = priority
        return downloadThread
    }

    private fun downloadOrChangePriority(videoUrl: String) {
        if (downloadedFilesMap.containsKey(videoUrl))
            lowerPriority(videoUrl)
        else
            download(videoUrl, Thread.NORM_PRIORITY)
    }

    private fun lowerPriority(videoUrl: String) {
        if (downloadThreads!!.containsKey(videoUrl))
            downloadThreads!![videoUrl]?.priority = Thread.NORM_PRIORITY
    }

    private fun download(videoUrl: String, priority: Int) {
        if (downloadedFilesMap.containsKey(videoUrl))
            return

        val downloadThread = createDownloadingThread(videoUrl, priority)
        downloadThreads!![videoUrl] = downloadThread
        downloadThread.start()
    }

    override fun onDownloadStart(videoUrl: String, file: File, fileLength: Int) {
        Log.i(TAG, "onDownloadStart: \n$fileLength \n${file.absolutePath} \n$videoUrl")
    }

    override fun onDownloadedChunk(videoUrl: String, file: File, percentDownloaded: Float) {
        Log.i(TAG, "onDownloadedChunk: \n$percentDownloaded \n${file.length()} \n$videoUrl")
    }

    override fun onDownloaded(videoUrl: String, file: File) {
        Log.i(TAG, "onDownloaded: \n${file.absolutePath} \n$videoUrl")
        downloadedFilesMap[videoUrl] = file.absolutePath
        downloadThreads!!.remove(videoUrl)
    }

    override fun onFailed(videoUrl: String, file: File) {
        Log.e(TAG, "onFailed: \n${file.absolutePath} \n$videoUrl")
        downloadThreads!!.remove(videoUrl)
    }
}