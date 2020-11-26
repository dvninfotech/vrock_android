package com.vrockk.view.cameraactivity

import VideoHandle.EpEditor
import VideoHandle.EpVideo
import VideoHandle.OnEditorListener
import android.Manifest
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.net.Uri
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.ankushgrover.hourglass.Hourglass
import com.daasuu.gpuv.camerarecorder.CameraRecordListener
import com.daasuu.gpuv.camerarecorder.GPUCameraRecorder
import com.daasuu.gpuv.camerarecorder.GPUCameraRecorderBuilder
import com.daasuu.gpuv.camerarecorder.LensFacing
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
//import com.google.android.gms.clearcut.ClearcutLogger
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.adapter.FilterViewAdapter
import com.vrockk.adapter.SongListAdapter
import com.vrockk.api.ApiResponse
import com.vrockk.api.Status
import com.vrockk.base.BaseActivity
import com.vrockk.custom_view.PhotoFilter
import com.vrockk.custom_view.SampleCameraGLView
import com.vrockk.editor.VideoEditor
import com.vrockk.editor.utils
import com.vrockk.filter.FilterType
import com.vrockk.interfaces.FFMpegCallback
import com.vrockk.interfaces.FilterListener
import com.vrockk.interfaces.OutputType
import com.vrockk.models.songs_list.DataItem
import com.vrockk.models.songs_list.SongsListModel
import com.vrockk.utils.Constant
import com.vrockk.utils.CountDownTimerPausable
import com.vrockk.utils.FileUtils
import com.vrockk.utils.Utils
import com.vrockk.viewmodels.viewmodels.CommonViewModel
import kotlinx.android.synthetic.main.activity_recordvideos.*
import kotlinx.android.synthetic.main.circuler_progress_bar.*
import kotlinx.coroutines.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.*
import java.lang.reflect.Field
import java.net.URL
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class RecordVideosActivity : BaseActivity(), FFMpegCallback, FilterListener {

    var timerStart: Boolean = false

    private var timerSecond: Int = 0

    private var songPathAfterDownload: String = ""
    private var songName: String = ""
    private var isUpload: Boolean = false
    private var isRecording: Boolean = false
    private var myVideoFile: File? = null
    private var sampleGLView: SampleCameraGLView? = null
    private var filepath: String? = null
    var tempFile: File? = null
    private var fileName: String = ""
    private var songId: String = ""
    private var toggleClick = false
    private var gpuCameraRecorder: GPUCameraRecorder? = null
    private var lensFacing = LensFacing.BACK
    private var cameraWidth = 1280
    private var cameraHeight = 720
    private var videoWidth = 720
    private var videoHeight = 1280
    private var position = 0
    var showTimer = false
    private var selectedPlayback: Float = 1.0F
    private var startTimeOfSong: Int = 0
    private var endTimeOfSong: Int = 0
    private var SECONDS_DELAY: Long = 15 // 20 SECS
    var TIMER_DELAY: Long = 3 // 20 SECS
    var PROGRESS_TIMER_COUNT: Long = 0 // 20 SECS
    private var audioFile: File? = null
    private var selectedSong: File? = null
    private var tempVideoFile: File? = null
    private var videoFile: File? = null
    private var displayId = -1
    private var videoFileOne: File? = null
    private var videoFileTwo: File? = null
    private var finalOutFile: File? = null
    var epVideos = ArrayList<EpVideo>()
    val localSongsList = ArrayList<DataItem>()
    var localPaths = ArrayList<File>()
    var downloadAsyncTask: AsyncTask<ArrayList<String>, String?, String?>? = null


    private lateinit var songListAdapter: SongListAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private var isSDPresent: Boolean? = null
    private var simpleDateFormat: SimpleDateFormat? = null
    private var timeStamp: String? = null


    private var player: SimpleExoPlayer? = null
    private var isPlayingSong = false

    private val commonViewModel by viewModel<CommonViewModel>()

    var finger_spacing = 0f
    var zoom_level = 1


    var hourglass: Hourglass? = null


    private val cdTimer: CountDownTimerPausable? = null

    companion object {
        private const val TAG = "CameraXDemo"
        const val KEY_GRID = "sPrefGridVideo"
        var stopRecordingJob: Job? = null
        var downloadingJob: Job? = null
        var showTimerJob: Job? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_recordvideos)
        rangeTimer()



        if (VrockkApplication.user_obj != null) {
            if (VrockkApplication.user_obj?.level == 0)
                SECONDS_DELAY = 60
            else
                SECONDS_DELAY = 180
        }
        resetTimer()

        observerApi()
        commonViewModel.getAllSongs(getMyAuthToken(), 1, "", 10)
        val displayMatrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMatrics)
        Log.e("Height", " $videoHeight")
        Log.e("width", " $videoWidth")

        if (intent.hasExtra("songName")) {
            songName = intent.getStringExtra("songName") ?: ""
            songId = intent.getStringExtra("songId") ?: ""
            val song = intent.getStringExtra("song") ?: ""
            Log.e("SOng", " $song")
            Log.e("SOng", " $songName")
            startDownload(this@RecordVideosActivity, song, songName.replace(".mp3", ""))
        }

        localSongsList.clear()
        // chooseSong()
        initFFmpeg()
        initComs()
        handleBottomSheetSlide()
        setupRecyclerview()
        setupFilter()
        rangeSpeed()

        ivCross.setOnClickListener {
            finish()
        }

        ivRotate.setOnClickListener {
            releaseCamera()
            lensFacing = if (lensFacing == LensFacing.BACK) {
                LensFacing.FRONT
            } else {
                LensFacing.BACK
            }
            toggleClick = true
        }
        ivSpeed.setOnClickListener {
            filter_list.visibility = View.GONE
            if (linearRange.isVisible)
                linearRange.visibility = View.GONE
            else
                linearRange.visibility = View.VISIBLE
        }
        ivTimer.setOnClickListener {

            // showTimer=!showTimer
            /////////
            ivTimer.visibility = View.INVISIBLE
            linearTimer.visibility = View.VISIBLE
            val aniRotate: Animation =
                AnimationUtils.loadAnimation(this, R.anim.slide_right_to_left)
            linearTimer.startAnimation(aniRotate)

        }
        ivStarOff.setOnClickListener {
            Log.e("Click", " click ")
            linearRange.visibility = View.GONE
            if (filter_list.isVisible) {
                filter_list.visibility = View.GONE
            } else {
                filter_list.visibility = View.VISIBLE
            }
        }
        ivUserProfile.setOnClickListener {
            linearRange.visibility = View.GONE
            listRaw()
            Handler().postDelayed({
                bottomSheetBehavior.peekHeight = 800
                ivStart.isClickable = true
            }, 300)
        }

        ivGallery.setOnClickListener {
            pickFromGallery()
        }

        ivCrossVideo.setOnClickListener {
            stopRecordingJob?.let {
                it.cancel()
            }
            pBar.visibility = View.GONE
            PROGRESS_TIMER_COUNT = 0
            //epVideos.clear()
            ivGallery.visibility = View.VISIBLE

            resetTimer()

            tvShowCounter.visibility = View.GONE
            ivUserProfile.visibility = View.VISIBLE
            ivSpeed.visibility = View.VISIBLE

            ivCrossVideo.visibility = View.GONE
            ivCheck.visibility = View.GONE

            localPaths = ArrayList<File>()
            epVideos = ArrayList<EpVideo>()
            videoFile = null

//            if(videoFile !=null && videoFile!!.exists()){
//                videoFile?.delete()
//                videoFile = null
//            }

        }

        ivCheck.setOnClickListener {
            showProgress("")
            PROGRESS_TIMER_COUNT = 0
            Handler().postDelayed({
                // trimAudio(videoFileOne!!)
                //mergerVideoClips()
                mergerVideoClips()

                /*if(selectedPlayback != 1.0F)
                    saveVideoWithSpeed(videoFileOne!!)
                else if(audioFile !=null)
                    trimAudio(videoFileOne!!)
                else
                    finalVideoSave()
*/

                pBar.visibility = View.GONE
                ivCheck.visibility = View.GONE
                ivCrossVideo.visibility = View.GONE
            }, 2000)
        }


        ibCross.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            bottomSheetBehavior.peekHeight = 0
        }

        txtUploadSong.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            bottomSheetBehavior.peekHeight = 0
            val ii = Intent()
            ii.action = Intent.ACTION_GET_CONTENT
            ii.type = "audio/*"
            startActivityForResult(ii, 50)
        }



        tvSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val text = p0.toString()
                val list = ArrayList<DataItem>()
                for (song in localSongsList) {
                    if (song.originalName.toLowerCase().contains(text.toLowerCase())) {
                        list.add(song)
                    }
                }

                songListAdapter.filterList(list)
                songListAdapter.notifyDataSetChanged()
            }
        })

        // setupVideoLengthSelection()
        /*videoLengthSelectionCross.setOnClickListener {
            videoLengthSelectionLayout.visibility = View.GONE
        }*/
    }

    private fun resetTimer() {
        try {
            hourglass?.stopTimer()
            hourglass = object : Hourglass(SECONDS_DELAY * 1000, 1000) {
                override fun onTimerTick(timeRemaining: Long) {
                    tvShowCounter.text = String.format(
                        Locale.getDefault(), "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(
                            timeRemaining
                        ) % 60,
                        TimeUnit.MILLISECONDS.toSeconds(
                            timeRemaining
                        ) % 60
                    )
                }

                override fun onTimerFinish() {
                    tvShowCounter.text = "00:00"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun observerApi() {
        commonViewModel.allSongsResponseLiveData().observe(this, Observer<ApiResponse> { response ->
            this.searchData(response)
        })
    }

    private fun searchData(response: ApiResponse?) {
        when (response!!.status) {
            Status.LOADING -> {
                // showProgress("")
            }
            Status.SUCCESS -> {
                //hideProgress()
                renderResponse(response)
            }
            Status.ERROR -> {
                // hideProgress()
                Log.e("response", Gson().toJson(response.error))
                //showCustomToastMessage(this, "Something went wrong!")
            }
        }
    }

    private fun renderResponse(response: ApiResponse) {
        val data: String = Utils.toJson(response.data)
        val gson1 = Gson()
        val songsModel = gson1.fromJson(data, SongsListModel::class.java)
        if (songsModel.isSuccess) {
            localSongsList.addAll(songsModel.data!!)
            songListAdapter.notifyDataSetChanged()
            // listener()
        }
    }

    fun rangeTimer() {
        tvOffTimer.setOnClickListener {
            Log.e("call", "timeroff")
            timerSecond = 0
            TIMER_DELAY = 0
            showTimer = false
            Log.e("call", "timersecond: " + timerSecond)

            tvOffTimer.background = resources.getDrawable(R.drawable.round_corner_white2)
            tvThreeSecond.background = null
            tvTenSecond.background = null

            tvThreeSecond.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            tvTenSecond.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            tvOffTimer.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
            if (showTimer)
                ivTimer.setImageResource(R.drawable.timer_icon)
            else
                ivTimer.setImageResource(R.drawable.timer_off)

        }
        tvThreeSecond.setOnClickListener {

            Log.e("call", "3 second")
            timerSecond = 3
            TIMER_DELAY = 3
            Log.e("call", "timersecond: " + timerSecond)
            showTimer = true
            tvOffTimer.background = null
            tvThreeSecond.background = resources.getDrawable(R.drawable.round_corner_white2)
            tvTenSecond.background = null

            tvThreeSecond.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
            tvTenSecond.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            tvOffTimer.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            if (showTimer)
                ivTimer.setImageResource(R.drawable.timer_icon)
            else
                ivTimer.setImageResource(R.drawable.timer_off)

        }
        tvTenSecond.setOnClickListener {

            Log.e("call", "10 second")
            timerSecond = 10
            TIMER_DELAY = 10
            Log.e("call", "timersecond: " + timerSecond)
            showTimer = true
            tvOffTimer.background = null
            tvThreeSecond.background = null
            tvTenSecond.background = resources.getDrawable(R.drawable.round_corner_white2)

            tvThreeSecond.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            tvTenSecond.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
            tvOffTimer.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            if (showTimer)
                ivTimer.setImageResource(R.drawable.timer_icon)
            else
                ivTimer.setImageResource(R.drawable.timer_off)

        }
        ivClosePopup.setOnClickListener {
            linearTimer.visibility = View.GONE
            ivTimer.visibility = View.VISIBLE
        }
    }

    fun rangeSpeed() {
        tvZeroPointThree.setOnClickListener {
            selectedPlayback = 1.75F
            tvZeroPointThree.background = resources.getDrawable(R.drawable.round_corner_white)
            tvZeroPointFive.background = null
            tvZeroPointOne.background = null
            tvTwo.background = null
            tvThree.background = null

            tvZeroPointOne.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            tvZeroPointFive.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            tvThree.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            tvTwo.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            tvZeroPointThree.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
        }
        tvZeroPointFive.setOnClickListener {
            selectedPlayback = 1.50F
            tvZeroPointThree.background = null
            tvZeroPointFive.background = resources.getDrawable(R.drawable.round_corner_white)
            tvZeroPointOne.background = null
            tvTwo.background = null
            tvThree.background = null

            tvZeroPointOne.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            tvZeroPointThree.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            tvThree.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            tvTwo.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            tvZeroPointFive.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
        }
        tvZeroPointOne.setOnClickListener {
            selectedPlayback = 1.0F
            tvZeroPointThree.background = null
            tvZeroPointFive.background = null
            tvZeroPointOne.background = resources.getDrawable(R.drawable.round_corner_white)
            tvTwo.background = null
            tvThree.background = null
            tvZeroPointFive.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            tvZeroPointThree.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            tvThree.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            tvTwo.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            tvZeroPointOne.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
        }
        tvTwo.setOnClickListener {
            selectedPlayback = 0.50F
            tvZeroPointThree.background = null
            tvZeroPointFive.background = null
            tvZeroPointOne.background = null
            tvTwo.background = resources.getDrawable(R.drawable.round_corner_white)
            tvThree.background = null

            tvZeroPointFive.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            tvZeroPointThree.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            tvThree.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            tvZeroPointOne.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            tvTwo.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))

        }
        tvThree.setOnClickListener {
            selectedPlayback = 0.25F
            tvZeroPointThree.background = null
            tvZeroPointFive.background = null
            tvZeroPointOne.background = null
            tvTwo.background = null
            tvThree.background = resources.getDrawable(R.drawable.round_corner_white)

            tvZeroPointFive.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            tvZeroPointThree.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            tvTwo.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            tvZeroPointOne.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            tvThree.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))

        }

    }

    private fun pickFromGallery() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) !== PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                "Read Storage",
                10
            )
        } else {
            val intent = Intent()
            intent.setTypeAndNormalize("video/mp4")
            intent.action = Intent.ACTION_GET_CONTENT
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(
                Intent.createChooser(
                    intent,
                    "Video Select"
                ), 11
            )
        }
    }

    private fun requestPermission(
        permission: String,
        rationale: String,
        requestCode: Int
    ) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Permission")
            builder.setMessage(rationale)
            builder.setPositiveButton(
                "Ok"
            ) { dialog, which ->
                ActivityCompat.requestPermissions(
                    this@RecordVideosActivity,
                    arrayOf(permission),
                    requestCode
                )
            }
            builder.setNegativeButton("Cancel", null)
            builder.show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            10 -> if (grantResults[0] === PackageManager.PERMISSION_GRANTED) {
                pickFromGallery()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun setupFilter() {

        val adapter = FilterViewAdapter(this)
        filter_list.adapter = adapter

    }


    private fun showTimerViews(isShow: Boolean) {
        if (isShow) {
            tv_timer.visibility = View.VISIBLE
            ivStart.visibility = View.GONE
        } else {
            tv_timer.visibility = View.GONE
            ivStart.visibility = View.VISIBLE
        }
    }

    private fun startRecording() {
        isRecording = false
        ivStart.setOnClickListener {
            if (!isRecording) {

                ////  show counter
                tvShowCounter.visibility = View.VISIBLE

                if (showTimer) {

                    Log.e("call", "Timer show... ")

//                    linearTimer.visibility = View.GONE
                    ivGallery.visibility = View.GONE
                    linearTimer.visibility = View.GONE
                    linearRange.visibility = View.GONE
                    filter_list.visibility = View.GONE
                    ivCheck.visibility = View.GONE
                    ivCrossVideo.visibility = View.GONE

                    showTimerJob = CoroutineScope(Dispatchers.Main).launch {
                        showTimerViews(true)
                        for (i in TIMER_DELAY downTo 0) {
                            Log.e("TIMER", " $i ")
                            tv_timer.text = i.toString()
                            delay(1000)
                        }
                        startRecordingVideo()
                    }
                } else {
                    startRecordingVideo()
                }

                if (hourglass!!.isPaused)
                    hourglass!!.resumeTimer()

            } else {

                hourglass!!.pauseTimer()

                stopRecordingJob?.let {
                    it.cancel()
                }
                // animateRecord.cancel()
                gpuCameraRecorder!!.stop()
                if (player != null) {
                    player!!.playWhenReady = false
                    isPlayingSong = false
                }

                //   filter_list.visibility = View.VISIBLE
                ivRotate.visibility = View.VISIBLE
                ivTimer.visibility = View.VISIBLE
                ivStarOff.visibility = View.VISIBLE
                if (PROGRESS_TIMER_COUNT >= 0) {
                } else {
                    ivSpeed.visibility = View.VISIBLE
                    ivUserProfile.visibility = View.VISIBLE
                }

            }
            isRecording = !isRecording
        }

    }


    private fun startRecordingVideo() {
        showTimerViews(false)
        linearTimer.visibility = View.GONE
        ivTimer.visibility = View.GONE
        ivRotate.visibility = View.GONE
        ivRotate.visibility = View.GONE
        ivSpeed.visibility = View.GONE
        ivGallery.visibility = View.GONE
        ivEmoji.visibility = View.GONE
        ivCheck.visibility = View.GONE
        ivCrossVideo.visibility = View.GONE
        ivUserProfile.visibility = View.GONE
        ivStarOff.visibility = View.GONE
        ivTimer.visibility = View.GONE
        linearRange.visibility = View.GONE
        filter_list.visibility = View.GONE
        ivCheck.visibility = View.GONE
        ivCrossVideo.visibility = View.GONE

        //animateRecord.start()
        stopRecordingJob = CoroutineScope(Dispatchers.Main).launch {
            stopRecording()
        }
        videoFile = File(outputDirectory, "${System.currentTimeMillis()}.mp4")
        filepath = videoFile!!.absolutePath
        gpuCameraRecorder!!.start(videoFile!!.absolutePath)
        if (selectedSong != null)
            setUpSimpleExoPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (player != null) {
            player!!.stop()
            player!!.release()
            player = null
        }

    }

    override fun onPause() {
        super.onPause()
        if (player != null) {
            player!!.playWhenReady = false
        }
    }

    private suspend fun stopRecording() {


        pBar.visibility = View.VISIBLE
        pBar.max = SECONDS_DELAY.toInt() * 10
        //delay(SECONDS_DELAY)

        for (i in PROGRESS_TIMER_COUNT until SECONDS_DELAY) {

            if (PROGRESS_TIMER_COUNT >= 0) {
                Log.e("call", "111111 start....")
                hourglass!!.startTimer()
            } else {
                Log.e("call", "111111 Resume....")
                hourglass!!.resumeTimer()
            }

            // Log.e("SECONDS", i.toString())
            PROGRESS_TIMER_COUNT = i + 1
            //pBar.progress = i.toInt()
            val progressAnimator =
                ObjectAnimator.ofInt(pBar, "progress", i.toInt() * 10, (i.toInt() + 1) * 10)
            progressAnimator.duration = 1000
            progressAnimator.interpolator = LinearInterpolator()
            progressAnimator.start()
            pBar.invalidate()
            delay(1000)

            if (PROGRESS_TIMER_COUNT == SECONDS_DELAY) {
                pBar.visibility = View.GONE
                stopRecordingJob?.let {
                    it.cancel()
                }
                isRecording = false
                try {
                    if (gpuCameraRecorder != null)
                        gpuCameraRecorder!!.stop()
                } catch (e: Exception) {

                }

                if (player != null) {
                    player!!.playWhenReady = false
                }
                pBar.visibility = View.GONE
                PROGRESS_TIMER_COUNT = 0
                //epVideos.clear()
                //  ivGallery.visibility = View.VISIBLE
                //   ivEmoji.visibility = View.VISIBLE
                ivCrossVideo.visibility = View.VISIBLE
                ivCheck.visibility = View.VISIBLE
                /*if(videoFile !=null && videoFile!!.exists()){
                    videoFile?.delete()
                }*/
            }
        }
        startRecording()
    }

    private fun initFFmpeg() {

    }

    override fun onBackPressed() {
        finish()
    }

    private fun initComs() {
        simpleDateFormat = SimpleDateFormat("ddMMyyyyhhmmss", Locale.US)
        timeStamp = simpleDateFormat!!.format(Date())
        isSDPresent = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }


    private fun setupRecyclerview() {
        songListAdapter = SongListAdapter(this, localSongsList)
        rvSongsList.adapter = songListAdapter
        rvSongsList.setHasFixedSize(true)
        rvSongsList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        listener()
    }

    private fun listener() {
        songListAdapter.adapterListener(object : SongListAdapter.ClickAdapter {
            override fun clickItem(p: Int) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                bottomSheetBehavior.peekHeight = 0
                position = p
                /*downloadingJob = CoroutineScope(Dispatchers.Main).launch {
                    DownloadingSong(IMAGE_BASE_URL+localSongsList[position].song,localSongsList[position].originalName.replace(".mp3",""))
                }*/
                startDownload(
                    this@RecordVideosActivity,
                    localSongsList[position].song,
                    localSongsList[position].originalName.replace(".mp3", "")
                )
            }
        })
    }


    private fun collapsebottomsheet() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun expandBottomsheet() {
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun handleBottomSheetSlide() {
        bottomSheetBehavior = BottomSheetBehavior.from<ConstraintLayout>(bottomSheet)
//        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        bottomSheet.setBackgroundResource(R.drawable.drawable_topround)
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            bottomSheet.setBackgroundColor(getColor(R.color.colorBlack))
                        else
                            bottomSheet.setBackgroundColor(Color.parseColor("#000000"))
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        bottomSheet.setBackgroundResource(R.drawable.drawable_topround)
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {

                    }
                }
            }
        })
    }


    fun listRaw() {

    }


    private fun chooseSong() {
        localPaths.clear()
        try {
            val fields: Array<Field> = R.raw::class.java.fields
            for (count in fields.indices) {
                val audioFile = utils.createAudioFolder(this)
                localPaths.add(audioFile)
                val reSourseIdd = fields[count].getInt(fields[count])
                val inputStream: InputStream = resources.openRawResource(reSourseIdd)
                val out: OutputStream = FileOutputStream(audioFile)
                val buf = ByteArray(1024)
                var len: Int = 0
                while (inputStream.read(buf).also { len = it } > 0) out.write(buf, 0, len)
                out.close()
                inputStream.close()
            }

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun setUpCameraView() {
        runOnUiThread(Runnable {
            wrap_view.removeAllViews()
            sampleGLView = null
            sampleGLView = SampleCameraGLView(this)
            sampleGLView!!.setTouchListener { count, event, width, height ->

                Log.e("touch", " ${event.pointerCount}")
                Log.e("touch", "count ${count}")
                if (gpuCameraRecorder == null)
                    return@setTouchListener

                gpuCameraRecorder!!.changeManualFocusPoint(event.x, event.y, width, height)

                gpuCameraRecorder!!.changeAutoFocus()

                if (event.pointerCount == 2) {
                    distance(event, 0, 1)
                    val zoomVal = distance(event, 0, 1)
                    val charVal = zoomVal.toString()[0]
                    Zoom(event)
                    // gpuCameraRecorder?.setGestureScale(charVal.toFloat())

                }

            }
            wrap_view.addView(sampleGLView)
        })

    }

    fun getCameraId(context: Context, facing: Int): String {
        val manager = context.getSystemService(CAMERA_SERVICE) as CameraManager

        return manager.cameraIdList.first {
            manager
                .getCameraCharacteristics(it)
                .get(CameraCharacteristics.LENS_FACING) == facing
        }
    }

    fun Zoom(event: MotionEvent) {
        try {

            val manager: CameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val characteristics: CameraCharacteristics =
                manager.getCameraCharacteristics(getCameraId(this, lensFacing.facing))
            val maxzoom: Float =
                characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)!! * 10
            val m: Rect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)!!
            val action: Int = event.action
            val current_finger_spacing: Float
            if (event.pointerCount > 1) {
                // Multi touch logic
                current_finger_spacing = distance(event, 0, 1)
                if (finger_spacing != 0f) {
                    if (current_finger_spacing > finger_spacing && maxzoom > zoom_level) {
                        zoom_level++
                    } else if (current_finger_spacing < finger_spacing && zoom_level > 1) {
                        zoom_level--
                    }
                    val difW: Int = m.width() - (m.width() / maxzoom).toInt()
                    val difH: Int = m.height() - (m.height() / maxzoom).toInt()
                    var cropW = difW / 100 * zoom_level
                    var cropH = difH / 100 * zoom_level
                    cropW -= cropW and 3
                    cropH -= cropH and 3

                    val zoom = Rect(cropW, cropH, m.width() - cropW, m.height() - cropH)

                    Log.e("zoom", " $zoom")
                    // GPUCameraRecorder.thread.requestBuilder
                    GPUCameraRecorder.thread.requestBuilder.set(
                        CaptureRequest.SCALER_CROP_REGION,
                        zoom
                    )
                }
                finger_spacing = current_finger_spacing
            } else {
                if (action == MotionEvent.ACTION_UP) {
                    //single touch logic
                }
            }
            try {
                // mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, null)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            } catch (ex: NullPointerException) {
                ex.printStackTrace()
            }
        } catch (e: CameraAccessException) {
            throw RuntimeException("can not access camera.", e)
        }
    }

    fun distance(event: MotionEvent, first: Int, second: Int): Float {
        val x: Float = event.getX(first) - event.getX(second)
        val y: Float = event.getY(first) - event.getY(second)
        return Math.sqrt(x * x + y * y.toDouble()).toFloat()
    }

    override fun onResume() {
        super.onResume()
        if (videoFileOne != null && videoFileOne!!.exists()) {
            videoFileOne!!.delete()
            videoFileOne = null

            timerStart = false
        }
        setUpCamera()
    }

    override fun onStop() {
        super.onStop()
        releaseCamera()
        if (downloadAsyncTask != null)
            downloadAsyncTask?.cancel(true)

        if (stopRecordingJob != null) {
            stopRecordingJob.let {
                it?.cancel()
            }
        }

    }

    private fun releaseCamera() {
        if (sampleGLView != null) {
            sampleGLView!!.onPause()
        }
        if (gpuCameraRecorder != null) {
            gpuCameraRecorder!!.stop()
            gpuCameraRecorder!!.release()
            gpuCameraRecorder = null
        }
        if (sampleGLView != null) {
            wrap_view.removeView(sampleGLView)
            sampleGLView = null
        }
    }

    private fun setUpCamera() {
        startRecording()
        setUpCameraView()
        gpuCameraRecorder = GPUCameraRecorderBuilder(this, sampleGLView) //.recordNoFilter(true)
            .cameraRecordListener(object : CameraRecordListener {
                override fun onGetFlashSupport(flashSupport: Boolean) {
                    /*requireActivity().runOnUiThread(Runnable {
                        findViewById<View>(R.id.btn_flash).setEnabled(
                            flashSupport
                        )
                    })*/
                }

                override fun onRecordComplete() {
                    // exportMp4ToGallery(this, filepath!!)
                    //val getfile = loadFile(fileName)
                    ivCheck.visibility = View.VISIBLE
                    ivCrossVideo.visibility = View.VISIBLE
                    Handler().postDelayed({
                        addVideoPaths()
                    }, 100)

                }

                override fun onRecordStart() {
                    Log.e("GPUCameraRecorder", " start recording ")

                    runOnUiThread(Runnable { filter_list.visibility = View.GONE })
                }

                override fun onError(exception: Exception) {
                    Log.e("GPUCameraRecorder", exception.toString())
                }

                override fun onCameraThreadFinish() {
                    if (toggleClick) {
                        runOnUiThread(Runnable { setUpCamera() })
                    }
                    toggleClick = false
                }

                override fun onVideoFileReady() {}
            })
            .videoSize(videoWidth, videoHeight)
            .cameraSize(cameraWidth, cameraHeight)
            .lensFacing(lensFacing)
            .build()
    }

    override fun onProgress(progress: String) {
        Log.e("FFMpegCallback", "onProgress")

    }

    override fun onSuccess(convertedFile: File, type: String) {
        Log.e("FFMpegCallback", "onSuccess -- $type")
        when (type) {
            OutputType.TYPE_AUDIO -> {

                audioFile = convertedFile
                mergeVideoWithAudio(videoFile!!)
                /* if(epVideos.size>1)
                     mergeVideoWithAudio(finalOutFile!!)
                 else
                     mergeVideoWithAudio(videoFileOne!!)*/
            }
            OutputType.TYPE_VIDEO_SPEED -> {
                videoFile = convertedFile
                if (audioFile != null)
                    trimAudio(convertedFile)
                else
                    finalVideoSave()
            }

            OutputType.TYPE_MERGE_VIDEO -> {
                videoFile = convertedFile
            }

            OutputType.TYPE_VIDEO -> {

                videoFile = convertedFile
                finalVideoSave()
            }
        }
    }

    private fun finalVideoSave() {

        if (audioFile != null && audioFile!!.exists())
            audioFile!!.delete()

        runOnUiThread {
            ivGallery.visibility = View.VISIBLE
            var lastFile: File? = null

            startActivity(
                // Intent(this,VideoTrimmerActivity::class.java).putExtra("videoFile",videoFile!!.absolutePath)
                Intent(this, PreviewVideoActivity::class.java)
                    .putExtra("videoFile", videoFile!!.absolutePath)
                    .putExtra("songId", songId)
                    .putExtra("isUpload", isUpload)
                    .putExtra("selectedSong", selectedSong?.absolutePath)
                    .putExtra("songName", songName)
            )
            finish()

        }
    }

    override fun onFailure(error: Exception) {
        Log.e("FFMpegCallback", "onFailure")
        Log.e("FFMpegCallbackOnFailure", error.localizedMessage.toString())
    }

    override fun onNotAvailable(error: Exception) {

        Log.e("CallbackOnNotAvailable", error.localizedMessage.toString())
    }

    override fun onFinish() {
        Log.e("FFMpegCallback", "onFinish")
    }

    private fun saveVideoWithSpeed(file: File) {

        val isHavingAudio = utils.isVideoHaveAudioTrack(file.absolutePath)
        // stopRunningProcess()

        // if (!isRunning()) {
        val outputFile = utils.createVideoFile(this)
        Log.e("tagName", "outputFile: speed ${outputFile.absolutePath}")

        VideoEditor.with(this)
            .setType(Constant.VIDEO_PLAYBACK_SPEED)
            .setFile(file)
            // .setAudioFile(audioFile!!)
            .setIsHavingAudio(isHavingAudio)
            .setOutputPath(outputFile.path)
            .setSpeedTempo(selectedPlayback.toString(), 2.0F.toString())
            .setCallback(this@RecordVideosActivity)
            .main()
        //  } else {
        // showInProgressToast()
        //  }
    }

    private fun mergeVideoWithAudio(file: File) {
        //   stopRunningProcess()
        //   if (!isRunning()) {
        val outputFile = utils.createVideoFile(this)
        Log.e("tagName", "outputFile: ${outputFile.absolutePath}")

        VideoEditor.with(this)
            .setType(Constant.VIDEO_AUDIO_MERGE)
            .setFile(file)
            .setAudioFile(audioFile!!)
            .setOutputPath(outputFile.path)
            .setCallback(this@RecordVideosActivity)
            .main()
        //} else {
        // showInProgressToast()
        // }
    }

    private fun trimAudio(file: File) {
        // chooseSong()
        // audioFile = localSongsList[0].songPath
        //stopRunningProcess()
        val calculateStart =
            calculateSecMinHour(TimeUnit.SECONDS.toMillis(startTimeOfSong.toLong()))

        val videoLength = calculateSecMinHour(
            utils.getVideoDuration(this, file) + TimeUnit.SECONDS.toMillis(startTimeOfSong.toLong())
        )

        Log.e("tagName", "calculateStart : $calculateStart")

        //  if (!isRunning()) {
        val outputFile = utils.createAudioFile(this)
        Log.e("tagName", "outputFile: ${outputFile.absolutePath}")
        Log.e("tagName", "audioFile: $audioFile")
        Log.e("tagName", "videoLength : $videoLength")


        VideoEditor.with(this)
            .setType(Constant.AUDIO_TRIM)
            .setAudioFile(audioFile!!)
            .setOutputPath(outputFile.absolutePath)
            //  .setStartTime("00:00:00")
            .setStartTime(calculateStart)
            .setEndTime(videoLength)
            .setCallback(this)
            .main()
        // } else {
        // showInProgressToast()
        //   }
    }

    private fun addVideoPaths() {
        if (videoFile != null)
            epVideos.add(EpVideo(videoFile!!.absolutePath))

        Log.e("ADDED", " videoFile!!.absolutePath  ${epVideos.size} ")
    }

    private fun mergerVideoClips() {


        if (epVideos.size > 1) {
            videoFile = utils.createVideoFile(this)
            val outputOption = EpEditor.OutputOption(videoFile!!.absolutePath) //Output
            // val outputOption = EpEditor.OutputOption(videoFile!!.absolutePath) //Output
            outputOption.setWidth(750) // output video width, default 480
            outputOption.setHeight(1334)
            outputOption.frameRate = 30; // output video frame rate, default 30
            outputOption.bitRate = 10
            Handler().postDelayed({
                EpEditor.merge(epVideos, outputOption, object : OnEditorListener {
                    override fun onSuccess() {
                        Log.e("MERGERRR", " $videoFile ")
                        hideProgress()

                        /*startActivity(
                            // Intent(this,VideoTrimmerActivity::class.java).putExtra("videoFile",videoFile!!.absolutePath)
                            Intent(this@RecordVideosActivity,PreviewVideoActivity::class.java).putExtra("videoFile",videoFile!!.absolutePath)
                        )*/

                        if (selectedPlayback != 1.0F)
                            saveVideoWithSpeed(videoFile!!)
                        else if (audioFile != null)
                            trimAudio(videoFile!!)
                        else
                            finalVideoSave()
                    }

                    override fun onFailure() {
                        Log.e("Progress", " faliure ")
                    }

                    override fun onProgress(progress: Float) {
                        Log.e("Progress", "$progress")
                        if (progress.toDouble() == 1.0) {

                        }
                    }
                })
            }, 300)
        } else {
            if (selectedPlayback != 1.0F)
                saveVideoWithSpeed(videoFile!!)
            else if (audioFile != null)
                trimAudio(videoFile!!)
            else
                finalVideoSave()
        }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10 && resultCode == Activity.RESULT_OK) {
            if (data != null && data.hasExtra("startTime")) {
                startTimeOfSong = data.getDoubleExtra("startTime", 0.0).toInt()
                endTimeOfSong = data.getDoubleExtra("endTime", 0.0).toInt()
                SECONDS_DELAY = data.getIntExtra("duration", 0).toLong()
                position = data.getIntExtra("position", 0)
                isUpload = data.getBooleanExtra("isUpload", false)
                selectedSong = File(data.getStringExtra("audioFile") ?: "")
//                Log.e("localSongsList" ," ${localSongsList[position].originalName} $position ")
                if (isUpload) {
                    Log.e("isUpload", " in ")
                    songName = data.getStringExtra("songName") ?: ""
                    audioFile = File(data.getStringExtra("mySongFilePath") ?: "")
                    txtSongName.text = songName.replace(".mp3", "")
                } else if (data.hasExtra("selectedSong")) {
                    Log.e("selectedSong", " in ")
                    songName = data.getStringExtra("songName") ?: ""
                    //  audioFile = File(data.getStringExtra("mySongFilePath")?:"")
                    audioFile = selectedSong
                    songId = data.getStringExtra("songId") ?: ""
                    txtSongName.text = songName

                } else {
                    Log.e("selectedSong", " else ")
                    txtSongName.text = localSongsList[position].originalName.replace(".mp3", "")
                    audioFile = loadFile(localSongsList[position].originalName.replace(".mp3", ""))
                    songId = localSongsList[position].id
                }
                resetTimer()
            }
        } else if (requestCode == 11 && resultCode == Activity.RESULT_OK) {
            val selectedUri: Uri? = data?.data
            val path = FileUtils.getRealPath(this, selectedUri)
            Log.e("selectedUri", " $path")
            if (selectedUri != null) {
                Log.e("selectedUri", " in $selectedUri")
                //Intent(this,VideoTrimmerActivity::class.java).putExtra("videoFile",selectedUri)
                val ii = Intent(this, VideoTrimmerActivity::class.java)
                ii.putExtra("videoUri", path)
                startActivity(ii)
                finish()
            } else {
                Toast.makeText(this, "Can't Retrive Video", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 50 && resultCode == Activity.RESULT_OK) {
            val fileUri = data?.data
            songPathAfterDownload = FileUtils.getRealPath(this, fileUri)
            if (!intent.hasExtra("songName")) {
                startActivityForResult(
                    Intent(this@RecordVideosActivity, StartCountDownActivity::class.java)
                        .putExtra("songFile", songPathAfterDownload)
                        .putExtra("position", 0)
                        .putExtra("upload", "")
                        .putExtra("songName", File(songPathAfterDownload).name), 10
                )
            }

        }
    }

    override fun onFilterSelected(position: Int, photoFilter: PhotoFilter?) {
        val filterTypes: List<FilterType> = FilterType.createFilterList()
        if (gpuCameraRecorder != null) {
            gpuCameraRecorder!!.setFilter(
                FilterType.createGlFilter(
                    filterTypes[position],
                    this
                )
            )
        }
    }

    fun initPlayer() {
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

        if (player == null) {
            initPlayer()

            player!!.apply {
                val userAgent =
                    Util.getUserAgent(this@RecordVideosActivity, getString(R.string.app_name))
                val mediaSource = ProgressiveMediaSource.Factory(
                    DefaultDataSourceFactory(this@RecordVideosActivity, userAgent),
                    DefaultExtractorsFactory()
                ).createMediaSource(Uri.parse(selectedSong!!.absolutePath))
                prepare(mediaSource!!, true, false)
                player!!.playWhenReady = true
                isPlayingSong = true
            }
        } else {
            player!!.playWhenReady = true
        }

    }


    private fun startDownload(context: Context, songfile: String?, songName: String?) {
        val songArr = ArrayList<String>()
        songArr.add(songfile!!)
        songArr.add(songName!!)
        // isDownload =
        val url = loadFile(songName)
        Log.e("SONG_FILE", " ${url.length()} ")

        if (url.length().toInt() == 0) {
            audioFile = url
            downloadAsyncTask = DownloadFileAsync(this@RecordVideosActivity)
            downloadAsyncTask?.execute(songArr)
        } else {
            audioFile = File(url.absolutePath)
            if (intent.hasExtra("songName")) {
                startActivityForResult(
                    Intent(this@RecordVideosActivity, StartCountDownActivity::class.java)
                        .putExtra("songFile", audioFile!!.path)
                        .putExtra("selectedSong", "")
                        .putExtra("songId", songId)
                        .putExtra("songName", songName)
                        .putExtra("position", position), 10
                )
            } else {
                startActivityForResult(
                    Intent(this@RecordVideosActivity, StartCountDownActivity::class.java)
                        .putExtra("songFile", audioFile!!.path)
                        .putExtra("position", position), 10
                )
            }

        }
    }

    fun updateUi() {
        Log.e("cancel", " ${downloadAsyncTask?.isCancelled}")
        if (!downloadAsyncTask?.isCancelled!!) {
            if (intent.hasExtra("songName")) {
                startActivityForResult(
                    Intent(this@RecordVideosActivity, StartCountDownActivity::class.java)
                        .putExtra("songFile", audioFile!!.path)
                        .putExtra("selectedSong", "")
                        .putExtra("songName", songName)
                        .putExtra("songId", songId)
                        .putExtra("position", position), 10
                )
            } else {
                startActivityForResult(
                    Intent(this@RecordVideosActivity, StartCountDownActivity::class.java)
                        .putExtra("songFile", audioFile!!.path)
                        .putExtra("position", position), 10
                )
            }

        }

    }

    private fun loadFile(name: String): File {
        var textFromFile = "";
        val testFile = File(this.cacheDir, "Songs")
        val songFile = File(testFile, "$name.mp3")
        // if(songFile.exists())
        Log.e("SONG_FILE", " ${songFile.length()} ")
        return songFile
    }
}


class DownloadFileAsync(val context: RecordVideosActivity) :
    AsyncTask<ArrayList<String>, String?, String?>() {

    override fun onPreExecute() {
        super.onPreExecute()

        //  mPDialog = ProgressDialog(context)
        //  circulerProgressBar = CirculerProgressBar(context,"")
    }

    override fun doInBackground(vararg params: ArrayList<String>): String {
        var count: Int = 0
        Log.e("1212", "gjajha ${params.get(0)}")
        val list = params[0]
        //  Log.e("1212","gjajha ${params[1].toString()}")


        try {


            val cacheDir = File(context.cacheDir, "Songs")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val url = URL(list[0].toString())
            val conexion: URLConnection = url.openConnection()
            conexion.connect()
            val lenghtOfFile: Int = conexion.contentLength
            Log.d("ANDRO_ASYNC", "Length of file: $lenghtOfFile")
            val input: InputStream = BufferedInputStream(url.openStream())
            val output: OutputStream = FileOutputStream(File(cacheDir, list[1] + ".mp3"))
            val data = ByteArray(1024)
            var total: Long = 0
            while (input.read(data).also { count = it } != -1) {
                total += count.toLong()
                publishProgress("" + (total * 100 / lenghtOfFile).toInt())
                output.write(data, 0, count)
            }
            output.flush()
            output.close()
            input.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    override fun onProgressUpdate(vararg values: String?) {
        super.onProgressUpdate(*values)
        context.circuler_bar.visibility = View.VISIBLE

        context.circulerProgressBar.progress = values[0]!!.toInt()

        context.txtProgress.text = "${values[0]!!}%"
        if (values[0]!!.toInt() == 100) {
            context.circuler_bar.visibility = View.GONE

            //  onPostExecute("")

        }

    }

    override fun onCancelled() {
        super.onCancelled()

    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        Log.e("post", " ${isCancelled}")
        context.updateUi()
    }

}