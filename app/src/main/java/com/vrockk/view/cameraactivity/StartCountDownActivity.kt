package com.vrockk.view.cameraactivity

import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.RelativeLayout
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.vrockk.R
import com.vrockk.base.BaseActivity
import com.vrockk.custom_view.MarkerView
import com.vrockk.custom_view.SoundFile
import com.vrockk.custom_view.WaveformView
import com.vrockk.custom_view.song_cutter.SongMetadataReader
import com.vrockk.editor.VideoEditor
import com.vrockk.editor.utils
import com.vrockk.interfaces.FFMpegCallback
import com.vrockk.utils.Constant
import kotlinx.android.synthetic.main.activity_startcountdown.*
import java.io.File
import java.util.concurrent.TimeUnit

class StartCountDownActivity : BaseActivity(), WaveformView.WaveformListener,
    MarkerView.MarkerListener, FFMpegCallback {

    private var duration: Int = 0
    private var startTime: Double = 0.0
    private var endTime: Double = 0.0
    private var mySongFilePath: String = ""
    private var selectedSong: String = ""
    private var mFile: File? = null
    private var songFileMetadataReader: SongMetadataReader? = null
    private var mSoundFile: SoundFile? = null
    private var mHandler: Handler? = null
    private val mDensity = 0f

    private var mKeyDown = false
    private var mLastDisplayedStartPos = 0
    private var mLastDisplayedEndPos = 0
    private var mStartVisible = false
    private var mEndVisible = false
    private var mWidth = 0
    private var mMaxPos = 0
    private var mStartPos = 0
    private var mEndPos = 0

    private var mLoadingLastUpdateTime: Long = 0
    private var mLoadingKeepGoing = false
    private val mRecordingLastUpdateTime: Long = 0
    private var mRecordingKeepGoing = false
    private val mRecordingTime = 0.0
    private var mFinishActivity = false

    private var mProgressDialog: ProgressDialog? = null

    private var mLoadSoundFileThread: Thread? = null
    private var mRecordAudioThread: Thread? = null
    private var mSaveSoundFileThread: Thread? = null


    private var mTouchDragging = false
    private var mTouchStart = 0f
    private var mTouchInitialOffset = 0
    private var mTouchInitialStartPos = 0
    private var mTouchInitialEndPos = 0
    private var mWaveformTouchStartMsec: Long = 0

    private var mOffset = 0
    private var mOffsetGoal = 0
    private var mFlingVelocity = 0
    private val mPlayStartMsec = 0
    private val mPlayEndMsec = 0
    private var position = 0

    private var mCaption = ""
    private var songName = ""
    private var isUpload = false
    private var player: SimpleExoPlayer? = null
    private var isPlayingSong = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startcountdown)

        mySongFilePath = intent.getStringExtra("songFile")!!

        position = intent.getIntExtra("position", 0)

        if (intent.hasExtra("upload")) {
            isUpload = true
        }

        if (intent.hasExtra("songName"))
            songName = intent.getStringExtra("songName") ?: ""

        setUpSimpleExoPlayer()

        tvStartCountDown.setOnClickListener {
            getSongAfterTrim()
            //navigate(VideoPostActivity::class.java)
        }

        mHandler = Handler()

        loadUi()
        mHandler!!.postDelayed(mTimerRunnable, 100)



        if (mySongFilePath.isNotEmpty())
            loadFromFile()

    }

    private fun getSongAfterTrim() {


        // Create an indeterminate progress dialog
        showProgress("")
        val audioFile = utils.createCroppedAudioFolder(this)

        if (isUpload) {

        }

        mSaveSoundFileThread = object : Thread() {
            override fun run() {
                try {
                    // Write the new file

                    startTime = mWaveformView.pixelsToSeconds(mStartPos)
                    endTime = mWaveformView.pixelsToSeconds(mEndPos)
                    val startFrame = mWaveformView.secondsToFrames(startTime)
                    val endFrame = mWaveformView.secondsToFrames(endTime)
                    duration = (endTime - startTime + 0.5).toInt()
                    //   mSoundFile!!.WriteFile(audioFile, startFrame, endFrame - startFrame);
                    val calculateStart =
                        calculateSecMinHour(TimeUnit.SECONDS.toMillis(startTime.toLong()))
                    val calculateEnd =
                        calculateSecMinHour(TimeUnit.SECONDS.toMillis(endTime.toLong()))
                    Log.e("calculateStart", " $calculateStart $calculateEnd ")
                    Log.e("calculateStart", " $mySongFilePath ")

                    VideoEditor.with(this@StartCountDownActivity)
                        .setType(Constant.AUDIO_TRIM)
                        .setAudioFile(File(mySongFilePath))
                        .setOutputPath(audioFile.absolutePath)
                        //  .setStartTime("00:00:00")
                        .setStartTime(calculateStart)
                        .setEndTime(calculateEnd)
                        .setCallback(this@StartCountDownActivity)
                        .main()


                } catch (e: Exception) {

                    // log the error and try to create a .wav file instead
                    if (audioFile.exists()) {
                        audioFile.delete()
                    }
                    hideProgress()
                }

                // Try to load the new file to make sure it worked
                try {
                    val listener: SoundFile.ProgressListener = SoundFile.ProgressListener {
                        // Do nothing - we're not going to try to
                        // estimate when reloading a saved sound
                        // since it's usually fast, but hard to
                        // estimate anyway.
                        true // Keep going
                    }
                    SoundFile.create("$audioFile", listener)
                } catch (e: java.lang.Exception) {
                    hideProgress()
                    e.printStackTrace()
                    return
                }

                hideProgress()

            }
        }

        mSaveSoundFileThread!!.start()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.v("Ringdroid", "EditActivity onConfigurationChanged")
        val saveZoomLevel = mWaveformView.zoomLevel
        super.onConfigurationChanged(newConfig)
        loadUi()

        mHandler!!.postDelayed({
            mStartMarker.requestFocus()
            markerFocus(mStartMarker)
            mWaveformView.zoomLevel = saveZoomLevel
            mWaveformView.recomputeHeights(mDensity)
            updateDisplay()
        }, 500)
    }

    private fun loadUi() {
        mSoundFile = null
        mKeyDown = false
        mHandler = Handler()
        mWaveformView.setListener(this)

        mMaxPos = 0
        mLastDisplayedStartPos = -1
        mLastDisplayedEndPos = -1

        if (mSoundFile != null && !mWaveformView.hasSoundFile()) {
            mWaveformView.setSoundFile(mSoundFile)
            mWaveformView.recomputeHeights(mDensity)
            mMaxPos = mWaveformView.maxPos()
        }

        mStartMarker.setListener(this)
        mStartMarker.alpha = 1f
        mStartMarker.isFocusable = true
        mStartMarker.isFocusableInTouchMode = true
        mStartVisible = true

        mEndMarker.setListener(this)
        mEndMarker.alpha = 1f
        mEndMarker.isFocusable = true
        mEndMarker.isFocusableInTouchMode = true
        mEndVisible = true

        updateDisplay()
    }

    private val mTimerRunnable: Runnable = object : Runnable {
        override fun run() {
            // Updating an EditText is slow on Android.  Make sure
            // we only do the update if the text has actually changed.
            if (mStartPos != mLastDisplayedStartPos &&
                !mStartText.hasFocus()
            ) {
                mStartText.text = formatTime(mStartPos)
                mLastDisplayedStartPos = mStartPos
            }
            if (mEndPos != mLastDisplayedEndPos &&
                !mEndText.hasFocus()
            ) {
                mEndText.text = formatTime(mEndPos)
                mLastDisplayedEndPos = mEndPos
            }
            mHandler!!.postDelayed(this, 100)
        }
    }


    private fun loadFromFile() {

        mFile = File(mySongFilePath)
        Log.e("mySongFilePath", " $mySongFilePath ")
        Log.e("mFile", " $mFile ")
        songFileMetadataReader = SongMetadataReader(
            this, mySongFilePath
        )
        /* mTitle = metadataReader.mTitle
         mArtist = metadataReader.mArtist
         var titleLabel: String = mTitle
         if (mArtist != null && mArtist.length > 0) {
             titleLabel += " - $mArtist"
         }
         title = titleLabel*/

        mLoadingLastUpdateTime = getCurrentTime()
        mLoadingKeepGoing = true
        mFinishActivity = false
        try {
            mProgressDialog = ProgressDialog(this@StartCountDownActivity)
            //  mProgressDialog!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            mProgressDialog!!.setTitle("Loading...")
            mProgressDialog!!.setCancelable(true)
            mProgressDialog!!.setOnCancelListener(
                DialogInterface.OnCancelListener {
                    mLoadingKeepGoing = false
                    mFinishActivity = true
                })
            mProgressDialog!!.show()
            val listener: SoundFile.ProgressListener =
                SoundFile.ProgressListener { fractionComplete ->
                    val now: Long = getCurrentTime()
                    if (now - mLoadingLastUpdateTime > 100) {
                        mProgressDialog!!.progress =
                            (mProgressDialog!!.max * fractionComplete).toInt()
                        mLoadingLastUpdateTime = now
                    }
                    mLoadingKeepGoing
                }

            // Load the sound file in a background thread
            mLoadSoundFileThread = object : Thread() {
                override fun run() {
                    try {
                        mSoundFile = SoundFile.create(mFile!!.absolutePath, listener)
                        Log.e("mSoundFile", " $mSoundFile")
                        if (mSoundFile == null) {
                            mProgressDialog!!.dismiss()
                            val name: String = mFile!!.name.toLowerCase()
                            val components =
                                name.split("\\.".toRegex()).toTypedArray()
                            val err: String
                            err = if (components.size < 2) {
                                resources.getString(
                                    R.string.no_extension_error
                                )
                            } else {
                                resources.getString(
                                    R.string.bad_extension_error
                                ) + " " +
                                        components[components.size - 1]
                            }
                            val runnable =
                                Runnable {
                                    //showFinalAlert(Exception(), err.toInt())
                                }
                            mHandler!!.post(runnable)
                            return
                        }
                        // mPlayer = SamplePlayer(mSoundFile)
                    } catch (e: Exception) {
                        mProgressDialog!!.dismiss()
                        Log.e("EXCEPTION", " $e")
                        e.printStackTrace()
                        //mInfoContent = e.toString()
                        // runOnUiThread { mInfo.setText(mInfoContent) }
                        /*  val runnable =
                              Runnable { showFinalAlert(e, resources.getText(R.string.read_error)) }
                          mHandler!!.post(runnable)*/
                        return
                    }
                    mProgressDialog!!.dismiss()
                    if (mLoadingKeepGoing) {
                        val runnable = Runnable { finishOpeningSoundFile() }
                        mHandler!!.post(runnable)
                    } else if (mFinishActivity) {
                        this@StartCountDownActivity.finish()
                    }
                }
            }
            mLoadSoundFileThread!!.start()
        } catch (e: Exception) {
            Log.e("Exception", " $e ")
        }


    }

    private fun showFinalAlert(
        e: java.lang.Exception,
        messageResourceId: Int
    ) {
        //showFinalAlert(e, resources.getText(messageResourceId).toString().toInt())
    }

    private fun finishOpeningSoundFile() {

        Log.e("mWaveformView", " mWaveformView $mSoundFile")
        mWaveformView.setSoundFile(mSoundFile)
        mWaveformView.recomputeHeights(mDensity)
        mMaxPos = mWaveformView.maxPos()
        mLastDisplayedStartPos = -1
        mLastDisplayedEndPos = -1
        mTouchDragging = false
        mOffset = 0
        mOffsetGoal = 0
        mFlingVelocity = 0
        resetPositions()
        if (mEndPos > mMaxPos) mEndPos = mMaxPos
        mCaption = mSoundFile!!.filetype.toString() + ", " +
                mSoundFile!!.sampleRate + " Hz, " +
                mSoundFile!!.avgBitrateKbps + " kbps, " +
                formatTime(mMaxPos) + " " +
                "seconds"
        //  mInfo.setText(mCaption)
        updateDisplay()
    }

    private val mMarkerLeftInset = 0
    private val mMarkerRightInset = 0
    private val mMarkerTopOffset = 0

    @Synchronized
    private fun updateDisplay() {
        /*if (mIsPlaying) {
            val now: Int = mPlayer.getCurrentPosition()
            val frames = mWaveformView.millisecsToPixels(now)
            mWaveformView.setPlayback(frames)
            setOffsetGoalNoUpdate(frames - mWidth / 2)
            if (now >= mPlayEndMsec) {
                handlePause()
            }
        }*/
        if (!mTouchDragging) {
            var offsetDelta: Int
            if (mFlingVelocity != 0) {
                offsetDelta = mFlingVelocity / 30
                if (mFlingVelocity > 80) {
                    mFlingVelocity -= 80
                } else if (mFlingVelocity < -80) {
                    mFlingVelocity += 80
                } else {
                    mFlingVelocity = 0
                }
                mOffset += offsetDelta
                if (mOffset + mWidth / 2 > mMaxPos) {
                    mOffset = mMaxPos - mWidth / 2
                    mFlingVelocity = 0
                }
                if (mOffset < 0) {
                    mOffset = 0
                    mFlingVelocity = 0
                }
                mOffsetGoal = mOffset
            } else {
                offsetDelta = mOffsetGoal - mOffset
                offsetDelta =
                    if (offsetDelta > 10) offsetDelta / 10 else if (offsetDelta > 0) 1 else if (offsetDelta < -10) offsetDelta / 10 else if (offsetDelta < 0) -1 else 0
                mOffset += offsetDelta
            }
        }
        mWaveformView.setParameters(mStartPos, mEndPos, mOffset)
        mWaveformView.invalidate()
        mStartMarker.contentDescription =
            "Start Marker".toString() + " " +
                    formatTime(mStartPos)
        mEndMarker.contentDescription = "End Marker".toString() + " " +
                formatTime(mEndPos)
        var startX: Int = mStartPos - mOffset - mMarkerLeftInset
        if (startX + mStartMarker.width >= 0) {
            if (!mStartVisible) {
                // Delay this to avoid flicker
                mHandler!!.postDelayed({
                    mStartVisible = true
                    mStartMarker.alpha = 1f
                }, 0)
            }
        } else {
            if (mStartVisible) {
                mStartMarker.alpha = 0f
                mStartVisible = false
            }
            startX = 0
        }
        var endX: Int = mEndPos - mOffset - mEndMarker.width + mMarkerRightInset
        if (endX + mEndMarker.width >= 0) {
            if (!mEndVisible) {
                // Delay this to avoid flicker
                mHandler!!.postDelayed({
                    mEndVisible = true
                    mEndMarker.alpha = 1f
                }, 0)
            }
        } else {
            if (mEndVisible) {
                mEndMarker.alpha = 0f
                mEndVisible = false
            }
            endX = 0
        }
        var params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(
            startX - utils.getDimensionInPixel(this, 42),
            mMarkerTopOffset,
            0,
            0
        )
        mStartMarker.layoutParams = params
        params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(
            endX + utils.getDimensionInPixel(this, 42),
            mWaveformView.measuredHeight - mEndMarker.height, 0,
            0
        )
        mEndMarker.layoutParams = params
    }


    private fun resetPositions() {
        mStartPos = mWaveformView.secondsToPixels(0.0)
        mEndPos = mWaveformView.secondsToPixels(15.0)
    }

    private fun getCurrentTime(): Long {
        return System.nanoTime() / 1000000
    }

    private fun formatTime(pixels: Int): String? {
        return if (mWaveformView != null && mWaveformView.isInitialized) {
            formatDecimal(mWaveformView.pixelsToSeconds(pixels))
        } else {
            ""
        }
    }

    private fun formatDecimal(x: Double): String? {
        var xWhole = x.toInt()
        var xFrac = (100 * (x - xWhole) + 0.5).toInt()
        if (xFrac >= 100) {
            xWhole++ //Round up
            xFrac -= 100 //Now we need the remainder after the round up
            if (xFrac < 10) {
                xFrac *= 10 //we need a fraction that is 2 digits long
            }
        }
        return if (xFrac < 10) "$xWhole.0$xFrac" else "$xWhole.$xFrac"
    }

    private fun trap(pos: Int): Int {
        if (pos < 0) return 0
        return if (pos > mMaxPos) mMaxPos else pos
    }

    private val mIsPlaying = false
    override fun waveformDraw() {
        mWidth = mWaveformView.measuredWidth
        if (mOffsetGoal != mOffset && !mKeyDown) updateDisplay() else if (mIsPlaying) {
            updateDisplay()
        } else if (mFlingVelocity != 0) {
            updateDisplay()
        }
    }

    override fun waveformTouchEnd() {
        mTouchDragging = false
        mOffsetGoal = mOffset
        val elapsedMsec = getCurrentTime() - mWaveformTouchStartMsec
        if (elapsedMsec < 300) {
            if (mIsPlaying) {
                val seekMsec = mWaveformView.pixelsToMillisecs(
                    (mTouchStart + mOffset).toInt()
                )
                /*if (seekMsec in mPlayStartMsec until mPlayEndMsec) {
                    mPlayer.seekTo(seekMsec)
                } else {
                    handlePause()
                }*/
            } else {
                //onPlay((mTouchStart + mOffset).toInt())
            }
        }
    }

    override fun waveformZoomIn() {
        mWaveformView.zoomIn()
        mStartPos = mWaveformView.start
        mEndPos = mWaveformView.end
        mMaxPos = mWaveformView.maxPos()
        mOffset = mWaveformView.offset
        mOffsetGoal = mOffset
        updateDisplay()
    }

    override fun waveformZoomOut() {
        mWaveformView.zoomOut()
        mStartPos = mWaveformView.start
        mEndPos = mWaveformView.end
        mMaxPos = mWaveformView.maxPos()
        mOffset = mWaveformView.offset
        mOffsetGoal = mOffset
        updateDisplay()
    }

    override fun waveformTouchMove(x: Float) {
        mOffset = trap((mTouchInitialOffset + (mTouchStart - x)).toInt())
        updateDisplay()
    }

    override fun waveformFling(x: Float) {
        mTouchDragging = false
        mOffsetGoal = mOffset
        mFlingVelocity = -x.toInt()
        updateDisplay()
    }

    override fun waveformTouchStart(x: Float) {
        mTouchDragging = true
        mTouchStart = x
        mTouchInitialOffset = mOffset
        mFlingVelocity = 0
        mWaveformTouchStartMsec = getCurrentTime()
    }

    override fun markerRight(marker: MarkerView?, velocity: Int) {
        mKeyDown = true

        if (marker === mStartMarker) {
            val saveStart = mStartPos
            mStartPos += velocity
            if (mStartPos > mMaxPos) mStartPos = mMaxPos
            mEndPos += mStartPos - saveStart
            if (mEndPos > mMaxPos) mEndPos = mMaxPos
            setOffsetGoalStart()
        }

        if (marker === mEndMarker) {
            mEndPos += velocity
            if (mEndPos > mMaxPos) mEndPos = mMaxPos
            setOffsetGoalEnd()
        }

        updateDisplay()
    }

    override fun markerTouchStart(marker: MarkerView?, pos: Float) {
        mTouchDragging = true
        mTouchStart = pos
        mTouchInitialStartPos = mStartPos
        mTouchInitialEndPos = mEndPos
    }

    override fun markerFocus(marker: MarkerView?) {
        mKeyDown = false
        if (marker === mStartMarker) {
            setOffsetGoalStartNoUpdate()
        } else {
            setOffsetGoalEndNoUpdate()
        }

        // Delay updaing the display because if this focus was in
        // response to a touch event, we want to receive the touch
        // event too before updating the display.

        // Delay updaing the display because if this focus was in
        // response to a touch event, we want to receive the touch
        // event too before updating the display.
        mHandler!!.postDelayed({ updateDisplay() }, 100)
    }

    override fun markerLeft(marker: MarkerView?, velocity: Int) {
        mKeyDown = true

        if (marker === mStartMarker) {
            val saveStart = mStartPos
            mStartPos = trap(mStartPos - velocity)
            mEndPos = trap(mEndPos - (saveStart - mStartPos))
            setOffsetGoalStart()
        }

        if (marker === mEndMarker) {
            if (mEndPos == mStartPos) {
                mStartPos = trap(mStartPos - velocity)
                mEndPos = mStartPos
            } else {
                mEndPos = trap(mEndPos - velocity)
            }
            setOffsetGoalEnd()
        }

        updateDisplay()
    }

    override fun markerKeyUp() {
        mKeyDown = false
        updateDisplay()
    }

    override fun markerEnter(marker: MarkerView?) {

    }

    override fun markerTouchEnd(marker: MarkerView?) {
        mTouchDragging = false
        if (marker === mStartMarker) {
            setOffsetGoalStart()
        } else {
            setOffsetGoalEnd()
        }
    }

    override fun markerDraw() {

    }

    override fun markerTouchMove(marker: MarkerView?, x: Float) {
        val delta: Float = x - mTouchStart

        if (marker === mStartMarker) {
            mStartPos = trap((mTouchInitialStartPos + delta).toInt())
            mEndPos = trap((mTouchInitialEndPos + delta).toInt())

            Log.e("mStartPos", " $mStartPos")
            player!!.seekTo((mWaveformView.pixelsToSeconds(mStartPos) * 1000).toLong())
            player!!.playWhenReady = true

        } else {
            mEndPos = trap((mTouchInitialEndPos + delta).toInt())
            if (mEndPos < mStartPos) mEndPos = mStartPos
        }

        updateDisplay()
    }

    private fun setOffsetGoalStart() {
        setOffsetGoal(mStartPos - mWidth / 2)
    }

    private fun setOffsetGoalEnd() {
        setOffsetGoal(mEndPos - mWidth / 2)
    }

    private fun setOffsetGoal(offset: Int) {
        setOffsetGoalNoUpdate(offset)
        updateDisplay()
    }

    private fun setOffsetGoalNoUpdate(offset: Int) {
        if (mTouchDragging) {
            return
        }
        mOffsetGoal = offset
        if (mOffsetGoal + mWidth / 2 > mMaxPos) mOffsetGoal = mMaxPos - mWidth / 2
        if (mOffsetGoal < 0) mOffsetGoal = 0
    }

    private fun setOffsetGoalStartNoUpdate() {
        setOffsetGoalNoUpdate(mStartPos - mWidth / 2)
    }

    private fun setOffsetGoalEndNoUpdate() {
        setOffsetGoalNoUpdate(mEndPos - mWidth / 2)
    }

    private fun closeThread(thread: Thread?) {
        if (thread != null && thread.isAlive) {
            try {
                thread.join()
            } catch (e: InterruptedException) {
            }
        }
    }

    override fun onDestroy() {
        Log.v("Ringdroid", "EditActivity OnDestroy")
        mLoadingKeepGoing = false
        mRecordingKeepGoing = false
        closeThread(mLoadSoundFileThread)
        closeThread(mRecordAudioThread)
        closeThread(mSaveSoundFileThread)
        mLoadSoundFileThread = null
        mRecordAudioThread = null
        mSaveSoundFileThread = null
        if (mProgressDialog != null) {
            mProgressDialog!!.dismiss()
            mProgressDialog = null
        }
        /*if (mAlertDialog != null) {
            mAlertDialog.dismiss()
            mAlertDialog = null
        }
        if (mPlayer != null) {
            if (mPlayer.isPlaying() || mPlayer.isPaused()) {
                mPlayer.stop()
            }
            mPlayer.release()
            mPlayer = null
        }*/
        super.onDestroy()
    }

    override fun onProgress(progress: String) {

    }

    override fun onSuccess(convertedFile: File, type: String) {
        /*val actualDuration = TimeUnit.MICROSECONDS.toSeconds(mSoundFile!!.playDuration.toLong()).toInt()
        if ((actualDuration == 0 || actualDuration > 15) && duration < 15) {
            showSnackbar("Selected song too short")
        } else*/
        if (duration > 60) {
            showSnackbar("Selected song too long")
        } else {
            val ii = Intent()
            ii.putExtra("startTime", startTime)
            ii.putExtra("endTime", endTime)
            ii.putExtra("duration", duration)
            ii.putExtra("position", position)
            ii.putExtra("isUpload", isUpload)
            if (intent.hasExtra("selectedSong"))
                ii.putExtra("selectedSong", "")

            if (intent.hasExtra("songId"))
                ii.putExtra("songId", intent.getStringExtra("songId"))

            ii.putExtra("songName", songName)
            ii.putExtra("mySongFilePath", mySongFilePath)
            ii.putExtra("audioFile", convertedFile.path)
            setResult(Activity.RESULT_OK, ii)
            finish()
        }
    }

    override fun onFailure(error: Exception) {

    }

    override fun onNotAvailable(error: Exception) {

    }

    override fun onFinish() {

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

    override fun onStop() {
        super.onStop()
        if (player != null) {
            player!!.stop()
            player!!.release()
            player = null
        }
    }

    private fun setUpSimpleExoPlayer() {

        if (player == null) {
            initPlayer()

            player!!.apply {
                val userAgent =
                    Util.getUserAgent(this@StartCountDownActivity, getString(R.string.app_name))
                val mediaSource = ProgressiveMediaSource.Factory(
                    DefaultDataSourceFactory(this@StartCountDownActivity, userAgent),
                    DefaultExtractorsFactory()
                ).createMediaSource(Uri.parse(mySongFilePath))
                prepare(mediaSource!!, true, false)
                player!!.playWhenReady = true
                isPlayingSong = true
            }
        } else {
            player!!.playWhenReady = true
        }

    }
}