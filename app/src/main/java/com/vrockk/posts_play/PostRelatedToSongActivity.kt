package com.vrockk.posts_play

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.adapter.PostsRelatedSongsAdapter
import com.vrockk.api.Event
import com.vrockk.base.BaseActivity
import com.vrockk.common.onfailure
import com.vrockk.interfaces.ItemClickListernerWithType
import com.vrockk.models.home.home_page.Data
import com.vrockk.models.home.home_page.HomeResponse
import com.vrockk.view.cameraactivity.RecordVideosActivity
import com.vrockk.view.posts_play.PostsPlayAcivity
import com.vrockk.viewmodels.viewmodels.HomePageViewModel
import kotlinx.android.synthetic.main.activity_search_hashtag.*
import kotlinx.android.synthetic.main.layout_loader.*
import org.koin.android.viewmodel.ext.android.viewModel


class PostRelatedToSongActivity : BaseActivity(), ItemClickListernerWithType {

    private var hashTagsList = ArrayList<Data>()
    lateinit var profileVideosAdapter: PostsRelatedSongsAdapter
    private var songId = ""
    private var songName = ""
    private var uploadedBy = ""
    private var song = ""
    private var parentPosition = 0

    val homePageViewModel by viewModel<HomePageViewModel>()

    var locationRequest: LocationRequest? = null
    var locationCallback: LocationCallback? = null
    var fusedLocationProviderClient: FusedLocationProviderClient? = null

    var isFetchLocation: Boolean = true

    companion object {
        var latitude_: Double = 0.0
        var longitude_: Double = 0.0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_hashtag)

        initProgress(contentLoadingProgressBar)
        showProgress("")

        latitude_ = 0.0
        longitude_ = 0.0

        txtCreate.visibility = View.VISIBLE

        textViewHeader.visibility = View.INVISIBLE
        profile_image.visibility = View.VISIBLE

        if (intent.hasExtra("songId"))
            songId = intent.getStringExtra("songId")!!

        if (intent.hasExtra("songName"))
            songName = intent.getStringExtra("songName")!!

        if (intent.hasExtra("uploadedBy"))
            uploadedBy = intent.getStringExtra("uploadedBy") ?: ""

        if (intent.hasExtra("song"))
            song = intent.getStringExtra("song") ?: ""

        tvHashTag.text = songName

        //tvViews.text = ""
        init()
        setUpRecyclerviews()
        observerApi()

        getLastKnownLocation()
    }

    private fun hitApi() {
        Log.e("songId", " $songId ")
        Log.e("user id", " $VrockkApplication.user_obj!!._id ")
        showProgress()
        if (VrockkApplication.user_obj != null)
            homePageViewModel.getV2HomeFeedsWithoutPostKey(
                1, 15, VrockkApplication.user_obj!!._id,
                latitude_, longitude_, songId, ""
            )
        else
            homePageViewModel.getV2HomeFeedsWithoutPostKey(
                1, 15, "",
                latitude_, longitude_, songId, ""
            )
    }

    fun init() {
        //hideStatusBar()
        ibBack.setOnClickListener { finish() }
        txtCreate.setOnClickListener {
            if (VrockkApplication.user_obj != null) {
                setupPermissions("video")
            } else {
                showLoginPopup()
            }
        }
    }

    private fun setUpRecyclerviews() {
        profileVideosAdapter = PostsRelatedSongsAdapter(this, hashTagsList, this)
        rvHashtag.adapter = profileVideosAdapter
        rvHashtag.setHasFixedSize(true)
        rvHashtag.layoutManager = GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false)
    }

    private fun observerApi() {
        homePageViewModel.getResponseWithoutPostKeyLiveData()
            .observe(this, Observer<Event<HomeResponse>> { response ->
                response.getContentIfNotHandled()?.let {
                    hideProgress()
                    homeResponse(it)
                }
            })
    }

    private fun homeResponse(response: HomeResponse) {

        Log.e("home_response: ", Gson().toJson(response))

        if (response.success) {
            hashTagsList.clear()
            hashTagsList.addAll(response.data)
            if (hashTagsList.isNotEmpty()) {
                txtCreate.visibility = View.VISIBLE
                /*if(uploadedBy == "user"){

                }*/
            }

            profileVideosAdapter.notifyDataSetChanged()

            if (hashTagsList.size > 0) {
                ivNoPost.visibility = View.GONE
                tvNoVideo.visibility = View.GONE
            } else {
                ivNoPost.visibility = View.VISIBLE
                tvNoVideo.visibility = View.VISIBLE
                ivNoPost.setImageResource(R.drawable.no_song)
            }

        }
    }


    /*private fun renderResponse(response: ApiResponse) {
        Log.e("Upload_Response: ", Gson().toJson(response))
        val data: String = Utils.toJson(response.data)
        val gson1 = Gson()
        val model = gson1.fromJson(data, HashTagsPostModel::class.java)
        if(model.success!!){
            hashTagsList.clear()
            hashTagsList.addAll(model?.data!!)
            profileVideosAdapter.notifyDataSetChanged()
        }
    }*/

    override fun onItemClicked(position: Int, type: String) {
        startActivity(
            Intent(this, PostsPlayAcivity::class.java)
                .putExtra("songId", songId)
                .putExtra("position", position)
                .putExtra("postId", hashTagsList[position]._id)
        )
    }

    override fun onPause() {
        super.onPause()
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
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
                val mylocation = locationResult!!.lastLocation
                Log.e(
                    "CURRENT",
                    " lat : Lng :" + mylocation!!.latitude + " " + mylocation.longitude
                )
                latitude_ = mylocation.latitude
                longitude_ = mylocation.longitude

                if (isFetchLocation) {
                    hitApi()
                    isFetchLocation = false
                }

            }
        }
        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        );
    }

    public fun setupPermissions(s: String) {
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) + ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) + ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) + ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@PostRelatedToSongActivity,
                    Manifest.permission.CAMERA
                )
                && ActivityCompat.shouldShowRequestPermissionRationale(
                    this@PostRelatedToSongActivity,
                    Manifest.permission.RECORD_AUDIO
                ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this@PostRelatedToSongActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this@PostRelatedToSongActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                onfailure(
                    this,
                    getString(R.string.pleasegiveallrequiredpermissions),
                    getString(R.string.gotosettings),
                    this@PostRelatedToSongActivity
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    1
                )
            }
        } else {
            Log.e("call", "11111 song: " + song)
            Log.e("call", "11111 song: " + songName)

            startActivity(
                Intent(this@PostRelatedToSongActivity, RecordVideosActivity::class.java)
                    .putExtra("song", song)
                    .putExtra("songName", songName)
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        if (requestCode == 1) {

            Log.e("call", "11111 song: " + song)
            Log.e("call", "11111 song: " + songName)

            // If request is cancelled, the result arrays are empty.
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(
                    Intent(this@PostRelatedToSongActivity, RecordVideosActivity::class.java)
                        .putExtra("song", song)
                        .putExtra("songId", songId)
                        .putExtra("songName", songName)
                )

            } else {

            }
        }
    }

}
