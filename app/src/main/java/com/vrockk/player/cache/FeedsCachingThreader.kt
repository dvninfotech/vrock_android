package com.vrockk.player.cache

class FeedsCachingThreader(val videoUrls: ArrayList<String>) {
    fun startCaching() {
        for (videoUrl in videoUrls)
            CachingThread(videoUrl).start()
    }
}