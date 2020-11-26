package com.vrockk.view.cameraactivity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.bumptech.glide.Glide
import com.daasuu.gpuv.egl.filter.GlFilter
import com.daasuu.gpuv.player.GPUPlayerView
import com.facebook.login.LoginManager
/*import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException*/
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.custom_view.MovieWrapperView
import com.vrockk.custom_view.PhotoEditorView
import com.vrockk.custom_view.PlayerTimer
import com.vrockk.editor.OnPhotoEditorListener
import com.vrockk.editor.PhotoEditor
import com.vrockk.editor.ViewType
import com.vrockk.filter.FilterAdjuster
import com.vrockk.utils.DimensionData
import com.vrockk.utils.PreferenceHelper
import com.vrockk.utils.SaveSettings
import com.vrockk.utils.Utils.getScaledDimension
import com.vrockk.view.create_video.VideoPostActivity
import com.vrockk.view.fragment.StickerBSFragment
import com.vrockk.view.home.HomeFragment
import com.vrockk.view.login.LoginActivity
import kotlinx.android.synthetic.main.activity_preview_video.*
import kotlinx.android.synthetic.main.my_custom_alert.*
import java.io.File
import java.io.IOException
import java.util.*

class PreviewVideoActivity : AppCompatActivity(), StickerBSFragment.StickerListener,
    OnPhotoEditorListener {

    private var STREAM_URL_MP4_VOD_LONG = ""
    private var gpuPlayerView: GPUPlayerView? = null
    private var player: SimpleExoPlayer? = null
    private val button: Button? = null
    private val timeSeekBar: SeekBar? = null
    private val filterSeekBar: SeekBar? = null
    private var playerTimer: PlayerTimer? = null
    private val filter: GlFilter? = null
    private val adjuster: FilterAdjuster? = null
    private var mPhotoEditor: PhotoEditor? = null
    private val mPhotoEditorView: PhotoEditorView? = null
    private var mStickerBSFragment: StickerBSFragment? = null
    private var newCommand = arrayOf("")
    private var originalDisplayWidth = 0
    private var originalDisplayHeight = 0
    private var newCanvasWidth = 0
    private  var newCanvasHeight = 0
    private var DRAW_CANVASW = 0
    private var DRAW_CANVASH = 0
    private var exeCmd: ArrayList<String>? = null
    // var fFmpeg: FFmpeg? = null
    private val videoPath = ""
    private var savedImagePath = ""
    private var songId = ""
    private var songName = ""
    private var isUpload = false
    private var selectedSong = ""
    private var progressDialog: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_video)
        STREAM_URL_MP4_VOD_LONG = intent.getStringExtra("videoFile")!!
        setupVideoOnView()
        if(intent.hasExtra("songId"))
           songId = intent.getStringExtra("songId")!!

        if(intent.hasExtra("isUpload")){
            isUpload = intent.getBooleanExtra("isUpload",false)
            songName = intent.getStringExtra("songName")?:""
            selectedSong = intent.getStringExtra("selectedSong")?:""
        }

        Glide.with(this).load(R.drawable.trans).centerCrop().into(ivImage.source)
        exeCmd = ArrayList()
        // fFmpeg = FFmpeg.getInstance(this)
        mStickerBSFragment = StickerBSFragment()
        progressDialog = ProgressDialog(this)
        mPhotoEditor = PhotoEditor.Builder(this, ivImage)
            .setPinchTextScalable(true) // set flag to make text scalable when pinch
            .setDeleteView(imgDelete) //.setDefaultTextTypeface(mTextRobotoTf)
            //.setDefaultEmojiTypeface(mEmojiTypeFace)
            .build() // build photo editor sdk
        mPhotoEditor!!.setOnPhotoEditorListener(this)
        mStickerBSFragment!!.setStickerListener(this)

        clicks()
    }

    fun executeCommand(
        command: Array<String>,
        absolutePath: String?
    ) {

        progressDialog!!.setTitle("Processing")
        // progressDialog!!.setMessage("Starting")
        if(progressDialog != null)
            progressDialog!!.show()
        FFmpeg.cancel()
        val executionId = FFmpeg.executeAsync(command) { executionId, returnCode ->
            if (returnCode == Config.RETURN_CODE_SUCCESS) {


                Log.i(
                    Config.TAG,
                    "Async command execution completed successfully."
                )

                deleteImage(savedImagePath)
                progressDialog!!.dismiss()
                //  Toast.makeText(applicationContext, "Sucess", Toast.LENGTH_SHORT).show()
                val i = Intent(this@PreviewVideoActivity, VideoPostActivity::class.java)
                i.putExtra("DATA", absolutePath)
                i.putExtra("songId", songId)
                i.putExtra("isUpload",isUpload)
                i.putExtra("selectedSong",selectedSong)
                i.putExtra("songName",songName)
                i.putExtra("type", "")
                startActivity(i)
                finish()

            } else if (returnCode == Config.RETURN_CODE_CANCEL) {
                Log.i(
                    Config.TAG,
                    "Async command execution cancelled by user."
                )
            }else if (returnCode == Config.RETURN_CODE_CANCEL) {
                Log.i(
                    Config.TAG,
                    "Async command execution cancelled by user."
                )
                progressDialog!!.dismiss()
            } else {
                progressDialog!!.dismiss()
                Log.i(
                    Config.TAG,
                    java.lang.String.format(
                        "Async command execution failed with rc"                )
                )
            }
        }
    }
    private fun setupVideoOnView() {

        try{
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(STREAM_URL_MP4_VOD_LONG)
            val metaRotation =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
            val rotation = metaRotation?.toInt() ?: 0
            if (rotation == 90 || rotation == 270) {
                DRAW_CANVASH =
                    Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
                DRAW_CANVASW =
                    Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
            } else {
                DRAW_CANVASW =
                    Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
                DRAW_CANVASH =
                    Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
            }
            setCanvasAspectRatio()

            vv_postVideo.layoutParams.width = newCanvasWidth
            vv_postVideo.layoutParams.height = newCanvasHeight

            ivImage.layoutParams.width = newCanvasWidth
            ivImage.layoutParams.height = newCanvasHeight

            Log.d(">>", "width>> " + newCanvasWidth + "height>> " + newCanvasHeight + " rotation >> " + rotation)
        }catch (e:Exception){
            Log.e("call","exception: "+e.toString())
        }


    }

    private fun clicks() {
        txtStickerClick.setOnClickListener {
            mStickerBSFragment!!.show(supportFragmentManager, mStickerBSFragment!!.tag)
        }
        txtSaveClick.setOnClickListener {
            saveImage()
        }
    }

    override fun onStickerClick(bitmap: Bitmap?) {
        //mPhotoEditor.setBrushDrawingMode(false)
        //binding.imgDraw.setBackgroundColor(ContextCompat.getColor(this, R.color.black_trasp))
        mPhotoEditor!!.addImage(bitmap)
    }

    override fun onResume() {
        super.onResume()
        setUpSimpleExoPlayer()
        setUoGlPlayerView()
        setUpTimer()
    }

    fun initPlayer(){
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                2000,
                DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                1500,
                1000
            )
            .createDefaultLoadControl()
        player = SimpleExoPlayer.Builder(this)
            .setLoadControl(loadControl)
            .build()
    }

    private fun setUpSimpleExoPlayer() {

        if(player == null)
            initPlayer()

        player!!.apply {
            val userAgent = Util.getUserAgent(this@PreviewVideoActivity, getString(R.string.app_name))
            val mediaSource = ProgressiveMediaSource.Factory(
                DefaultDataSourceFactory(this@PreviewVideoActivity, userAgent),
                DefaultExtractorsFactory()
            ).createMediaSource(Uri.parse(STREAM_URL_MP4_VOD_LONG))
            prepare(mediaSource!!, true, false)
            player!!.playWhenReady = true
            repeatMode = Player.REPEAT_MODE_ALL
        }
        vv_postVideo.player = player
        vv_postVideo.setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)

    }

    private fun getDisplayWidth(): Int {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    private fun getDisplayHeight(): Int {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }
    private fun setCanvasAspectRatio() {

        try {
            originalDisplayHeight = getDisplayHeight()
            originalDisplayWidth = getDisplayWidth()
            val displayDiamenion: DimensionData = getScaledDimension(
                DimensionData(DRAW_CANVASW as Int, DRAW_CANVASH as Int),
                DimensionData(originalDisplayWidth, originalDisplayHeight)
            )
            newCanvasWidth = displayDiamenion.width
            newCanvasHeight = displayDiamenion.height
        }catch (e:Exception)
        {
            Log.e("call","exception: "+e.toString())
        }

    }

    private fun setUoGlPlayerView() {
    }

    private fun setUpTimer() {
        playerTimer = PlayerTimer()
        playerTimer!!.setCallback(object : PlayerTimer.Callback{
            override fun onTick(timeMillis: Long) {
                if(player != null){
                    val position = player!!.currentPosition
                    val duration = player!!.duration

                    if (duration <= 0) return



                    if(position/1000 == duration/1000){
                        Log.e("duration" , "timeMillis ${position/1000} ")
                        Log.e("duration" , "duration ${position/1000} ")
                        player!!.playWhenReady = false
                        player!!.stop()
                        // player!!.release()
                        setUpSimpleExoPlayer()
                        //setUoGlPlayerView()
                        setUpTimer()
                    }
                }

                /*timeSeekBar!!.max = duration.toInt() / 1000
                timeSeekBar.progress = position.toInt() / 1000*/
            }
        })
        playerTimer!!.start()
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onPause() {
        super.onPause()
        if(progressDialog != null && progressDialog!!.isShowing)
            progressDialog!!.dismiss()

        releasePlayer()
        if (playerTimer != null) {
            playerTimer!!.stop()
            playerTimer!!.removeMessages(0)
        }
    }
    private fun releasePlayer() {

        if(player != null){
            player!!.stop()
            player!!.release()
            player = null
        }

    }

    override fun onEditTextChangeListener(
        rootView: View?,
        text: String?,
        colorCode: Int,
        pos: Int
    ) {

    }

    override fun onStartViewChangeListener(viewType: ViewType?) {

    }

    override fun onRemoveViewListener(viewType: ViewType?, numberOfAddedViews: Int) {

    }

    override fun onAddViewListener(viewType: ViewType?, numberOfAddedViews: Int) {

    }

    override fun onStopViewChangeListener(viewType: ViewType?) {

    }

    @SuppressLint("MissingPermission")
    private fun saveImage() {
        val file = File(
            Environment.getExternalStorageDirectory()
                .toString() + File.separator + ""
                    + System.currentTimeMillis() + ".png"
        )
        try {
            file.createNewFile()
            val saveSettings: SaveSettings = SaveSettings.Builder()
                .setClearViewsEnabled(true)
                .setTransparencyEnabled(false)
                .build()
            mPhotoEditor!!.saveAsFile(file.absolutePath, saveSettings, object : PhotoEditor.OnSaveListener {
                override fun onSuccess(@NonNull imagePath: String) {
                    savedImagePath = imagePath
                    Log.d("imagePath>>", imagePath)
                    Log.d(
                        "imagePath2>>",
                        Uri.fromFile(File(imagePath)).toString()
                    )
                    ivImage.source.setImageURI(Uri.fromFile(File(imagePath)))

                    //Commented by Azam
                    //applayWaterMark()

                    //Added by Azam
                    val i = Intent(this@PreviewVideoActivity, VideoPostActivity::class.java)
                    i.putExtra("DATA", STREAM_URL_MP4_VOD_LONG)
                    i.putExtra("songId", songId)
                    i.putExtra("isUpload",isUpload)
                    i.putExtra("selectedSong",selectedSong)
                    i.putExtra("songName",songName)
                    i.putExtra("type", "")
                    i.putExtra("videoFile", STREAM_URL_MP4_VOD_LONG)
                    i.putExtra("savedImagePath", savedImagePath)
                    startActivity(i)
                    finish()
                }

                override fun onFailure(exception: Exception) {
                    Toast.makeText(
                        this@PreviewVideoActivity,
                        "Saving Failed...",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun deleteImage(path: String) {
        val fDelete = File(path)
        /*if (fDelete.exists()) {
            if (fDelete.delete()) {
                MediaScannerConnection.scanFile(this, arrayOf(Environment.getExternalStorageDirectory().toString()), null) { path, uri ->
                    Log.d("debug", "DONE")
                    deleteImage(STREAM_URL_MP4_VOD_LONG)
                }
            }
        }*/
    }
    private fun applayWaterMark() {

        val output = File(
            Environment.getExternalStorageDirectory()
                .toString() + File.separator + ""
                    + System.currentTimeMillis() + ".mp4"
        )
        try {
            output.createNewFile()
            exeCmd!!.add("-y")
            exeCmd!!.add("-i")
            exeCmd!!.add(STREAM_URL_MP4_VOD_LONG)
            //            exeCmd.add("-framerate 30000/1001 -loop 1");
            exeCmd!!.add("-i")
            exeCmd!!.add(savedImagePath)
            exeCmd!!.add("-filter_complex")
            //            exeCmd.add("-strict");
//            exeCmd.add("-2");
//            exeCmd.add("-map");
//            exeCmd.add("[1:v]scale=(iw+(iw/2)):(ih+(ih/2))[ovrl];[0:v][ovrl]overlay=x=(main_w-overlay_w)/2:y=(main_h-overlay_h)/2");
//            exeCmd.add("[1:v]scale=720:1280:1823[ovrl];[0:v][ovrl]overlay=x=0:y=0");
            exeCmd!!.add("[1:v]scale=$DRAW_CANVASW:$DRAW_CANVASH[ovrl];[0:v][ovrl]overlay=x=0:y=0")
            //  exeCmd!!.add("-c:v") // hide
            exeCmd!!.add("-codec:a") // manpreet
            // exeCmd!!.add("libx264")  // hide
            exeCmd!!.add("copy")  // manpreet
            // exeCmd!!.add("-preset")  // hide
            //exeCmd!!.add("ultrafast") // hide
            exeCmd!!.add(output.absolutePath)
            newCommand = Array(exeCmd!!.size){""}
            for (j in exeCmd!!.indices) {
                newCommand[j] = exeCmd!![j]
            }
            for (k in newCommand.indices) {
                Log.d("CMD==>>", newCommand[k] + "")
            }

            //    newCommand = new String[]{"-i", videoPath, "-i", imagePath, "-preset", "ultrafast", "-filter_complex", "[1:v]scale=2*trunc(" + (width / 2) + "):2*trunc(" + (height/ 2) + ") [ovrl], [0:v][ovrl]overlay=0:0" , output.getAbsolutePath()};

            //newCommand =  arrayOf("-i", STREAM_URL_MP4_VOD_LONG, "-i", savedImagePath, "-preset", "ultrafast", "-filter_complex", "[1:v]scale=2*trunc(" + (width / 2) + "):2*trunc(" + (height/ 2) + ") [ovrl], [0:v][ovrl]overlay=0:0" , output.absolutePath)
            executeCommand(newCommand, output.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        showPopup()
    }


    fun showPopup()
    {

        val myCustomDlg = Dialog(this)
        myCustomDlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        myCustomDlg.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        myCustomDlg.setContentView(R.layout.my_custom_alert)
        myCustomDlg.tvAlertMessage.text = resources.getString(R.string.are_you_sure_you_want_to_discard_video)
        myCustomDlg.tvTitleCustom.text = resources.getString(R.string.alert)
        myCustomDlg.positiveBtn.text = resources.getString(R.string.yes)
        myCustomDlg.noBtn.text = resources.getString(R.string.no)
        myCustomDlg.show()

        myCustomDlg.noBtn.setOnClickListener{
            myCustomDlg.dismiss()
        }

        myCustomDlg.positiveBtn.setOnClickListener {
            myCustomDlg.dismiss()
            finish()
        }

//        AlertDialog.Builder(this).setTitle(resources.getString(R.string.confirm))
//            .setMessage(getString(R.string.are_you_sure_you_want_to_discard_video))
//            .setPositiveButton(getString(R.string.ok), object : DialogInterface.OnClickListener {
//                override fun onClick(p0: DialogInterface?, p1: Int) {
//                    finish()
//                }
//            })
//            .setNeutralButton(getString(R.string.no), null)
//            .show()
    }

}