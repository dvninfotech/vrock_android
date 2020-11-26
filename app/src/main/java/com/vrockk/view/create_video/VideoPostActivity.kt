package com.vrockk.view.create_video

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.hendraanggrian.appcompat.widget.Hashtag
import com.hendraanggrian.appcompat.widget.HashtagArrayAdapter
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.adapter.RecommendedAdapter
import com.vrockk.api.ApiResponse
import com.vrockk.api.Status
import com.vrockk.base.BaseActivity
import com.vrockk.models.upload_post.UploadPostResponse
import com.vrockk.models.upload_post.gethashtags.Data
import com.vrockk.models.upload_post.gethashtags.GetHashTagsResponse
import com.vrockk.models.upload_song.UploadSongModel
import com.vrockk.utils.Utils
import com.vrockk.view.dashboard.DashboardActivity
import com.vrockk.viewmodels.Get_HashTags_ViewModel
import com.vrockk.viewmodels.UploadSongViewModel
import com.vrockk.viewmodels.viewmodels.CommonViewModel
import kotlinx.android.synthetic.main.activity_like.ibBack
import kotlinx.android.synthetic.main.activity_videopost.*
import kotlinx.android.synthetic.main.my_custom_alert.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class VideoPostActivity : BaseActivity() {

    var status = 0

    private val getHashtagsViewmodel by viewModel<Get_HashTags_ViewModel>()

    lateinit var recommendedAdapter: RecommendedAdapter
    private val commonViewModel by viewModel<CommonViewModel>()
    private val uploadSongViewModel by viewModel<UploadSongViewModel>()

    val hashMap : HashMap<String, RequestBody> = HashMap()
    val songUploadHashMap : HashMap<String, RequestBody> = HashMap()

    private var absolutePath = ""
    private var songId = ""
    var listOfStrings = ArrayList<String>()

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    var locationRequest: LocationRequest? = null
    var locationCallback: LocationCallback? = null
    var fusedLocationProviderClient: FusedLocationProviderClient? = null

    private var songName = ""
    private var isUpload = false
    private var selectedSong = ""
    private var type = ""

    var recorded = ""
    var target = ""

    companion object{
        private const val VIDEO_DIRECTORY_NAME = "DuetApp"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_videopost)

        getLastKnownLocation()

        type = intent.getStringExtra("type")?:""

        if (type.equals("duet"))
        {
            clTags.visibility = View.GONE
            target = intent.getStringExtra("Target")
            recorded = intent.getStringExtra("Recorded")

            val mediaStorageDir = File(
                Environment.getExternalStorageDirectory(),
                VIDEO_DIRECTORY_NAME
            )
            val timeStamp = SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
            ).format(Date())
            val duetName = (mediaStorageDir.path + File.separator
                    + "DUET_VID_" + timeStamp + ".mp4")
            FFmpeg.executeAsync("-i $recorded -i $target -filter_complex [0:v][1:v]hstack=inputs=2:shortest=1[outv]  -r 25 -b:v 8M -minrate 6M -maxrate 8M -bufsize 4M  -map 1:a? -shortest -map [outv] $duetName"
            ) { executionId, returnCode ->
                if (returnCode == Config.RETURN_CODE_SUCCESS) {
                  //  showToast("Duet saved")
                    try{
                        progressBarr.visibility = View.GONE
                        Log.e("call", "path: $duetName")
                        Glide.with(this)
                            .load(duetName)
                            .into(previewIMg);
                        Log.i(
                            Config.TAG,
                            "Command execution completed successfully."
                        )
                        clTags.visibility = View.VISIBLE
                    }catch (e: Exception){

                    }

                } else if (returnCode == Config.RETURN_CODE_CANCEL) {
                    Log.i(
                        Config.TAG,
                        "Command execution cancelled by user."
                    )
                } else {
                    Log.i(
                        Config.TAG,
                        String.format(
                            "Command execution failed with rc=%d and the output below.",
                            returnCode
                        )
                    )
                    Config.printLastCommandOutput(Log.INFO)
                }
            }

            tvPostNow.setOnClickListener {

                if (edDesciption.text.toString().equals(""))
                {
                    showConfirmPopup()
                }
                else
                {
                    listOfStrings.addAll(getHashTags(edDesciption.text.toString())!!)
                    Log.e("Hash_"," TAGs ${listOfStrings}")
                    hitApi(duetName)
                }
            }

        }
        else
        {
            progressBarr.visibility = View.GONE
            absolutePath = intent.getStringExtra("DATA")?:""
            songId = intent.getStringExtra("songId")?:""

            if(intent.hasExtra("isUpload")){
                isUpload = intent.getBooleanExtra("isUpload",false)
                if(isUpload){
                    songName = intent.getStringExtra("songName")?:""
                    selectedSong = intent.getStringExtra("selectedSong")?:""
                    Log.e("selectedSong", " video post activity $selectedSong")
                    hitUploadSongApi()
                }

            }

            Glide.with(this)
                .load(absolutePath)
                .into(previewIMg);

            tvPostNow.setOnClickListener {

                if (edDesciption.text.toString().equals(""))
                {
                    showConfirmPopup()
                }
                else
                {
                    listOfStrings.addAll(getHashTags(edDesciption.text.toString())!!)
                    Log.e("Hash_"," TAGs ${listOfStrings}")
                    if(absolutePath.isEmpty()){
                        showSnackbar("Please add video file")
                    }
                    else
                    {
                            hitApi("")
                    }
                }
            }
        }

        init()
        initHashTags()
        observerApi()

    }


    fun showConfirmPopup() {
        val myCustomDlg = Dialog(this)
        myCustomDlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        myCustomDlg.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        myCustomDlg.setContentView(R.layout.my_custom_alert)
        myCustomDlg.tvAlertMessage.text = resources.getString(R.string.please_enter_description)
        myCustomDlg.tvTitleCustom.text = resources.getString(R.string.alert)
        myCustomDlg.positiveBtn.text = resources.getString(R.string.ok)
        myCustomDlg.noBtn.visibility = View.GONE
        myCustomDlg.show()


        myCustomDlg.positiveBtn.setOnClickListener {
            myCustomDlg.dismiss()
        }

//        AlertDialog.Builder(this).setTitle("Alert")
//            .setMessage("Please enter description")
//            .setPositiveButton(getString(R.string.ok)
//            ) { p0, p1 ->
//                p0.dismiss()
//
//            }
//            .show()
    }



    private fun initHashTags() {
        edDesciption.isHashtagEnabled = true
        edDesciption.setOnHashtagClickListener { view, text ->

        }

    }

    fun getHashTags(str: String?): List<String>? {
        val MY_PATTERN : Pattern = Pattern.compile("#(\\S+)")
        val mat: Matcher = MY_PATTERN.matcher(str!!)
        val strs: MutableList<String> = ArrayList()
        while (mat.find()) {
            strs.add("#"+mat.group(1)!!)
        }
        return strs
    }

    private fun initHasTagAdapter(data: ArrayList<Data>) {
        val hashtagAdapter: ArrayAdapter<Hashtag> = HashtagArrayAdapter(this)


        for (i in 0 until data.size)
        {
            hashtagAdapter.add(Hashtag(data.get(i)._id.replace("#",""),0))
        }
        edDesciption.hashtagAdapter = hashtagAdapter
    }


    private fun observerApi() {
//        commonViewModel.uploadPostResponseLiveData().observe(this, Observer<ApiResponse> { response ->
//            this.uploadData(response,1)
//        })

        uploadSongViewModel.uploadSongResponse().observe(this, Observer<ApiResponse> { response ->
            this.uploadData(response,2)
        })

        getHashtagsViewmodel.get_HashTagsResponse().observe(this, Observer<ApiResponse> { response ->
            this.uploadData(response,3)
        })

        getHashtagsViewmodel.hitGet_HashTags()
    }


    private fun hitApi(duetName: String) {

        val i = Intent(this, DashboardActivity::class.java)
        i.putExtra("absolutePath",absolutePath)
        i.putExtra("description_text",edDesciption.text!!.toString())
        i.putExtra("song_id",songId)
        i.putExtra("latitude",latitude.toString())
        i.putExtra("longitude",longitude.toString())
        i.putExtra("hashtaglist",listOfStrings.toString().replace("[","").replace("]",""))

        if(isUpload)
            i.putExtra("uploaded","true")
        else
            i.putExtra("uploaded","false")

        if (type.equals("duet"))
            i.putExtra("path",""+duetName)
        else
            i.putExtra("path",""+absolutePath)

        i.putExtra("type",type)

        i.putExtra("upload","upload")

        startActivity(i)

        /*

            val token  = "SEC "+ VrockkApplication.user_obj!!.authToken
            val requestFile = RequestBody.create(MediaType.parse("video/mp4"), File(absolutePath))
            val text = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), edDesciption.text!!.toString())
            val songIdd = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), songId)
            val lat = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), latitude.toString())
            val lng = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), longitude.toString())
            val hash = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), listOfStrings.toString().replace("[","").replace("]",""))
            var uploaded : RequestBody ? =null
            if(isUpload)
                uploaded = RequestBody.create(MediaType.parse("application/json"), "true")
            else
                uploaded = RequestBody.create(MediaType.parse("application/json"), "false")

            if (type.equals("duet"))
            {
                val videoPath = "video\"; filename=\"$duetName"
                val requestFile = RequestBody.create(MediaType.parse("video/mp4"), File(duetName))
                hashMap[videoPath] = requestFile
            }
            else
            {
                val videoPath = "video\"; filename=\"stitchlrvideo$absolutePath"
                hashMap[videoPath] = requestFile
            }

            hashMap["description"] = text
            hashMap["hashtags"] = hash
            hashMap["songId"] = songIdd
            hashMap["isUploaded"] = uploaded
            hashMap["latitude"] = lat
            hashMap["longitude"] = lng
            Log.e("songId"," $songId")
            Log.e("hashMap", hashMap.toString())
            commonViewModel.uploadPost(token,hashMap)

         */

    }

    private fun hitUploadSongApi() {
        val token  = "SEC "+ VrockkApplication.user_obj!!.authToken
        val songPath = "song\"; filename=\"stitchlrvideo$selectedSong"
        val requestFile = RequestBody.create(MediaType.parse("audio/*"), File(selectedSong))

        val json  = JSONObject()
        json.put("name", songName)
        json.put("artist", "")

        val songDetail = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),json.toString())
        val uploadedBy = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),"user")

        songUploadHashMap[songPath] = requestFile
        songUploadHashMap["description"] = songDetail
        songUploadHashMap["uploadedBy"] = uploadedBy
        uploadSongViewModel.uploadSong(token,songUploadHashMap)
    }

    private fun uploadData(response: ApiResponse?, type: Int) {
        when (response!!.status) {
            Status.LOADING -> {
                if(type == 1)
                    showProgress("")

                //  showProgress("")

            }
            Status.SUCCESS -> {
                if(type == 1)
                   hideProgress()

                renderResponse(response,type)
            }
            Status.ERROR -> {
                if(type == 1)
                   hideProgress()
                Log.e("response", Gson().toJson(response.error))
                //showCustomToastMessage(this, "Something went wrong!")
            }
        }
    }

    fun showBackgroundLoader()
    {
//        Thread(Runnable {
//            while (status < 100) {
//                status += 1
//                try {
//                    Thread.sleep(200)
//                } catch (e: InterruptedException) {
//                    e.printStackTrace()
//                }
//                handler.post(Runnable {
//                    circularProgressbar.progress = status
//                    tvPercentage.setText(""+status+"%")
//                    if (status === 100) {
//                    }
//                })
//            }
//        }).start()
    }

    fun init()
    {
        setUpRecyclerviews()
        ibBack.setOnClickListener { finish() }

    }

    private fun setUpRecyclerviews() {

    }


    private fun renderResponse(response: ApiResponse, type: Int) {
        if(response != null){
            Log.e("Upload_Response: ", Gson().toJson(response))
            val data: String = Utils.toJson(response.data)
            val gson1 = Gson()
            if(type == 1){
                val uploadPostResponse = gson1.fromJson(data, UploadPostResponse::class.java)
                if (uploadPostResponse.success!!) {

                    DashboardActivity.Companion.classType = "home"
                    navigateFinishAffinity(DashboardActivity::class.java)
                }
                else {
                    showSnackbar(""+uploadPostResponse.message)
                }
            }

            if (type == 2){
                val uploadPostResponse = gson1.fromJson(data, UploadSongModel::class.java)
                if(uploadPostResponse.success!!){
                    songId = uploadPostResponse?.data?.id!!
                }
            }

            if (type == 3)
            {
                val data: String = Utils.toJson(response.data)
                val gson1 = Gson();
                val getHashTagsResponse = gson1.fromJson(data, GetHashTagsResponse::class.java)
                if (getHashTagsResponse.success!!) {

                    if (getHashTagsResponse.data!!.size > 0)
                    {

                        initHasTagAdapter(getHashTagsResponse.data!!)

                        tvRecommended.visibility = View.VISIBLE
                        //tvChallenge.visibility = View.VISIBLE

                        recommendedAdapter = RecommendedAdapter(this,getHashTagsResponse.data!!)

                        val layoutManager = FlexboxLayoutManager(this)
                        layoutManager.flexDirection = FlexDirection.ROW
                        layoutManager.justifyContent = JustifyContent.FLEX_START
                        rvRecommended.layoutManager = layoutManager
                        rvRecommended.itemAnimator = DefaultItemAnimator()
                        rvRecommended.adapter = recommendedAdapter

                        recommendedAdapter = RecommendedAdapter(this, getHashTagsResponse.data!!)

                        val layoutManager_ = FlexboxLayoutManager(this)
                        layoutManager_.flexDirection = FlexDirection.ROW
                        layoutManager_.justifyContent = JustifyContent.FLEX_START
                        rvChallengeTags.layoutManager = layoutManager_
                        rvChallengeTags.itemAnimator = DefaultItemAnimator()
                        rvChallengeTags.adapter = recommendedAdapter

                    }
                    else
                    {
                        tvRecommended.visibility = View.INVISIBLE
                        tvChallenge.visibility = View.INVISIBLE
                    }
                }
                else {

                }
            }


        }
    }

    fun getTags(tags:String)
    {
        var tags = tags.replace("##","#")
        edDesciption.setText(edDesciption.text.toString()+" "+tags)
    }


    /*-------Method to Get Location-------------*/
    fun getLastKnownLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
        locationRequest!!.interval = 100;
        // locationRequest!!.setFastestInterval(1500)
        initLocationCallback()
    }


    @SuppressLint("MissingPermission")
    private fun initLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                val  mylocation = locationResult!!.lastLocation
                Log.e("CURRENT"," lat : Lng :" +mylocation!!.latitude+" "+ mylocation.longitude )
                latitude = mylocation.latitude
                longitude = mylocation.longitude

            }
        }
        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        );
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

}