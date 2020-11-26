package com.vrockk.view.home

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.login.LoginManager
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.adapter.HomeAdapter
import com.vrockk.api.ApiResponse
import com.vrockk.api.BASE_URL
import com.vrockk.api.Event
import com.vrockk.api.Status
import com.vrockk.api.parsers.SettingsParser
import com.vrockk.base.BaseFragment
import com.vrockk.interfaces.ItemClickListernerWithType
import com.vrockk.models.home.home_page.Data
import com.vrockk.models.home.home_page.HomeResponse
import com.vrockk.player.cache.CacheUtils
import com.vrockk.services.FeedsCacheJobService
import com.vrockk.utils.ConnectionStateMonitor
import com.vrockk.utils.Constant.Companion.GIFT
import com.vrockk.utils.Constant.Companion.HOME_CLICK
import com.vrockk.utils.PreferenceHelper
import com.vrockk.utils.Utils
import com.vrockk.view.comment.CommentActivity
import com.vrockk.view.duet.DuetMainActivity
import com.vrockk.view.feeds.ForYouFeedsFragment
import com.vrockk.view.fragment.GiftsBSFragment
import com.vrockk.view.login.LoginActivity
import com.vrockk.view.profile.OtherProfileActivity
import com.vrockk.viewmodels.*
import com.vrockk.viewmodels.viewmodels.HomePageViewModel
import com.vrockk.viewmodels.viewmodels.SettingsViewModel
import kotlinx.android.synthetic.main.dialog_confirmation_report.*
import kotlinx.android.synthetic.main.dialog_report.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.my_custom_alert.*
import org.koin.android.viewmodel.ext.android.viewModel
import kotlin.math.ceil

class HomeFragment : BaseFragment(), ItemClickListernerWithType,
    SwipeRefreshLayout.OnRefreshListener
//    , ConnectionStateMonitor.ConnectivityChangedListener
{
    companion object {
        const val TAG = "HomeFragment"

        const val FEEDS_TYPE_TRENDING = "trending"
        const val FEEDS_TYPE_FOR_YOU = "foryou"
        const val FEEDS_TYPE_LIVE = "live"

        const val ITEMS_PER_PAGE_COUNT = 10
        const val RELOAD_OFFSET = 5

        var JOBS_START_ID = 2345

        var playingFeedsType = FEEDS_TYPE_TRENDING
        var playingFeedsPageNo = 1
        var feedsTotalPages = 0

        var isNextPageLoading = false
        var isHomeVisible: Boolean = true

        var exoPlayer: SimpleExoPlayer? = null

        val songId: String = ""
        var otherUserProfileId = ""
    }

    lateinit var typefaceRegular: Typeface
    lateinit var typefaceBlack: Typeface

    var currentCacheJobId: Int = JOBS_START_ID
    val jobsList: ArrayList<Int> = ArrayList()

    var playingFeedListIndex: Int = 0
    var playingFeedPlayPosition: Long = 0

    var layoutManager: LinearLayoutManager? = null
    var snapHelper: PagerSnapHelper? = null

    var homeAdapter: HomeAdapter? = null
    val feedsData = ArrayList<Data>()
    var isPaginatedFetch: Boolean = false
    var responseHasElements: Boolean = false

    private val homePageViewModel by viewModel<HomePageViewModel>()
    private val addViewToPostViewModel by viewModel<AddViewToPostViewModel>()
    private val getFollowingViewModel by viewModel<FollowingViewModel>()
    private val likePostViewModel by viewModel<LikePostViewModel>()
    private val followUnFollowViewModel by viewModel<FollowUnfollowViewModel>()
    private val reportViewModel by viewModel<ReportViewModel>()
    private val settingsViewModel by viewModel<SettingsViewModel>()

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var bottomSheetBehaviorForMore: BottomSheetBehavior<ConstraintLayout>
    private lateinit var giftsBSFragment: GiftsBSFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isLoggedIn())
            homePageViewModel.preFetchHomeFeeds(
                playingFeedsPageNo, ITEMS_PER_PAGE_COUNT, VrockkApplication.user_obj!!._id,
                getMyLatitude(), getMyLongitude(), songId, ""
            )
        else
            homePageViewModel.preFetchHomeFeeds(
                playingFeedsPageNo, ITEMS_PER_PAGE_COUNT, "",
                getMyLatitude(), getMyLongitude(), songId, ""
            )

        typefaceRegular = Typeface.createFromAsset(activity!!.assets, "roboto_regular.ttf")
        typefaceBlack = Typeface.createFromAsset(activity!!.assets, "roboto_black.ttf")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initProgress(contentLoadingProgressBar)

        if (isLoggedIn())
            clHeadertop.visibility = View.VISIBLE

        createExoplayer()
        giftsBSFragment = GiftsBSFragment(activity!!)

        handleCoinsBottomSheet()
        handleMoreBottomSheet()

        snapHelper = PagerSnapHelper()
        rvFeeds.onFlingListener = null
        snapHelper!!.attachToRecyclerView(rvFeeds)

        layoutManager = LinearLayoutManager(activity!!, LinearLayoutManager.VERTICAL, false)
        homeAdapter = HomeAdapter(context!!, feedsData, this, this)

        rvFeeds.layoutManager = layoutManager
        rvFeeds.adapter = homeAdapter

        addScrollListener()
        addListeners()
        addObservers()

    }

    private fun addScrollListener() {
        rvFeeds.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    paginate()
                    onPlayingFeedScrolled()
                }
            }
        })
    }

    private fun purposeNotKnown() {
        if (playingFeedsType == FEEDS_TYPE_FOR_YOU) {
            try {
//                if (totalFeedsInList == feedsData.size) {
                if (VrockkApplication.isLoggedIn()) {
                    playingFeedsType = FEEDS_TYPE_FOR_YOU
                    playingFeedListIndex = 0
                    playingFeedPlayPosition = 0
                    playingFeedsPageNo = 1

                    tvForYou.typeface = typefaceBlack
                    tvTranding.typeface = typefaceRegular

                    stopFeed()
                    releaseExoplayer()
                    resetFeedsList()

                    getFollowingViewModel.getFollowingFeeds(
                        "SEC " + VrockkApplication.user_obj!!.authToken,
                        1, ITEMS_PER_PAGE_COUNT, songId
                    )
                } else
                    showLoginPopup()
//                }
//                totalFeedsInList = 0
            } catch (e: Exception) {

            }
        }
    }

    private fun onPlayingFeedScrolled() {
        try {
            val currentFeedView = snapHelper?.findSnapView(rvFeeds.layoutManager)
            val currentFeedViewPosition = rvFeeds.layoutManager!!.getPosition(currentFeedView!!)

            if (currentFeedViewPosition != playingFeedListIndex) {
                playingFeedListIndex = currentFeedViewPosition
                playingFeedPlayPosition = exoPlayer!!.currentPosition

                stopFeed()
//                releaseExoplayer()

                if (VrockkApplication.isLoggedIn()) {
                    if (feedsData[playingFeedListIndex].userId._id != VrockkApplication.user_obj?._id ?: "")
                        addViewToPostViewModel.addViewToPost(
                            "SEC " + VrockkApplication.user_obj!!.authToken,
                            feedsData[playingFeedListIndex]._id
                        )
                }
            } else
                playingFeedPlayPosition = exoPlayer!!.currentPosition
            homeAdapter?.notifyDataSetChanged()
        } catch (exp: Exception) {

        }
    }

    private fun paginate() {
        val totalItemCount = layoutManager!!.itemCount
        val lastVisible = layoutManager!!.findLastVisibleItemPosition()
        val endHasBeenReached = lastVisible + RELOAD_OFFSET >= totalItemCount

        if (totalItemCount > 0 && endHasBeenReached) {
            if (!isNextPageLoading) {
                playingFeedsPageNo++
                if (playingFeedsType == FEEDS_TYPE_TRENDING) {
                    isNextPageLoading = true
                    isPaginatedFetch = true
                    if (VrockkApplication.isLoggedIn()) {
                        homePageViewModel.getV2HomeFeeds(
                            playingFeedsPageNo,
                            ITEMS_PER_PAGE_COUNT,
                            VrockkApplication.user_obj!!._id,
                            getMyLatitude(),
                            getMyLongitude(),
                            songId,
                            ""
                        )
                    } else
                        homePageViewModel.getV2HomeFeeds(
                            playingFeedsPageNo, ITEMS_PER_PAGE_COUNT,
                            "", getMyLatitude(), getMyLongitude(), songId, ""
                        )
                } else if (playingFeedsType == FEEDS_TYPE_FOR_YOU) {
                    isNextPageLoading = true
                    isPaginatedFetch = true
                    if (VrockkApplication.isLoggedIn()) {
                        getFollowingViewModel.getFollowingFeeds(
                            "SEC " + VrockkApplication.user_obj!!.authToken,
                            playingFeedsPageNo, ITEMS_PER_PAGE_COUNT, songId
                        )
                    }
                }
            }
        }
    }

    private fun addListeners() {
        srlFeeds.setOnRefreshListener(this)

        tvLive.setOnClickListener {
            showSnackbar("Go live as soon as you have 5,000 followers")
        }

        tvTranding.setOnClickListener {
            if (playingFeedsType == FEEDS_TYPE_TRENDING)
                return@setOnClickListener

            tvForYou.typeface = typefaceRegular
            tvTranding.typeface = typefaceBlack

            playingFeedsType = FEEDS_TYPE_TRENDING
            playingFeedListIndex = 0
            playingFeedPlayPosition = 0
            playingFeedsPageNo = 1

            Handler().postDelayed({
                stopFeed()
                releaseExoplayer()
                resetFeedsList()
                showProgress("")

                isPaginatedFetch = false
                if (VrockkApplication.isLoggedIn())
                    homePageViewModel.getV2HomeFeeds(
                        playingFeedsPageNo, ITEMS_PER_PAGE_COUNT, VrockkApplication.user_obj!!._id,
                        getMyLatitude(), getMyLongitude(), songId, ""
                    )
                else
                    homePageViewModel.getV2HomeFeeds(
                        playingFeedsPageNo, ITEMS_PER_PAGE_COUNT, "",
                        getMyLatitude(), getMyLongitude(), songId, ""
                    )
            }, 150)
        }

        tvForYou.setOnClickListener {
            if (VrockkApplication.isLoggedIn()) {
                if (playingFeedsType == FEEDS_TYPE_FOR_YOU)
                    return@setOnClickListener

                tvTranding.typeface = typefaceRegular
                tvForYou.typeface = typefaceBlack

                playingFeedsType = FEEDS_TYPE_FOR_YOU
                playingFeedListIndex = 0
                playingFeedPlayPosition = 0
                playingFeedsPageNo = 1

                stopFeed()
                releaseExoplayer()
                resetFeedsList()
                showProgress("")

                isPaginatedFetch = false
                getFollowingViewModel.getFollowingFeeds(
                    "SEC " + VrockkApplication.user_obj!!.authToken,
                    1, ITEMS_PER_PAGE_COUNT, songId
                )
            } else
                showLoginPopup()
        }
    }

    private fun addObservers() {
        homePageViewModel.getPreFetchedResponseLiveData()
            .observe(activity!!, Observer<Event<HomeResponse>> { response ->
                handleFeedsApiResponse(response.peekContent())
            })

        homePageViewModel.getResponseLiveData()
            .observe(activity!!, Observer<Event<HomeResponse>> { response ->
                response.getContentIfNotHandled()?.let {
                    handleFeedsApiResponse(it)
                }
            })

        homePageViewModel.errorLiveData.observe(activity!!, Observer { it ->
            it.getContentIfNotHandled()?.let {
                hideProgress()
                showProgress("" + it)
            }
        })

        getFollowingViewModel.errorMessage.observe(activity!!, Observer { it ->
            it.getContentIfNotHandled()?.let {
                hideProgress()
            }
        })

        getFollowingViewModel.getFollowingResponse()
            .observe(activity!!, Observer<Event<HomeResponse>> { response ->
                response.getContentIfNotHandled()?.let {
                    handleFeedsApiResponse(it)
                }
            })

        likePostViewModel.likePostResponse()
            .observe(viewLifecycleOwner, Observer<ApiResponse> { response ->
                this.handleOtherApisResponse(2, response)
            })

        addViewToPostViewModel.uploadSongResponse()
            .observe(viewLifecycleOwner, Observer<ApiResponse> { response ->
                this.handleOtherApisResponse(6, response)
            })

        reportViewModel.reportResponse()
            .observe(viewLifecycleOwner, Observer<ApiResponse> { response ->
                this.handleOtherApisResponse(7, response)
            })

        followUnFollowViewModel.followUnfollowResponse()
            .observe(viewLifecycleOwner, Observer<ApiResponse> { response ->
                this.handleOtherApisResponse(3, response)
            })

        settingsViewModel.shortenUrlResponseLiveData().observe(viewLifecycleOwner, Observer {
            val shortUrlData = SettingsParser.parseShortUrlResponse(it.data)
            if (shortUrlData != null) {
                shareDeepLinkUrl(shortUrlData.shortUrl)
            }
        })
    }

    override fun onRefresh() {
        playingFeedListIndex = 0
        playingFeedPlayPosition = 0
        playingFeedsPageNo = 1

        isPaginatedFetch = false
        requestFeeds(true)
    }

    private var mediaSourceFactory: MediaSourceFactory? = null
    private fun createExoplayer() {
        val trackSelector = DefaultTrackSelector(activity!!);
        trackSelector.parameters = DefaultTrackSelector.ParametersBuilder(activity!!)
            .setForceHighestSupportedBitrate(false)
            .setViewportSizeToPhysicalDisplaySize(activity!!, false)
            .build()

        val loadControl = DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, 16))
            .setBufferDurationsMs(
                1000,
                60000,
                500,
                1000
            )
            .setTargetBufferBytes(-1)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        mediaSourceFactory = DefaultMediaSourceFactory(
            CacheUtils.myInstance().cacheDataSourceFactory
        )

        exoPlayer = SimpleExoPlayer.Builder(activity!!)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
//            .setMediaSourceFactory(mediaSourceFactory!!)
            .build()

        exoPlayer!!.repeatMode = Player.REPEAT_MODE_ONE
    }

    private fun assignExoplayer(holder: HomeAdapter.ViewHolder, videoUrl: String) {
        try {
            if (exoPlayer == null)
                createExoplayer()

            holder.vv_postVideo.player = exoPlayer
            holder.vv_postVideo.setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)

            exoPlayer!!.addListener(object : Player.EventListener {
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            holder.determinateBar.visibility = View.VISIBLE
                        }
                        Player.STATE_READY -> {
                            holder.determinateBar.visibility = View.GONE
                            holder.vv_postVideo.visibility = View.VISIBLE
                        }
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun changeFeed(holder: HomeAdapter.ViewHolder, videoUrl: String) {
        try {
            assignExoplayer(holder, videoUrl)

            val mediaItem = MediaItem.Builder()
                .setUri(videoUrl)
                .setMimeType(MimeTypes.APPLICATION_MP4)
                .build()

            exoPlayer!!.setMediaSource(
                mediaSourceFactory!!.createMediaSource(mediaItem), playingFeedPlayPosition
            )
            playingFeedPlayPosition = 0

            prepareExoplayer()
        } catch (e: Exception) {

        }
    }

    private fun prepareExoplayer() {
        try {
            exoPlayer!!.playWhenReady = true
            exoPlayer!!.prepare()

        } catch (e: Exception) {

        }
    }

    fun isPlaying(): Boolean {
        try {
            return exoPlayer!!.isPlaying
        } catch (e: Exception) {

        }
        return false
    }

    fun playFeed() {
        try {
            exoPlayer!!.play()
        } catch (e: Exception) {

        }
    }

    fun pauseFeed() {
        try {
            exoPlayer!!.pause()
        } catch (e: Exception) {

        }
    }

    fun stopFeed() {
        try {
            exoPlayer!!.stop()
        } catch (e: Exception) {

        }
    }

    fun releaseExoplayer() {
        try {
            exoPlayer!!.stop(true)
            exoPlayer!!.release()
            exoPlayer = null
        } catch (e: Exception) {

        }
    }

    private fun resetFeedsList() {
        feedsData.clear()
        homeAdapter?.notifyDataSetChanged()
    }

    private fun requestFeeds(refresh: Boolean) {
        if (playingFeedsType == FEEDS_TYPE_TRENDING) {
            if (!refresh) {
                stopFeed()
                releaseExoplayer()
                resetFeedsList()
                showProgress("")
            }

            Handler().postDelayed({
                if (isLoggedIn())
                    homePageViewModel.getV2HomeFeeds(
                        playingFeedsPageNo, ITEMS_PER_PAGE_COUNT, VrockkApplication.user_obj!!._id,
                        getMyLatitude(), getMyLongitude(), songId, ""
                    )
                else
                    homePageViewModel.getV2HomeFeeds(
                        playingFeedsPageNo, ITEMS_PER_PAGE_COUNT, "",
                        getMyLatitude(), getMyLongitude(), songId, ""
                    )
            }, 600)
        } else {
            if (isLoggedIn()) {
                if (!refresh) {
//                    stopFeed()
//                    releaseExoplayer()
//                    resetFeedsList()
                    showProgress("")
                }

                playingFeedsType = FEEDS_TYPE_FOR_YOU

                tvTranding.typeface = typefaceRegular
                tvForYou.typeface = typefaceBlack

                getFollowingViewModel.getFollowingFeeds(
                    "SEC " + VrockkApplication.user_obj!!.authToken,
                    1, ITEMS_PER_PAGE_COUNT, songId
                )
            } else
                showLoginPopup()
        }
    }

    private fun handleFeedsApiResponse(feedsResponse: HomeResponse) {
        if (homeAdapter == null)
            return

        if (feedsResponse.success) {
//            if (isLoggedIn() && (homeResponse.user.isDeleted || homeResponse.user.status == 0))
//                showDisablePopup()

            responseHasElements = feedsResponse.data.isNotEmpty()
            val pagesFromApi = feedsResponse.total / ITEMS_PER_PAGE_COUNT
            feedsTotalPages = ceil(pagesFromApi.toDouble()).toInt()

            if (feedsResponse.data.size > 0) {
                // caching of streams
                val videoUrls = arrayListOf<String>()
                for (i in 0 until feedsResponse.data.size) {
                    val videoUrl = feedsResponse.data[i].post
                    if (videoUrl.isNotEmpty())
                        videoUrls.add(videoUrl)
                }
                cacheFeedStreams(videoUrls)

                onFeedsDataFetched(true)
                if (srlFeeds.isRefreshing) {
                    stopFeed()
                    releaseExoplayer()
                    resetFeedsList()

                } else if (playingFeedsPageNo == 1)
                    feedsData.clear()
                feedsData.addAll(feedsResponse.data)

            } else
                onFeedsDataFetched(false)
        } else
            onFeedsDataFetched(true)

//        totalFeedsInList = 0
        isNextPageLoading = false
        hideProgress()
        srlFeeds.isRefreshing = false

        if (isPaginatedFetch && exoPlayer != null)
            playingFeedPlayPosition = exoPlayer!!.currentPosition

        if (homeAdapter != null)
            homeAdapter?.notifyDataSetChanged()
    }

    private fun onFeedsDataFetched(showFeedsRecycler: Boolean) {
        if (showFeedsRecycler) {
            if (rvFeeds != null && rvFeeds.visibility == View.GONE)
                rvFeeds.visibility = View.VISIBLE
            if (ivNoPost != null && ivNoPost.visibility == View.VISIBLE)
                ivNoPost.visibility = View.GONE
            if (tvNoPost != null && tvNoPost.visibility == View.VISIBLE)
                tvNoPost.visibility = View.GONE
        } else {
            if (rvFeeds != null && rvFeeds.visibility == View.VISIBLE)
                rvFeeds.visibility = View.GONE
            if (ivNoPost != null && ivNoPost.visibility == View.GONE)
                ivNoPost.visibility = View.VISIBLE
            if (tvNoPost != null && tvNoPost.visibility == View.GONE)
                tvNoPost.visibility = View.VISIBLE
        }
    }

    private fun handleOtherApisResponse(type: Int, response: ApiResponse) {
        when (response.status) {
            Status.LOADING -> {
                if (type == 1 || type == 4) {
                    isNextPageLoading = true
                }
            }
            Status.SUCCESS -> {
                isNextPageLoading = false
                if (type == 1 || type == 4) {
                    hideProgress()
                }
                renderOtherApisResponse(type, response)
            }
            Status.ERROR -> {
                isNextPageLoading = false
                if (type == 1 || type == 4)
                    hideProgress()
            }
        }
    }

    private fun renderOtherApisResponse(type: Int, response: ApiResponse) {
        if (type == 1 || type == 4) {
            feedsData.clear()

            val data: String = Utils.toJson(response.data)
            val homeResponse = Gson().fromJson(data, HomeResponse::class.java)

            if (homeResponse.success) {
                if (homeResponse.data.size > 0) {
                    feedsData.addAll(homeResponse.data)
                    feedsTotalPages =
                        ceil(homeResponse.total / ITEMS_PER_PAGE_COUNT.toDouble()).toInt()
                }
            }

            if (isPaginatedFetch)
                playingFeedPlayPosition = exoPlayer!!.currentPosition
            homeAdapter?.notifyDataSetChanged()
        }

        if (type == 7) {
            showSnackbar("" + resources.getString(R.string.report_send_succesfully))
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        isHomeVisible = isVisibleToUser
    }

    private fun cacheFeedStreams(videoUrls: ArrayList<String>) {
//        if (currentCacheJobId != JOBS_START_ID) {
//
//        }

        val cachingIntent = Intent(activity, FeedsCacheJobService::class.java)
        cachingIntent.putExtra(FeedsCacheJobService.EXTRA_VIDEOS, videoUrls)

//        jobsList.add(currentCacheJobId)
        FeedsCacheJobService.startCaching(
            context!!, /*currentCacheJobId*/
            JOBS_START_ID, cachingIntent
        )
//        ++currentCacheJobId
    }

    fun postLike(_id: String) {
        if (VrockkApplication.isLoggedIn())
            likePostViewModel.likePost("SEC " + VrockkApplication.user_obj!!.authToken, _id)
        else
            showLoginPopup()
    }

    fun followUnFollow(_id: String) {
        if (VrockkApplication.isLoggedIn())
            followUnFollowViewModel.followUnfollowPost(
                "SEC " + VrockkApplication.user_obj!!.authToken, _id
            )
        else
            showLoginPopup()
    }

    override fun onStart() {
        super.onStart()

        activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        prepareExoplayer()
    }

    override fun onResume() {
        super.onResume()

        isHomeVisible = true
        if (homeAdapter != null)
            homeAdapter?.notifyDataSetChanged()

        activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        prepareExoplayer()

//        ConnectionStateMonitor.connectivityChangedListener = this
//        ConnectionStateMonitor.checkCapabilities(context!!)
    }

    override fun onPause() {
        super.onPause()

        isHomeVisible = false

        activity!!.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        stopFeed()
        releaseExoplayer()
    }

    override fun onStop() {
        super.onStop()

        activity!!.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        stopFeed()
        releaseExoplayer()
//        ConnectionStateMonitor.connectivityChangedListener = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

    }

    private fun handleCoinsBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from<ConstraintLayout>(bottomSheetCoins)
        bottomSheetBehavior.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        bottomSheet.setBackgroundResource(R.drawable.drawable_topround)
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        bottomSheet.setBackgroundColor(activity!!.getColor(R.color.colorBlack))
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        bottomSheet.setBackgroundResource(R.drawable.drawable_topround)
                    }
                }
            }
        })
    }

    private fun handleMoreBottomSheet() {
        bottomSheetBehaviorForMore = BottomSheetBehavior.from<ConstraintLayout>(bottomSheetForMore)
        bottomSheetBehaviorForMore.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        bottomSheetBehaviorForMore.peekHeight = 0
                    }
                }
            }
        })
    }

    override fun onItemClicked(position: Int, type: String) {
        if (type == GIFT) {

        } else if (type == HOME_CLICK) {
            otherUserProfileId = "" + feedsData[position].userId._id
            GiftsBSFragment.otherUserProfileId = otherUserProfileId
            giftsBSFragment = GiftsBSFragment(activity!!)
            giftsBSFragment.show(activity!!.supportFragmentManager, giftsBSFragment.tag)

        } else if (type == "more") {

        } else if (type == "comment") {
            if (VrockkApplication.isLoggedIn()) {
                pauseFeed()
                val commentIntent = Intent(context, CommentActivity::class.java)
                commentIntent.putExtra("_id", feedsData[position]._id)
                commentIntent.putExtra("position", position)
                startActivityForResult(commentIntent, 20)
            } else
                showLoginPopup()

        } else if (type == "other") {
            if (VrockkApplication.isLoggedIn()) {
                pauseFeed()
                otherUserProfileId = "" + feedsData[position].userId._id
                GiftsBSFragment.otherUserProfileId = otherUserProfileId
                val othersProfileIntent = Intent(context, OtherProfileActivity::class.java)
                othersProfileIntent.putExtra("_id", feedsData[position].userId._id)
                othersProfileIntent.putExtra("position", position)
                startActivityForResult(othersProfileIntent, 30)
            } else
                showLoginPopup()

        } else if (type == "share") {
            pauseFeed()
            showProgress()
            settingsViewModel.shortenDeepLinkUrl(
                "$BASE_URL/api/user/deeplink?url=vrockk%3A%2F%2F%26postId%3D${feedsData[position]._id}"
            )

        } else if (type == "duet") {
            pauseFeed()
            val duetIntent = Intent(context, DuetMainActivity::class.java)
            if (feedsData[position].post.contains("media.vrockk"))
                duetIntent.putExtra("videolink", feedsData[position].post)
            else
                duetIntent.putExtra("videolink", feedsData[position].post)
            startActivityForResult(duetIntent, 30)
        }
    }

    private fun shareDeepLinkUrl(shortUrl: String) {
        hideProgress()
        playFeed()

        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "vRockk")
        shareIntent.putExtra(Intent.EXTRA_TEXT, shortUrl)
        startActivity(Intent.createChooser(shareIntent, "Post"))
    }

    fun showReportDialog(_id: String) {
        val dialog1 = Dialog(activity!!)
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog1.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
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

    private fun showConfirmReportDialog(
        _id: String,
        message: String,
        dialog: Dialog
    ) {
        val dialog1 = Dialog(activity!!)
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog1.setContentView(R.layout.dialog_confirmation_report)
        dialog1.show()

        dialog1.tvTitle.text =
            resources.getString(R.string.are_you_sure_you_want_to_report_this_post)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 20) {
            if (data?.hasExtra("count")!!) {
                val count = data.getIntExtra("count", 0)
                val position = data.getIntExtra("position", 0)
                feedsData[position].totalComments = count
                homeAdapter?.notifyItemChanged(position)
            }
        } else if (resultCode == Activity.RESULT_OK && requestCode == 30) {
            if (data?.hasExtra("isFollow")!! && data.getStringExtra("isFollow") == "block") {
                playingFeedListIndex = 0
                playingFeedPlayPosition = 0
                playingFeedsPageNo = 1

                requestFeeds(false)

            } else {
                if (data.hasExtra("followStatus")) {
                    val followstatus = data.getStringExtra("followStatus")
                    if (followstatus != "") {
                        val position = data.getIntExtra("position", 0)
                        feedsData[position].isFollowing = followstatus != "follow"
                    }
                }
                homeAdapter?.notifyDataSetChanged()
            }
        }
    }

    fun showDisablePopup() {
        val myCustomDlg = Dialog(activity!!)
        myCustomDlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        myCustomDlg.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        myCustomDlg.setContentView(R.layout.my_custom_alert)
        myCustomDlg.tvTitleCustom.text = resources.getString(R.string.alert)
        myCustomDlg.tvAlertMessage.text =
            resources.getString(R.string.your_account_is_disabled_by_admin)
        myCustomDlg.positiveBtn.text = resources.getString(R.string.ok)
        myCustomDlg.noBtn.visibility = View.GONE
        myCustomDlg.show()
        myCustomDlg.positiveBtn.setOnClickListener {
            myCustomDlg.dismiss()
            val editor = VrockkApplication.prefs.edit()

            val sharedPreference = PreferenceHelper.defaultPrefs(activity!!)
            val deviceToken = sharedPreference.getString(PreferenceHelper.Key.FCMTOKEN, "")
            editor!!.remove(PreferenceHelper.Key.REGISTEREDUSER)
            editor.putString(PreferenceHelper.Key.FCMTOKEN, deviceToken)
            editor.apply()
            VrockkApplication.user_obj = null

            activity!!.startActivity(Intent(activity!!, LoginActivity::class.java))
            activity!!.finishAffinity()

            try {
                LoginManager.getInstance().logOut();
            } catch (e: java.lang.Exception) {

            }
        }
    }

//    override fun onNetworkConnectionChanged(isConnected: Boolean) {
//        if (isConnected) {
//            internetInfoTv.alpha = 0f
//        } else {
//            internetInfoTv.alpha = 1f
//        }
//    }
}

