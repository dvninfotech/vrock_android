package com.vrockk.view.feeds

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.api.ApiResponse
import com.vrockk.api.BASE_URL
import com.vrockk.api.Event
import com.vrockk.api.Status
import com.vrockk.api.parsers.SettingsParser
import com.vrockk.base.BaseFragment
import com.vrockk.interfaces.ItemClickListernerWithType
import com.vrockk.models.home.home_page.Data
import com.vrockk.models.home.home_page.HomeResponse
import com.vrockk.player.cache.FeedsCachingThreader
import com.vrockk.player.media.MySinglePlayer
import com.vrockk.utils.Constant.Companion.GIFT
import com.vrockk.utils.Constant.Companion.HOME_CLICK
import com.vrockk.utils.PreferenceHelper
import com.vrockk.utils.Utils
import com.vrockk.view.comment.CommentActivity
import com.vrockk.view.dashboard.PostsFragment
import com.vrockk.view.duet.DuetMainActivity
import com.vrockk.view.fragment.GiftsBSFragment
import com.vrockk.view.login.LoginActivity
import com.vrockk.view.profile.OtherProfileActivity
import com.vrockk.viewmodels.AddViewToPostViewModel
import com.vrockk.viewmodels.FollowUnfollowViewModel
import com.vrockk.viewmodels.LikePostViewModel
import com.vrockk.viewmodels.ReportViewModel
import com.vrockk.viewmodels.viewmodels.HomePageViewModel
import com.vrockk.viewmodels.viewmodels.SettingsViewModel
import kotlinx.android.synthetic.main.dialog_confirmation_report.*
import kotlinx.android.synthetic.main.dialog_report.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.my_custom_alert.*
import org.koin.android.viewmodel.ext.android.viewModel
import kotlin.math.ceil

class TrendingFeedsFragment : BaseFragment(), ItemClickListernerWithType,
    SwipeRefreshLayout.OnRefreshListener {
    companion object {
        const val TAG = "TrendingFeedsFragment"
        const val PLAYER_TAG = "feeds_player"

        var JOBS_START_ID = 2345

        var playingFeedsPageNo = 1
        var feedsTotalPages = 0

        var isNextPageLoading = false
        var isTrendingVisible: Boolean = true

        val songId: String = ""
        var otherUserProfileId = ""

        var isFragmentVisible = false
    }

//    var myPlayer: MyMultiPlayer? = null
    var myPlayer: MySinglePlayer? = null

    var playingFeedListIndex: Int = 0
    var playingFeedPlayPosition: Long = 0

    var layoutManager: LinearLayoutManager? = null
    var snapHelper: PagerSnapHelper? = null

    var trendingFeedsAdapter: TrendingFeedsAdapter? = null
    private val feedsData = ArrayList<Data>()
    var isPaginatedFetch: Boolean = false
    var responseHasElements: Boolean = false
    private var isShareRequested = false

    private val homePageViewModel by viewModel<HomePageViewModel>()
    private val addViewToPostViewModel by viewModel<AddViewToPostViewModel>()

    private val likePostViewModel by viewModel<LikePostViewModel>()
    private val followUnFollowViewModel by viewModel<FollowUnfollowViewModel>()
    private val reportViewModel by viewModel<ReportViewModel>()
    private val settingsViewModel by viewModel<SettingsViewModel>()

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var bottomSheetBehaviorForMore: BottomSheetBehavior<ConstraintLayout>
    private lateinit var giftsBSFragment: GiftsBSFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        myPlayer = MyMultiPlayer.createInstance(PLAYER_TAG, context!!)
        myPlayer = MySinglePlayer.createInstance(PLAYER_TAG, context!!)

        homePageViewModel.preFetchHomeFeeds(
            playingFeedsPageNo, PostsFragment.ITEMS_PER_PAGE_COUNT,
            getMyUserId(), getMyLatitude(), getMyLongitude(),
            songId, ""
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_feeds, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initProgress(contentLoadingProgressBar)

        giftsBSFragment = GiftsBSFragment(activity!!)

        handleCoinsBottomSheet()
        handleMoreBottomSheet()

        snapHelper = PagerSnapHelper()
        rvFeeds.onFlingListener = null
        snapHelper!!.attachToRecyclerView(rvFeeds)

        layoutManager = LinearLayoutManager(activity!!, LinearLayoutManager.VERTICAL, false)
        trendingFeedsAdapter = TrendingFeedsAdapter(context!!, feedsData, this, this)

        rvFeeds.layoutManager = layoutManager
        rvFeeds.adapter = trendingFeedsAdapter

        addScrollListener()
        addListeners()
        addObservers()

        requestFeeds(false)
    }

    private fun addScrollListener() {
        rvFeeds.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    myPlayer?.pause()

                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    onFeedsChanged()
//                    if (PostsFragment.isPostDisplaying)
//                        myPlayer?.resumeCurrentPlayer()
                    if (PostsFragment.isPostDisplaying)
                        myPlayer?.resume()
                    checkPagination()
                }
            }
        })
    }

    private fun onFeedsChanged() {
        try {
            val currentFeedView = snapHelper?.findSnapView(rvFeeds.layoutManager)
            val currentFeedViewPosition = rvFeeds.layoutManager!!.getPosition(currentFeedView!!)

            playingFeedListIndex = currentFeedViewPosition
            myPlayer?.changeItem(currentFeedViewPosition)

            trendingFeedsAdapter?.notifyDataSetChanged()
        } catch (exp: Exception) {

        }
    }

    private fun checkPagination() {
        val totalItemCount = layoutManager!!.itemCount
        val lastVisible = layoutManager!!.findLastVisibleItemPosition()
        val endHasBeenReached = lastVisible + PostsFragment.RELOAD_OFFSET >= totalItemCount

        if (totalItemCount > 0 && endHasBeenReached) {
            if (!isNextPageLoading) {
                playingFeedsPageNo++

                isNextPageLoading = true
                isPaginatedFetch = true
                homePageViewModel.getV2HomeFeeds(
                    playingFeedsPageNo, PostsFragment.ITEMS_PER_PAGE_COUNT,
                    getMyUserId(), getMyLatitude(), getMyLongitude(),
                    songId, ""
                )
            }
        }
    }

    private fun addListeners() {
        srlFeeds.setOnRefreshListener(this)
    }

    private fun addObservers() {
//        homePageViewModel.getPreFetchedResponseLiveData()
//            .observe(activity!!, Observer<Event<HomeResponse>> { response ->
//                handleFeedsApiResponse(response.peekContent())
//            })

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

    override fun onTabSwitched() {
        if (feedsData.isEmpty()) {
            homePageViewModel.getV2HomeFeeds(
                playingFeedsPageNo, PostsFragment.ITEMS_PER_PAGE_COUNT, getMyUserId(),
                getMyLatitude(), getMyLongitude(), songId, ""
            )
        }
    }

    override fun onRefresh() {
        playingFeedListIndex = 0
        playingFeedPlayPosition = 0
        playingFeedsPageNo = 1

        isPaginatedFetch = false
        requestFeeds(true)
    }

    private fun resetFeedsList() {
        myPlayer?.reset()
        feedsData.clear()
        trendingFeedsAdapter?.notifyDataSetChanged()
    }

    private fun requestFeeds(fromRefresh: Boolean) {
        if (!fromRefresh) {
            resetFeedsList()
            showProgress()
        }

        Handler().postDelayed({
            homePageViewModel.getV2HomeFeeds(
                playingFeedsPageNo,
                PostsFragment.ITEMS_PER_PAGE_COUNT,
                getMyUserId(),
                getMyLatitude(),
                getMyLongitude(),
                songId,
                ""
            )
        }, 600)
    }

    private fun handleFeedsApiResponse(feedsResponse: HomeResponse) {
        if (trendingFeedsAdapter == null)
            return

        if (feedsResponse.success) {

            responseHasElements = feedsResponse.data.isNotEmpty()
            val pagesFromApi = feedsResponse.total / PostsFragment.ITEMS_PER_PAGE_COUNT
            feedsTotalPages = ceil(pagesFromApi.toDouble()).toInt()

            if (feedsResponse.data.size > 0) {

                // caching of streams
                val videoUrls = arrayListOf<String>()
                for (i in 0 until feedsResponse.data.size) {
                    val videoUrl = feedsResponse.data[i].post
                    if (videoUrl.isNotEmpty())
                        videoUrls.add(videoUrl)
                }

                onFeedsDataFetched(true)
                if (srlFeeds.isRefreshing) {
                    resetFeedsList()

                } else if (playingFeedsPageNo == 1) {
                    myPlayer?.reset()
                    feedsData.clear()
                }
                myPlayer?.appendToUrls(videoUrls)
                feedsData.addAll(feedsResponse.data)

//                cacheFeedStreams(videoUrls)

            } else
                onFeedsDataFetched(false)
        } else
            onFeedsDataFetched(true)

        isNextPageLoading = false
        hideProgress()
        srlFeeds.isRefreshing = false

        if (trendingFeedsAdapter != null)
            trendingFeedsAdapter?.notifyDataSetChanged()

        if (!isPaginatedFetch) {
            myPlayer?.changeItem(0, PostsFragment.isPostDisplaying)
        }
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
            myPlayer?.reset()
            feedsData.clear()

            val data: String = Utils.toJson(response.data)
            val homeResponse = Gson().fromJson(data, HomeResponse::class.java)

            if (homeResponse.success) {
                if (homeResponse.data.size > 0) {

                    // caching of streams
                    val videoUrls = arrayListOf<String>()
                    for (i in 0 until homeResponse.data.size) {
                        val videoUrl = homeResponse.data[i].post
                        if (videoUrl.isNotEmpty())
                            videoUrls.add(videoUrl)
                    }

                    myPlayer?.appendToUrls(videoUrls)
                    feedsData.addAll(homeResponse.data)

                    feedsTotalPages =
                        ceil(homeResponse.total / PostsFragment.ITEMS_PER_PAGE_COUNT.toDouble()).toInt()

//                    cacheFeedStreams(videoUrls)
                }
            }

            trendingFeedsAdapter?.notifyDataSetChanged()

            if (!isPaginatedFetch)
                myPlayer?.changeItem(0)
        }

        if (type == 7) {
            showSnackbar("" + resources.getString(R.string.report_send_succesfully))
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        isTrendingVisible = isVisibleToUser
    }

    private fun cacheFeedStreams(videoUrls: ArrayList<String>) {
//        val cachingIntent = Intent(activity, FeedsCacheJobService::class.java)
//        cachingIntent.putExtra(FeedsCacheJobService.EXTRA_VIDEOS, videoUrls)
//
//        FeedsCacheJobService.startCaching(
//            context!!, /*currentCacheJobId*/
//            JOBS_START_ID, cachingIntent
//        )

        FeedsCachingThreader(videoUrls).startCaching()
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

    fun playOrLoad() {
        if (feedsData.size == 0)
            onTabSwitched()
        else
            playFeed()
    }

    fun playFeed() {
        myPlayer?.resume()
    }

    fun pauseFeed() {
        myPlayer?.pause()
    }

    fun releasePlayers() {
        myPlayer?.resetAndRelease()
    }
    
    fun preparePlayers() {
        val videoUrls = arrayListOf<String>()
        for (i in 0 until feedsData.size) {
            val videoUrl = feedsData[i].post
            if (videoUrl.isNotEmpty())
                videoUrls.add(videoUrl)
        }
        myPlayer?.reset()
        myPlayer?.appendToUrls(videoUrls)

        if (videoUrls.size > 0) {
            myPlayer?.changeItem(playingFeedListIndex)
        }
    }

    override fun onStart() {
        super.onStart()

        activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (isFragmentVisible) {
            myPlayer?.resume()
        }
    }

    override fun onResume() {
        super.onResume()

        isTrendingVisible = true
        if (trendingFeedsAdapter != null)
            trendingFeedsAdapter?.notifyDataSetChanged()

        activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (isFragmentVisible) {
            myPlayer?.resume()
        }
    }

    override fun onPause() {
        super.onPause()

        isTrendingVisible = false

        activity!!.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        myPlayer?.pause()
    }

    override fun onStop() {
        super.onStop()

        activity!!.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        myPlayer?.pause()
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

                val commentIntent = Intent(context, CommentActivity::class.java)
                commentIntent.putExtra("_id", feedsData[position]._id)
                commentIntent.putExtra("position", position)
                startActivityForResult(commentIntent, 20)
            } else
                showLoginPopup()

        } else if (type == "other") {
            if (VrockkApplication.isLoggedIn()) {

                otherUserProfileId = "" + feedsData[position].userId._id
                GiftsBSFragment.otherUserProfileId = otherUserProfileId
                val othersProfileIntent = Intent(context, OtherProfileActivity::class.java)
                othersProfileIntent.putExtra("_id", feedsData[position].userId._id)
                othersProfileIntent.putExtra("position", position)
                startActivityForResult(othersProfileIntent, 30)
            } else
                showLoginPopup()

        } else if (type == "share") {
            if (isShareRequested)
                return
            isShareRequested = true
            showProgress()
            settingsViewModel.shortenDeepLinkUrl(
                "$BASE_URL/api/user/deeplink?url=vrockk%3A%2F%2F%26postId%3D${feedsData[position]._id}"
            )

        } else if (type == "duet") {

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

        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "vRockk")
        shareIntent.putExtra(Intent.EXTRA_TEXT, shortUrl)
        startActivity(Intent.createChooser(shareIntent, "Post"))

        isShareRequested = false
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
                trendingFeedsAdapter?.notifyItemChanged(position)
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
                trendingFeedsAdapter?.notifyDataSetChanged()
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
}

