package com.vrockk.player.cache

import java.io.File

class FileComparator: Comparator<File> {
    override fun compare(o1: File?, o2: File?): Int {
        try {
            return o1!!.lastModified().compareTo(o2!!.lastModified())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }
}