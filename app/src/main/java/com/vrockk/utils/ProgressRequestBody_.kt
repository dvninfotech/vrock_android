package com.vrockk.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.IOException
import java.io.InputStream
import java.lang.ref.WeakReference


class ProgressRequestBody_(
    context: Context?,
    uploadInfo: UploadInfo,
    listener: ProgressCallback
) :
    RequestBody() {
    interface ProgressCallback {
        fun onProgress(progress: Long, total: Long)
    }

    class UploadInfo {
        //Content uri for the file
        var contentUri: Uri? = null

        // File size in bytes
        var contentLength: Long = 0
    }

    private val mContextRef: WeakReference<Context>
    private val mUploadInfo: UploadInfo
    private val mListener: ProgressCallback
    override fun contentType(): MediaType? {
        // NOTE: We are posting the upload as binary data so we don't need the true mimeType
        return MediaType.parse("application/octet-stream")
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val fileLength = mUploadInfo.contentLength
        val buffer =
            ByteArray(UPLOAD_PROGRESS_BUFFER_SIZE)
        val `in`: InputStream = `in`()
        var uploaded: Long = 0
        try {
            var read: Int = 0
            while (`in`.read(buffer).also({ read = it }) != -1) {
                mListener.onProgress(uploaded, fileLength)
                uploaded += read.toLong()
                sink.write(buffer, 0, read)
            }
        } finally {
            `in`.close()
        }
    }

    /**
     * WARNING: You must override this function and return the file size or you will get errors
     */
    @Throws(IOException::class)
    override fun contentLength(): Long {
        return mUploadInfo.contentLength
    }

    @Throws(IOException::class)
    private fun `in`(): InputStream {
        var stream: InputStream? = null
        try {
            stream = contentResolver!!.openInputStream(mUploadInfo.contentUri!!)
        } catch (ex: Exception) {
            Log.e(
                LOG_TAG,
                "Error getting input stream for upload",
                ex
            )
        }
        return stream!!
    }

    private val contentResolver: ContentResolver?
        private get() = if (mContextRef.get() != null) {
            mContextRef.get()!!.getContentResolver()
        } else null

    companion object {
        private val LOG_TAG = ProgressRequestBody_::class.java.simpleName
        private const val UPLOAD_PROGRESS_BUFFER_SIZE = 8192
    }

    init {
        mContextRef = WeakReference(context!!)
        mUploadInfo = uploadInfo
        mListener = listener
    }
}