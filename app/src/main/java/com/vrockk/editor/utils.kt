package com.vrockk.editor

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.TypedValue
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.vrockk.utils.Constant
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


object utils {


    fun isVideoHaveAudioTrack(path: String): Boolean {
        var audioTrack = false

        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        val hasAudioStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)
        audioTrack = hasAudioStr == "yes"

        return audioTrack
    }

    fun createVideoFile(context: Context): File {
        val timeStamp: String = SimpleDateFormat(Constant.DATE_FORMAT, Locale.getDefault()).format(
            Date()
        )
        val imageFileName: String = Constant.APP_NAME + timeStamp + "_"
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        if (storageDir?.exists() == true) storageDir.mkdirs()
        return File.createTempFile(imageFileName, Constant.VIDEO_FORMAT, storageDir)
    }

    fun createAudioFile(context: Context): File {
        val timeStamp: String = SimpleDateFormat(Constant.DATE_FORMAT, Locale.getDefault()).format(
            Date()
        )
        val imageFileName: String = Constant.APP_NAME + timeStamp + "_"
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        if (storageDir?.exists() == true) storageDir.mkdirs()
        return File.createTempFile(imageFileName, Constant.AUDIO_FORMAT, storageDir)
    }

    fun createImageFile(context: Context): File {
        val timeStamp: String = SimpleDateFormat(Constant.DATE_FORMAT, Locale.getDefault()).format(
            Date()
        )
        val imageFileName: String = Constant.APP_NAME + timeStamp + "_"
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_DCIM)
        if (storageDir?.exists() == true) storageDir.mkdirs()
        return File.createTempFile(imageFileName, Constant.DATE_FORMAT, storageDir)
    }

    fun createAudioFolder(context: Context): File {
        val timeStamp: String = SimpleDateFormat(Constant.DATE_FORMAT, Locale.getDefault()).format(
            Date()
        )
        val imageFileName: String = Constant.APP_NAME + timeStamp + "_"
        val storageDir: File? = context.getExternalFilesDir("Local_Songs")
        if (storageDir?.exists() == true) storageDir.mkdirs()
        return File.createTempFile(imageFileName, Constant.AUDIO_FORMAT, storageDir)
    }

    fun createCroppedAudioFolder(context: Context): File {
        val timeStamp: String = SimpleDateFormat(Constant.DATE_FORMAT, Locale.getDefault()).format(
            Date()
        )
        val imageFileName: String = Constant.APP_NAME + timeStamp + "_"
        val storageDir: File? = context.getExternalFilesDir("Cropped")
        if (storageDir?.exists() == true) storageDir.mkdirs()
        return File.createTempFile(imageFileName, Constant.AUDIO_FORMAT, storageDir)
    }
    fun deleteFolder(context: Context): File{
        return context.getExternalFilesDir("Local_Songs")!!
    }

    fun getVideoDuration(context: Context, file: File): Long{
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, Uri.fromFile(file))
        val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val timeInMillis = time.toLong()
        retriever.release()
        return timeInMillis
    }

    fun getDimensionInPixel(context: Context, dp: Int): Int {
        return TypedValue.applyDimension(
            0,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }



}