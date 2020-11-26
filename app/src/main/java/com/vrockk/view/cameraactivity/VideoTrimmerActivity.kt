package com.vrockk.view.cameraactivity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.vrockk.R
import com.vrockk.base.BaseActivity
import com.vrockk.editor.utils
import kotlinx.android.synthetic.main.activity_video_trimmer.*
import life.knowledge4.videotrimmer.interfaces.OnTrimVideoListener
import java.io.File
import java.util.concurrent.TimeUnit

class VideoTrimmerActivity : BaseActivity(), OnTrimVideoListener {
    private var STREAM_URL_MP4_VOD_LONG = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_trimmer)

        STREAM_URL_MP4_VOD_LONG = intent.getStringExtra("videoUri")!!

        if (timeLine == null) {
            showToast("Unable to initialise trimming")
            finish()
        }

        timeLine.setOnTrimVideoListener(this)

        if(File(STREAM_URL_MP4_VOD_LONG).length() > 0){
            val milliSec = utils.getVideoDuration(this, File(STREAM_URL_MP4_VOD_LONG))
            val secs = TimeUnit.MILLISECONDS.toSeconds(milliSec)

            if(secs > 180)
                timeLine.setMaxDuration(180)
            else
                timeLine.setMaxDuration(secs.toInt())
            timeLine.setVideoURI(Uri.parse(STREAM_URL_MP4_VOD_LONG))
        }
    }

    override fun getResult(uri: Uri?) {
        if(uri != null){
            startActivity(
                Intent(this, PreviewVideoActivity::class.java).putExtra("videoFile",uri.toString())
            )
            finish()
        }
    }

    override fun cancelAction() {
        finish()
    }
}