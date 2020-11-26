package com.vrockk.player.cache

import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheEvictor
import com.google.android.exoplayer2.upstream.cache.CacheSpan
import java.util.*

open class FeedsCacheEvictor(fileCount: Int, currentCount: Int): CacheEvictor {
    companion object {
        fun compare(lhs: CacheSpan, rhs: CacheSpan): Int {
            val lastTouchTimestampDelta = lhs.lastTouchTimestamp - rhs.lastTouchTimestamp
            if (lastTouchTimestampDelta == 0L) {
                return lhs.compareTo(rhs)
            }
            return if (lhs.lastTouchTimestamp < rhs.lastTouchTimestamp) -1 else 1
        }
    }
    private var fileCount: Int = 0
    private var leastRecentlyUsed: TreeSet<CacheSpan>? = null
    private var currentCount: Int = 0

    init {
        this.fileCount = fileCount
        this.leastRecentlyUsed = TreeSet { o1, o2 -> compare(o1!!, o2!!) }
        this.currentCount = currentCount
    }

//    fun FeedsEvictor(fileCount: Int) {
//        this.fileCount = fileCount
//        this.leastRecentlyUsed = TreeSet()
//    }

    override fun requiresCacheSpanTouches(): Boolean {
        return true
    }

    override fun onCacheInitialized() {
        // Do nothing.
    }

    override fun onStartFile(cache: Cache, key: String, position: Long, length: Long) {
        evictCache(cache)
    }

    override fun onSpanAdded(cache: Cache, span: CacheSpan) {
        leastRecentlyUsed!!.add(span)
        currentCount += 1
        evictCache(cache)
    }

    override fun onSpanRemoved(cache: Cache, span: CacheSpan) {
        leastRecentlyUsed!!.remove(span)
        currentCount -= 1
    }

    override fun onSpanTouched(cache: Cache, oldSpan: CacheSpan, newSpan: CacheSpan) {
        onSpanRemoved(cache, oldSpan)
        onSpanAdded(cache, newSpan)
    }

    open fun evictCache(cache: Cache) {
        while (currentCount + 1 > fileCount && !leastRecentlyUsed!!.isEmpty()) {
            cache.removeSpan(leastRecentlyUsed!!.first())
        }
    }
}