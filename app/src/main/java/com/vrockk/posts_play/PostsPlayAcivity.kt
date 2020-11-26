package com.vrockk.view.posts_play

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.gms.location.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.adapter.GiftsAdapter
import com.vrockk.adapter.PostPlayAdapter
import com.vrockk.api.ApiResponse
import com.vrockk.api.BASE_URL
import com.vrockk.api.Event
import com.vrockk.api.Status
import com.vrockk.api.parsers.HomeParser
import com.vrockk.api.parsers.ProfileParser
import com.vrockk.api.parsers.SettingsParser
import com.vrockk.base.BaseActivity
import com.vrockk.interfaces.ItemClickListernerWithType
import com.vrockk.models.coins.CoinsModel
import com.vrockk.models.home.home_page.Data
import com.vrockk.models.home.home_page.HomeResponse
import com.vrockk.posts_play.PostRelatedToSongActivity
import com.vrockk.utils.Constant
import com.vrockk.utils.Utils
import com.vrockk.view.comment.CommentActivity
import com.vrockk.view.dashboard.DashboardActivity
import com.vrockk.view.duet.DuetMainActivity
import com.vrockk.view.fragment.GiftsBSFragment
import com.vrockk.view.home.HomeFragment
import com.vrockk.view.profile.OtherProfileActivity
import com.vrockk.viewmodels.*
import com.vrockk.viewmodels.viewmodels.CommonViewModel
import com.vrockk.viewmodels.viewmodels.HomePageViewModel
import com.vrockk.viewmodels.viewmodels.SettingsViewModel
import kotlinx.android.synthetic.main.activity_posts_play_acivity.*
import kotlinx.android.synthetic.main.dialog_confirmation_report.*
import kotlinx.android.synthetic.main.dialog_report.*
import kotlinx.android.synthetic.main.dialog_share.*
import kotlinx.android.synthetic.main.layout_loader.*
import org.koin.android.viewmodel.ext.android.viewModel
import kotlin.math.ceil

class PostsPlayAcivity : BaseActivity(), ItemClickListernerWithType {

    private var totalPages: Int = 0
    private var pageNo: Int = 1
    var isLoading = false

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var bottomSheetBehaviorForMore: BottomSheetBehavior<ConstraintLayout>
    private lateinit var giftsAdapter: GiftsAdapter
    private var coinList = ArrayList<CoinsModel>()

    private var coinImgs = arrayListOf(
        R.drawable.ic_copper, R.drawable.ic_bronze,
        R.drawable.ic_silver, R.drawable.ic_gold, R.drawable.ic_platinum, R.drawable.ic_crown
    )
    private var coinNames = arrayListOf("Copper", "Bronze", "Silver", "Gold", "Platinum", "Crown")
    private var coinCount = arrayListOf("10", "50", "100", "500", "500", "1000")

    private lateinit var giftsBSFragment: GiftsBSFragment

    lateinit var instanceObj: Activity

    companion object {
        var index: Int = 0
        var isHomeVisible: Boolean = false
        var player: SimpleExoPlayer? = null
    }

    var listPosts = ArrayList<Data>()
    var postPlayAdapter: PostPlayAdapter? = null

    private val homePageViewModel by viewModel<HomePageViewModel>()
    private val getVideoSearchViewModel by viewModel<GetVideoSearchViewModel>()
    private val commonViewModel by viewModel<CommonViewModel>()
    val getFollowingViewModel by viewModel<FollowingViewModel>()
    private val likePostViewModel by viewModel<LikePostViewModel>()
    val addViewToPostViewModel by viewModel<AddViewToPostViewModel>()
    private val followUnfollowViewModel by viewModel<FollowUnfollowViewModel>()

    private val settingsViewModel by viewModel<SettingsViewModel>()
    val profilePageViewModel by viewModel<ProfilePageViewModel>()
    private val reportViewModel by viewModel<ReportViewModel>()

    private var hashTag = ""
    private var postId = ""
    private var songId = ""
    private var position = 0

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    var locationRequest: LocationRequest? = null
    var locationCallback: LocationCallback? = null
    var fusedLocationProviderClient: FusedLocationProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts_play_acivity)

        initProgress(contentLoadingProgressBar)

        giftsBSFragment = GiftsBSFragment(this)

        getLastKnownLocation()
        isHomeVisible = true
        instanceObj = this
        index = 0

        if (intent.hasExtra("hashTag")) {
            hashTag = intent.getStringExtra("hashTag")!!
            position = intent.getIntExtra("position", 0)
        }
        if (intent.hasExtra("songId")) {
            songId = intent.getStringExtra("songId")!!
        }
        postId = intent.getStringExtra("postId")!!

        handleBottomSheetSlide()
        handleBottomSheetSlideForMore()
        setCoinAdapter()

        Log.e("postId", " $postId ")

        observer()
        ibBack.setOnClickListener {
            if (player != null) {
                player?.playWhenReady = false
                player?.stop()
                player!!.release()
                player = null
            }

            if (isTaskRoot) {
                startActivity(Intent(this@PostsPlayAcivity, DashboardActivity::class.java))
                finishAffinity()
            } else {
                finish()
            }
        }

        index = 0
        Handler().postDelayed({
            hitApi()
        }, 150)
    }

    override fun onResume() {
        super.onResume()

        if (postPlayAdapter != null)
            postPlayAdapter?.notifyDataSetChanged()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun observer() {

        addViewToPostViewModel.uploadSongResponse()
            .observe(this, Observer<ApiResponse> { response ->
                this.dataResponse(6, response)
            })


        reportViewModel.reportResponse().observe(this, Observer<ApiResponse> { response ->
            this.dataResponse(5, response)
        })

        profilePageViewModel.profilePageResponse().observe(this, Observer<ApiResponse> { response ->
            this.dataResponse(4, response)
        })

        commonViewModel.hashTagsResponseLiveData().observe(this, Observer<ApiResponse> { response ->
            this.dataResponse(1, response)
        })

        likePostViewModel.likePostResponse().observe(this, Observer<ApiResponse> { response ->
            this.dataResponse(2, response)
        })

        followUnfollowViewModel.followUnfollowResponse()
            .observe(this, Observer<ApiResponse> { response ->
                this.dataResponse(3, response)
            })
        homePageViewModel.getResponseWithoutPostKeyLiveData()
            .observe(this, Observer<Event<HomeResponse>> { response ->
                response.getContentIfNotHandled()?.let {
                    hideProgress()
                    homeResponse(it)
                }
            })
        getVideoSearchViewModel.homeResponse()
            .observe(this, Observer<Event<HomeResponse>> { response ->
                response.getContentIfNotHandled()?.let {
                    hideProgress()
                    homeResponse(it)
                }
            })

        settingsViewModel.shortenUrlResponseLiveData().observe(this, Observer {
            val shortUrlData = SettingsParser.parseShortUrlResponse(it.data)
            if (shortUrlData != null) {
                shareDeepLinkUrl(shortUrlData.shortUrl)
            }
        })
    }

    private fun homeResponse(homeResponse: HomeResponse) {
        isLoading = false
        if (homeResponse.success) {
            if (homeResponse.data.size > 0) {

                val pagesFromApi = homeResponse.total / 5.0
                totalPages = ceil(pagesFromApi).toInt()
                Log.e("totalPages", " $totalPages")

                if (pageNo == 1) {
                    listPosts.clear()
                    listPosts.addAll(homeResponse.data)
                    postPlayAdapter = PostPlayAdapter(this, listPosts, instanceObj, this)
                    rvHome.adapter = postPlayAdapter
                    rvHome.layoutManager =
                        LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                } else {
                    listPosts.addAll(homeResponse.data)
                }

                val snapHelper = PagerSnapHelper()
                rvHome.onFlingListener = null
                snapHelper.attachToRecyclerView(rvHome)
                rvHome.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            try {
                                val centerView = snapHelper.findSnapView(rvHome.layoutManager)
                                val pos = rvHome.layoutManager!!.getPosition(centerView!!)
                                Log.e("call", "itemPosition : $pos")
                                player!!.playWhenReady = false
                                index = pos
                                postPlayAdapter?.notifyDataSetChanged()
                            } catch (exp: Exception) {

                            }
                        }
                    }

                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        val layoutManager =
                            LinearLayoutManager::class.java.cast(recyclerView.layoutManager)
                        val totalItemCount = layoutManager!!.itemCount
                        val lastVisible = layoutManager.findLastVisibleItemPosition()
                        val endHasBeenReached = lastVisible + 2 >= totalItemCount
                        if (totalItemCount > 0 && endHasBeenReached) {
                            if (pageNo < totalPages && !isLoading) {

                                pageNo++
                                isLoading = true
                                if (songId.isNotEmpty()) {
                                    //showProgress("")
                                    homePageViewModel.getV2HomeFeeds(
                                        pageNo,
                                        5,
                                        VrockkApplication.user_obj?.id ?: "",
                                        PostRelatedToSongActivity.latitude_,
                                        PostRelatedToSongActivity.longitude_,
                                        songId,
                                        postId
                                    )
                                } else if (intent.hasExtra("search") || intent.hasExtra("notification")) {
                                    //showProgress("")
                                    getVideoSearchViewModel.getHomePageWithoutSong(
                                        pageNo,
                                        5,
                                        VrockkApplication.user_obj?._id!!,
                                        postId
                                    )
                                }
                            }
                        }
                    }
                })
            }

        }
    }

    override fun onBackPressed() {
        if (player != null) {
            player?.playWhenReady = false
            player?.stop()
            player!!.release()
            player = null
        }

        // finish()
        if (isTaskRoot) {
            startActivity(Intent(this@PostsPlayAcivity, DashboardActivity::class.java))
            finishAffinity()
        } else {
            finish()
        }
    }

    private fun hitApi() {
        index = 0
        if (hashTag.isNotEmpty())
            commonViewModel.getHashTags(
                1,
                10,
                hashTag,
                postId,
                VrockkApplication.user_obj?._id ?: ""
            )
        else if (songId.isNotEmpty()) {

            showProgress("")
            if (VrockkApplication.user_obj != null)
                homePageViewModel.getV2HomeFeedsWithoutPostKey(
                    1,
                    5,
                    VrockkApplication.user_obj?._id ?: "",
                    PostRelatedToSongActivity.latitude_,
                    PostRelatedToSongActivity.longitude_,
                    songId,
                    postId
                )
            else
                homePageViewModel.getV2HomeFeedsWithoutPostKey(
                    1,
                    5,
                    "",
                    latitude,
                    longitude,
                    songId,
                    postId
                )

        } else if (intent.hasExtra("search") || intent.hasExtra("notification")) {
            showProgress("")
            Log.e("postId", " $postId  without song")
            //  homePageViewModel.getHomePage(1,15,VrockkApplication.user_obj?.id?:"",latitude,longitude,songId,postId)
            if (VrockkApplication.user_obj != null) {
                getVideoSearchViewModel.getHomePageV2WithoutSongRadixedWithoutPostKey(
                    1, 5, VrockkApplication.user_obj?._id!!, postId
                )
            } else {
                getVideoSearchViewModel.getHomePageV2WithoutSongRadixedWithoutPostKey(
                    1, 5, "", postId
                )
            }
        }
    }

    private fun dataResponse(type: Int, response: ApiResponse) {
        when (response.status) {
            Status.LOADING -> {
                isLoading = true
                if (type == 1)
                    showProgress("")
            }
            Status.SUCCESS -> {
                isLoading = false
                if (type == 1)
                    hideProgress()

                renderResponse(type, response)
            }
            Status.ERROR -> {
                isLoading = false
                if (type == 1)
                    hideProgress()
                Log.e("home_error", Gson().toJson(response.error))
                //showCustomToastMessage(this, "Something went wrong!")
            }
        }
    }

    private fun renderResponse(type: Int, response: ApiResponse) {
        Log.e("home_response: ", Gson().toJson(response))
        if (type == 1) {
//            val data: String = Utils.toJson(response.data)
//            val gson= Gson();
            try {
                if (response.data == null)
                    return
                val homeResponse = /*gson.fromJson(data, HomeResponse::class.java)*/
                    HomeParser.parseResponse(response.data)
                if (homeResponse!!.success) {
                    if (homeResponse.data.size > 0) {

                        val pagesFromApi = homeResponse.total / 5.0
                        totalPages = ceil(pagesFromApi).toInt()
                        Log.e("totalPages", " $totalPages")
                        if (pageNo == 1) {
                            listPosts.clear()
                            listPosts.addAll(homeResponse.data)
                            Log.e("call", "list size: " + listPosts.size)
                            postPlayAdapter = PostPlayAdapter(this, listPosts, instanceObj, this)
                            rvHome.adapter = postPlayAdapter
                            rvHome.layoutManager =
                                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                        } else {
                            listPosts.addAll(homeResponse.data)
                        }

                        val snapHelper = PagerSnapHelper()
                        snapHelper.attachToRecyclerView(rvHome)

                        if (VrockkApplication.user_obj != null) {
                            if (listPosts[position].userId._id ?: "" != VrockkApplication.user_obj?._id ?: "")
                                addViewToPostViewModel.addViewToPost(
                                    "SEC " + VrockkApplication.user_obj!!.authToken,
                                    homeResponse.data.get(0)._id
                                )
                        }

                        rvHome.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                            override fun onScrollStateChanged(
                                recyclerView: RecyclerView,
                                newState: Int
                            ) {
                                super.onScrollStateChanged(recyclerView, newState)
                                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                                    try {
                                        val centerView =
                                            snapHelper.findSnapView(rvHome.layoutManager)
                                        val pos = rvHome.layoutManager!!.getPosition(centerView!!)
                                        player!!.playWhenReady = false
                                        index = pos
                                        rvHome.adapter!!.notifyDataSetChanged()

                                        if (VrockkApplication.user_obj != null) {
                                            if (listPosts[position].userId._id ?: "" != VrockkApplication.user_obj?._id ?: "")
                                                addViewToPostViewModel.addViewToPost(
                                                    "SEC " + VrockkApplication.user_obj!!.authToken,
                                                    homeResponse.data.get(index)._id
                                                )
                                        }
                                    } catch (exp: Exception) {

                                    }
                                }
                            }

                            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                                super.onScrolled(recyclerView, dx, dy)
                                val layoutManager =
                                    LinearLayoutManager::class.java.cast(recyclerView.layoutManager)
                                val totalItemCount = layoutManager!!.itemCount
                                val lastVisible = layoutManager.findLastVisibleItemPosition()
                                val endHasBeenReached = lastVisible + 2 >= totalItemCount
                                if (totalItemCount > 0 && endHasBeenReached) {
                                    if (pageNo < totalPages && !isLoading) {
                                        pageNo++
                                        isLoading = true
                                        if (hashTag.isNotEmpty())
                                            commonViewModel.getHashTags(
                                                pageNo,
                                                5,
                                                hashTag,
                                                postId,
                                                VrockkApplication.user_obj?._id ?: ""
                                            )
                                    }
                                }
                            }
                        })
                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (type == 4) {
            val data: String = Utils.toJson(response.data)
            val gson1 = Gson();
//                val userProfileResponse = gson1.fromJson(data, ProfilePageResponse::class.java)
            val userProfileResponse = ProfileParser.parseResponse(response.data)
            if (userProfileResponse != null && userProfileResponse.success) {

            }
        } else if (type == 5) {
            showSnackbar("" + resources.getString(R.string.report_send_succesfully))
        }
    }

    fun postLike(_id: String) {
        if (VrockkApplication.user_obj != null)
            likePostViewModel.likePost("SEC " + VrockkApplication.user_obj!!.authToken, _id)
        else
            showLoginPopup()
    }

    fun followUnfollow(_id: String) {
        if (VrockkApplication.user_obj != null)
            followUnfollowViewModel.followUnfollowPost(
                "SEC " + VrockkApplication.user_obj!!.authToken,
                _id
            )
        else
            showLoginPopup()
    }

//    override fun onPause() {
//        super.onPause()
//        if(player != null){
//            player!!.stop()
//            player!!.release()
//        }
//    }

    override fun onStop() {
        super.onStop()

        if (player != null) {
            player!!.stop()
            player!!.release()
            player = null
        }

        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient?.removeLocationUpdates(locationCallback)

        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    fun getLastKnownLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
        locationRequest!!.interval = 100
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

    fun showShareDialog(post: String, postId: String) {
        val dialog1 = Dialog(this)
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog1.setContentView(R.layout.dialog_share)
        dialog1.show()

        dialog1.tvDuet.setOnClickListener {

            val i = Intent(this, DuetMainActivity::class.java)
            i.putExtra("videolink", post)
            startActivity(i)

            dialog1.dismiss()
        }

        dialog1.tvShareLink.setOnClickListener {


            val link: String =
                "$BASE_URL/api/user/deeplink?url=vrockk%3A%2F%2F%26postId%3D${postId}"
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "vRockk")
            shareIntent.putExtra(Intent.EXTRA_TEXT, link)
            startActivity(Intent.createChooser(shareIntent, "Post"))

            dialog1.dismiss()
        }
    }

    fun showReportDialog(_id: String) {
        val dialog1 = Dialog(this)
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog1.setContentView(R.layout.dialog_report)
        dialog1.show()

        dialog1.tvSpam.setOnClickListener {

            showConfirmReportDialog(_id, "it's spam", dialog1)
            dialog1.dismiss()
        }

        dialog1.tvInformation.setOnClickListener {

            showConfirmReportDialog(_id, "False Information", dialog1)
            dialog1.dismiss()
        }


        dialog1.tvBullying.setOnClickListener {

            showConfirmReportDialog(_id, "Bullying and harassment", dialog1)
            dialog1.dismiss()
        }

        dialog1.tvJust.setOnClickListener {

            showConfirmReportDialog(_id, "I just don't Like", dialog1)
            dialog1.dismiss()
        }
        dialog1.tvInpropriate.setOnClickListener {

            showConfirmReportDialog(_id, "Inappropriate Content", dialog1)
            dialog1.dismiss()
        }
    }

    fun showConfirmReportDialog(
        _id: String,
        message: String,
        dialog: Dialog
    ) {
        val dialog1 = Dialog(this)
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog1.setContentView(R.layout.dialog_confirmation_report)
        dialog1.show()

        dialog1.tvTitle.setText("" + resources.getString(R.string.are_you_sure_you_want_to_report_this_post))

        dialog1.tvNo.setOnClickListener {
            dialog1.dismiss()
        }

        dialog1.tvYes.setOnClickListener {

            reportViewModel.hitReport(
                "SEC " + VrockkApplication.user_obj!!.authToken, _id, message, "Post"
            )

            dialog1.dismiss()
            dialog.dismiss()

        }

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
                        bottomSheetBehaviorForMore.peekHeight = 0
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {

                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        bottomSheet.setBackgroundColor(getColor(R.color.colorBlack))
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

    private fun setCoinAdapter() {
        coinList.clear()
    }

    private fun handleBottomSheetSlideForMore() {
        bottomSheetBehaviorForMore = BottomSheetBehavior.from<ConstraintLayout>(bottomSheetForMore)
//        bottomSheetBehavior.isHideable = true
        bottomSheetBehaviorForMore.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        // bottomSheet.setBackgroundResource(R.drawable.drawable_topround)
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {

                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        // bottomSheet.setBackgroundColor(activity!!.getColor(R.color.colorBlack))
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        //bottomSheet.setBackgroundResource(R.drawable.drawable_topround)
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {

                    }
                }
            }
        })


    }

    override fun onItemClicked(position: Int, type: String) {
        if (type == Constant.ADAPTER_CLICK) {

            showSnackbar("Send gifts to your loved ones that they can redeem, coming soon")
            // bottomSheetBehavior.peekHeight = 1200
            //bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        } else if (type == Constant.HOME_CLICK) {
            HomeFragment.otherUserProfileId = "" + listPosts[position].userId._id
            GiftsBSFragment.otherUserProfileId = listPosts[position].userId._id
            giftsBSFragment = GiftsBSFragment(this)
            giftsBSFragment.show(supportFragmentManager, giftsBSFragment.tag)
        } else if (type == "more") {

        } else if (type == "comment") {

            if (VrockkApplication.user_obj != null) {
                val i = Intent(this, CommentActivity::class.java)
                i.putExtra("_id", listPosts[position]._id)
                Log.e("position", " $position ")
                i.putExtra("position", position)
                startActivityForResult(i, 20)
            } else {
                showLoginPopup()
            }

        } else if (type == "other") {
            if (VrockkApplication.user_obj != null) {
                HomeFragment.otherUserProfileId = "" + listPosts[position].userId._id
                GiftsBSFragment.otherUserProfileId = listPosts[position].userId._id
                val i = Intent(this, OtherProfileActivity::class.java)
                i.putExtra("_id", listPosts[position].userId._id)
                i.putExtra("position", position)
                startActivityForResult(i, 30)
            } else {
                showLoginPopup()
            }
        } else if (type == "share") {
//            val link :String = "$BASE_URL/api/user/deeplink?url=vrockk%3A%2F%2F%26postId%3D${listPosts[position]._id}"
//            val shareIntent = Intent()
//            shareIntent.action = Intent.ACTION_SEND
//            shareIntent.type="text/plain"
//            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "vRockk")
//            shareIntent.putExtra(Intent.EXTRA_TEXT, link)
//            startActivity(Intent.createChooser(shareIntent,"Post"))
            settingsViewModel.shortenDeepLinkUrl(
                "$BASE_URL/api/user/deeplink?url=vrockk%3A%2F%2F%26postId%3D${listPosts[position]._id}"
            )

        } else if (type == "duet") {
            val i = Intent(this, DuetMainActivity::class.java)
            if (listPosts[position].post.contains("media.vrockk"))
                i.putExtra("videolink", listPosts[position].post)
            else
                i.putExtra("videolink", listPosts[position].post)
            startActivityForResult(i, 30)
        }
    }

    fun shareDeepLinkUrl(shortUrl: String) {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "vRockk")
        shareIntent.putExtra(Intent.EXTRA_TEXT, shortUrl)
        startActivity(Intent.createChooser(shareIntent, "Post"))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 20) {
            if (data?.hasExtra("count")!!) {
                val count = data.getIntExtra("count", 0)
                val position = data.getIntExtra("position", 0)
                Log.e("count", " $count $position")
                listPosts[position].totalComments = count
                postPlayAdapter?.notifyItemChanged(position)
            }
        } else if (resultCode == Activity.RESULT_OK && requestCode == 30) {
            if (data?.hasExtra("isFollow")!! && data.getStringExtra("isFollow") == "block") {
                hitApi()
            } else {
                postPlayAdapter?.notifyDataSetChanged()
            }
        }
    }

}