package com.vrockk.api

interface UploadCallbacks {
    fun onProgressUpdate(percentage: Int)
    fun onError()
    fun onFinish()
}