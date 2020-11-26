package com.vrockk.view.feeds.trial1

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.vrockk.base.BaseFragment

class FeedsFragment : BaseFragment(), /*ItemClickListernerWithType,*/
    SwipeRefreshLayout.OnRefreshListener {

    override fun onRefresh() {
        TODO("Not yet implemented")
    }
//    companion object {
//        const val TAG = "FeedsFragment"
//
//        const val FEEDS_TYPE_TRENDING = "trending"
//        const val FEEDS_TYPE_FOR_YOU = "foryou"
//        const val FEEDS_TYPE_LIVE = "live"
//
//        const val ITEMS_PER_PAGE_COUNT = 10
//        const val RELOAD_OFFSET = 5
//
//        var JOBS_START_ID = 2345
//
//        var playingFeedsType = FEEDS_TYPE_TRENDING
//        var playingFeedsPageNo = 1
//        var feedsTotalPages = 0
//
//        var isNextPageLoading = false
//        var isHomeVisible: Boolean = true
//
////        var exoPlayer: SimpleExoPlayer? = null
//
//        val songId: String = ""
//        var otherUserProfileId = ""
//    }
//
//    val FEEDS_DELAY_TO_HANDLER = TimeUnit.MILLISECONDS.toMillis(100)
//    val handler = Handler()
//
//    lateinit var typefaceRegular: Typeface
//    lateinit var typefaceBlack: Typeface
//
//    var layoutManager: LinearLayoutManager? = null
//    var snapHelper: PagerSnapHelper? = null
//
//    var feedsAdapter: FeedsAdapter? = null
//    val feedDataModels = ArrayList<FeedDataModel>()
//    var isPaginatedFetch: Boolean = false
//    var responseHasElements: Boolean = false
//
//    private val feedsViewModel by viewModel<FeedsViewModel>()
//    private val addViewToPostViewModel by viewModel<AddViewToPostViewModel>()
//
//    private val likePostViewModel by viewModel<LikePostViewModel>()
//    private val followUnFollowViewModel by viewModel<FollowUnfollowViewModel>()
//    private val reportViewModel by viewModel<ReportViewModel>()
//
//    private val settingsViewModel by viewModel<SettingsViewModel>()
//
//    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
//    private lateinit var bottomSheetBehaviorForMore: BottomSheetBehavior<ConstraintLayout>
//    private lateinit var giftsBSFragment: GiftsBSFragment
//
//    private val onFeedActionListener = object : OnFeedActionListener {
//        override fun onViewUserRequested(adapterPosition: Int, feedDataModel: FeedDataModel) {
//            toast("Not yet implemented - onViewUserRequested")
//        }
//
//        override fun onFollowChangeRequested(adapterPosition: Int, feedDataModel: FeedDataModel) {
//            toast("Not yet implemented - onFollowChangeRequested")
//        }
//
//        override fun onLikesChanged(adapterPosition: Int, feedDataModel: FeedDataModel) {
//            toast("Not yet implemented - onLikesChanged")
//        }
//
//        override fun onCommentRequested(adapterPosition: Int, feedDataModel: FeedDataModel) {
//            toast("Not yet implemented - onCommentRequested")
//        }
//
//        override fun onGiftClicked(adapterPosition: Int, feedDataModel: FeedDataModel) {
//            toast("Not yet implemented - onGiftClicked")
//        }
//
//        override fun onShareClicked(adapterPosition: Int, feedDataModel: FeedDataModel) {
//            toast("Not yet implemented - onShareClicked")
//        }
//
//        override fun onMoreClicked(adapterPosition: Int, feedDataModel: FeedDataModel) {
//            toast("Not yet implemented - onMoreClicked")
//        }
//
//        override fun onDuetClicked(adapterPosition: Int, feedDataModel: FeedDataModel) {
//            toast("Not yet implemented - onDuetClicked")
//        }
//
//        override fun onReportClicked(adapterPosition: Int, feedDataModel: FeedDataModel) {
//            toast("Not yet implemented - onReportClicked")
//        }
//    }
//
//    private val onMediaEventChangeListener = object : OnMediaEventChangeListener {
//        override fun onMediaStateChanged(adapterPosition: Int) {
//
//        }
//
//        override fun onPlayChanged(adapterPosition: Int, playRequested: Boolean) {
//            PlayerUtils.shouldPlay = playRequested
//            feedsAdapter?.notifyItemChanged(adapterPosition)
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        if (isLoggedIn())
//            feedsViewModel.preFetchFeeds(
//                playingFeedsPageNo, ITEMS_PER_PAGE_COUNT, VrockkApplication.user_obj!!._id,
//                getMyLatitude(), getMyLongitude(), songId, ""
//            )
//        else
//            feedsViewModel.preFetchFeeds(
//                playingFeedsPageNo, ITEMS_PER_PAGE_COUNT, "",
//                getMyLatitude(), getMyLongitude(), songId, ""
//            )
//
//        typefaceRegular = Typeface.createFromAsset(activity!!.assets, "roboto_regular.ttf")
//        typefaceBlack = Typeface.createFromAsset(activity!!.assets, "roboto_black.ttf")
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_home, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        initProgress(contentLoadingProgressBar)
//
//        if (isLoggedIn())
//            clHeadertop.visibility = View.VISIBLE
//
//        giftsBSFragment = GiftsBSFragment(activity!!)
//
//        handleCoinsBottomSheet()
//        handleMoreBottomSheet()
//
//        snapHelper = PagerSnapHelper()
//        rvFeeds.onFlingListener = null
//        snapHelper!!.attachToRecyclerView(rvFeeds)
//
//        layoutManager = LinearLayoutManager(activity!!, LinearLayoutManager.VERTICAL, false)
//        feedsAdapter = FeedsAdapter(
//            context!!,
//            feedDataModels,
//            onMediaEventChangeListener,
//            onFeedActionListener
//        )
//
//        rvFeeds.layoutManager = layoutManager
//        rvFeeds.adapter = feedsAdapter
//
//        addScrollListener()
//        addListeners()
//        addObservers()
//    }
//
//    private fun addScrollListener() {
//        rvFeeds.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//
//                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    onPlayingFeedScrolled()
//                    paginate()
//                }
//            }
//        })
//    }
//
//    private fun onPlayingFeedScrolled() {
//        try {
//            val currentFeedView = snapHelper?.findSnapView(rvFeeds.layoutManager)
//            val currentFeedViewPosition = rvFeeds.layoutManager!!.getPosition(currentFeedView!!)
//
//            if (feedsAdapter?.getPlayingIndex() != currentFeedViewPosition) {
//                PlayerUtils.mediaState = PlayerUtils.MEDIA_UNPREPARED
//                feedsAdapter?.setPlayingIndex(currentFeedViewPosition)
//                feedsAdapter?.notifyItemChanged(currentFeedViewPosition)
//            }
//        } catch (exp: Exception) {
//
//        }
//    }
//
//    private fun paginate() {
//        val totalItemCount = layoutManager!!.itemCount
//        val lastVisible = layoutManager!!.findLastVisibleItemPosition()
//        val endHasBeenReached = lastVisible + RELOAD_OFFSET >= totalItemCount
//
//        if (totalItemCount > 0 && endHasBeenReached) {
//            if (!isNextPageLoading) {
//                playingFeedsPageNo++
//                if (playingFeedsType == FEEDS_TYPE_TRENDING) {
//                    isNextPageLoading = true
//                    isPaginatedFetch = true
//                    if (VrockkApplication.isLoggedIn()) {
//                        feedsViewModel.getV2Feeds(
//                            playingFeedsPageNo,
//                            ITEMS_PER_PAGE_COUNT,
//                            VrockkApplication.user_obj!!._id,
//                            getMyLatitude(),
//                            getMyLongitude(),
//                            songId,
//                            ""
//                        )
//                    } else
//                        feedsViewModel.getV2Feeds(
//                            playingFeedsPageNo, ITEMS_PER_PAGE_COUNT,
//                            "", getMyLatitude(), getMyLongitude(), songId, ""
//                        )
//                } else if (playingFeedsType == FEEDS_TYPE_FOR_YOU) {
//                    isNextPageLoading = true
//                    isPaginatedFetch = true
//                    if (VrockkApplication.isLoggedIn()) {
//                        feedsViewModel.getForMeFeeds(
//                            "SEC " + VrockkApplication.user_obj!!.authToken,
//                            playingFeedsPageNo, ITEMS_PER_PAGE_COUNT, songId
//                        )
//                    }
//                }
//            }
//        }
//    }
//
//    private fun addListeners() {
//        srlFeeds.setOnRefreshListener(this)
//
//        tvLive.setOnClickListener {
//            showSnackbar("Go live as soon as you have 5,000 followers")
//        }
//
//        tvTranding.setOnClickListener {
//            if (playingFeedsType == FEEDS_TYPE_TRENDING)
//                return@setOnClickListener
//
//            tvForYou.typeface = typefaceRegular
//            tvTranding.typeface = typefaceBlack
//
//            playingFeedsType = FEEDS_TYPE_TRENDING
//            playingFeedsPageNo = 1
//
//            Handler().postDelayed({
//                resetFeeds()
//                showProgress("")
//
//                isPaginatedFetch = false
//                if (VrockkApplication.isLoggedIn())
//                    feedsViewModel.getV2Feeds(
//                        playingFeedsPageNo, ITEMS_PER_PAGE_COUNT, VrockkApplication.user_obj!!._id,
//                        getMyLatitude(), getMyLongitude(), songId, ""
//                    )
//                else
//                    feedsViewModel.getV2Feeds(
//                        playingFeedsPageNo, ITEMS_PER_PAGE_COUNT, "",
//                        getMyLatitude(), getMyLongitude(), songId, ""
//                    )
//            }, 150)
//        }
//
//        tvForYou.setOnClickListener {
//            if (VrockkApplication.isLoggedIn()) {
//                if (playingFeedsType == FEEDS_TYPE_FOR_YOU)
//                    return@setOnClickListener
//
//                tvTranding.typeface = typefaceRegular
//                tvForYou.typeface = typefaceBlack
//
//                playingFeedsType = FEEDS_TYPE_FOR_YOU
//                playingFeedsPageNo = 1
//
//                resetFeeds()
//                showProgress("")
//
//                isPaginatedFetch = false
//                feedsViewModel.getForMeFeeds(
//                    "SEC " + VrockkApplication.user_obj!!.authToken,
//                    1, ITEMS_PER_PAGE_COUNT, songId
//                )
//            } else
//                showLoginPopup()
//        }
//    }
//
//    private fun addObservers() {
//        feedsViewModel.getPreFetchedFeedsModelLiveData()
//            .observe(activity!!, Observer<Event<FeedsModel>> { response ->
//                handleFeedsApiResponse(response.peekContent())
//            })
//
//        feedsViewModel.getFeedsModelLiveData()
//            .observe(activity!!, Observer<Event<FeedsModel>> { response ->
//                response.getContentIfNotHandled()?.let {
//                    handleFeedsApiResponse(it)
//                }
//            })
//
//        feedsViewModel.getFormeFeedsModelLiveData()
//            .observe(activity!!, Observer<Event<FeedsModel>> { response ->
//                response.getContentIfNotHandled()?.let {
//                    handleFeedsApiResponse(it)
//                }
//            })
//
//        feedsViewModel.errorLiveData.observe(activity!!, Observer { it ->
//            it.getContentIfNotHandled()?.let {
//                hideProgress()
//                showProgress("" + it)
//            }
//        })
//
//        likePostViewModel.likePostResponse()
//            .observe(viewLifecycleOwner, Observer<ApiResponse> { response ->
//                this.handleOtherApisResponse(2, response)
//            })
//
//        addViewToPostViewModel.uploadSongResponse()
//            .observe(viewLifecycleOwner, Observer<ApiResponse> { response ->
//                this.handleOtherApisResponse(6, response)
//            })
//
//        reportViewModel.reportResponse()
//            .observe(viewLifecycleOwner, Observer<ApiResponse> { response ->
//                this.handleOtherApisResponse(7, response)
//            })
//
//        followUnFollowViewModel.followUnfollowResponse()
//            .observe(viewLifecycleOwner, Observer<ApiResponse> { response ->
//                this.handleOtherApisResponse(3, response)
//            })
//
//        settingsViewModel.shortenUrlResponseLiveData().observe(viewLifecycleOwner, Observer {
//            val shortUrlData = SettingsParser.parseShortUrlResponse(it.data)
//            if (shortUrlData != null) {
//                shareDeepLinkUrl(shortUrlData.shortUrl)
//            }
//        })
//    }
//
//    override fun onRefresh() {
//        playingFeedsPageNo = 1
//
//        isPaginatedFetch = false
//        requestFeeds(true)
//    }
//
//    fun pausePlaying() {
//        feedsAdapter?.pauseCurrentFeed()
//    }
//
//    fun resumePlaying() {
//        feedsAdapter?.resumeCurrentFeed()
//    }
//
//    fun releasePlayers() {
//        feedsAdapter?.resetPlayer()
//    }
//
//    private fun resetFeeds() {
//        feedsAdapter?.resetPlayer()
//        feedDataModels.clear()
//        feedsAdapter?.notifyDataSetChanged()
//    }
//
//    private fun requestFeeds(refresh: Boolean) {
//        if (playingFeedsType == FEEDS_TYPE_TRENDING) {
//            if (!refresh) {
//                playingFeedsPageNo = 1
//                resetFeeds()
//                showProgress("")
//            }
//
//            Handler().postDelayed({
//                if (isLoggedIn())
//                    feedsViewModel.getV2Feeds(
//                        playingFeedsPageNo, ITEMS_PER_PAGE_COUNT, VrockkApplication.user_obj!!._id,
//                        getMyLatitude(), getMyLongitude(), songId, ""
//                    )
//                else
//                    feedsViewModel.getV2Feeds(
//                        playingFeedsPageNo, ITEMS_PER_PAGE_COUNT, "",
//                        getMyLatitude(), getMyLongitude(), songId, ""
//                    )
//            }, 600)
//        } else {
//            if (isLoggedIn()) {
//                if (!refresh) {
//                    playingFeedsPageNo = 1
//                    resetFeeds()
//                    showProgress("")
//                }
//
//                playingFeedsType = FEEDS_TYPE_FOR_YOU
//
//                tvTranding.typeface = typefaceRegular
//                tvForYou.typeface = typefaceBlack
//
//                feedsViewModel.getForMeFeeds(
//                    "SEC " + VrockkApplication.user_obj!!.authToken,
//                    playingFeedsPageNo, ITEMS_PER_PAGE_COUNT, songId
//                )
//            } else
//                showLoginPopup()
//        }
//    }
//
//    private fun handleFeedsApiResponse(feedsModel: FeedsModel) {
//        val currentListSize = feedDataModels.size
//
//        if (feedsModel.success) {
//            responseHasElements = feedsModel.dataModels.isNotEmpty()
//            val pagesFromApi = feedsModel.total / ITEMS_PER_PAGE_COUNT
//            feedsTotalPages = ceil(pagesFromApi.toDouble()).toInt()
//
//            if (feedsModel.dataModels.size > 0) {
//                // caching of streams
////                val videoUrls = arrayListOf<String>()
////                for (i in 0 until feedsResponse.data.size) {
////                    val videoUrl = feedsResponse.data[i].post
////                    if (videoUrl.isNotEmpty())
////                        videoUrls.add(videoUrl)
////                }
////                cacheFeedStreams(videoUrls)
//
//                onFeedsDataFetched(true)
//                if (srlFeeds.isRefreshing) {
//                    playingFeedsPageNo == 1
//                    resetFeeds()
//                }
//                feedDataModels.addAll(feedsModel.dataModels)
//
//            } else
//                onFeedsDataFetched(false)
//        } else
//            onFeedsDataFetched(true)
//
//        isNextPageLoading = false
//        hideProgress()
//        srlFeeds.isRefreshing = false
//
//        feedsAdapter?.notifyItemRangeInserted(currentListSize, feedsModel.dataModels.size)
//    }
//
//    private fun onFeedsDataFetched(showFeedsRecycler: Boolean) {
//        if (showFeedsRecycler) {
//            if (rvFeeds != null && rvFeeds.visibility == View.GONE)
//                rvFeeds.visibility = View.VISIBLE
//            if (ivNoPost != null && ivNoPost.visibility == View.VISIBLE)
//                ivNoPost.visibility = View.GONE
//            if (tvNoPost != null && tvNoPost.visibility == View.VISIBLE)
//                tvNoPost.visibility = View.GONE
//        } else {
//            if (rvFeeds != null && rvFeeds.visibility == View.VISIBLE)
//                rvFeeds.visibility = View.GONE
//            if (ivNoPost != null && ivNoPost.visibility == View.GONE)
//                ivNoPost.visibility = View.VISIBLE
//            if (tvNoPost != null && tvNoPost.visibility == View.GONE)
//                tvNoPost.visibility = View.VISIBLE
//        }
//    }
//
//    private fun handleOtherApisResponse(type: Int, response: ApiResponse) {
//        when (response.status) {
//            Status.LOADING -> {
//                if (type == 1 || type == 4) {
//                    isNextPageLoading = true
//                }
//            }
//            Status.SUCCESS -> {
//                isNextPageLoading = false
//                if (type == 1 || type == 4) {
//                    hideProgress()
//                }
//                renderOtherApisResponse(type, response)
//            }
//            Status.ERROR -> {
//                isNextPageLoading = false
//                if (type == 1 || type == 4)
//                    hideProgress()
//            }
//        }
//    }
//
//    private fun renderOtherApisResponse(type: Int, response: ApiResponse) {
//        if (type == 1 || type == 4) {
//            resetFeeds()
//
//            val feedsModel = FeedsParser.parseFeeds(response.data!!)
//
//            if (feedsModel!!.success) {
//                if (feedsModel.dataModels.size > 0) {
//                    feedDataModels.addAll(feedsModel.dataModels)
//                    feedsTotalPages = ceil(feedsModel.total / ITEMS_PER_PAGE_COUNT.toDouble()).toInt()
//                }
//            }
//
//            feedsAdapter?.notifyDataSetChanged()
//        }
//
//        if (type == 7) {
//            showSnackbar("" + resources.getString(R.string.report_send_succesfully))
//        }
//    }
//
//    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
//        super.setUserVisibleHint(isVisibleToUser)
//
//        isHomeVisible = isVisibleToUser
//    }
//
//    private fun cacheFeedStreams(videoUrls: ArrayList<String>) {
//        val cachingIntent = Intent(activity, FeedsCacheJobService::class.java)
//        cachingIntent.putExtra(FeedsCacheJobService.EXTRA_VIDEOS, videoUrls)
//
//        FeedsCacheJobService.startCaching(
//            context!!, /*currentCacheJobId*/
//            JOBS_START_ID, cachingIntent
//        )
//    }
//
//    fun postLike(_id: String) {
//        if (VrockkApplication.isLoggedIn())
//            likePostViewModel.likePost("SEC " + VrockkApplication.user_obj!!.authToken, _id)
//        else
//            showLoginPopup()
//    }
//
//    fun followUnFollow(_id: String) {
//        if (VrockkApplication.isLoggedIn())
//            followUnFollowViewModel.followUnfollowPost(
//                "SEC " + VrockkApplication.user_obj!!.authToken, _id
//            )
//        else
//            showLoginPopup()
//    }
//
//    override fun onStart() {
//        super.onStart()
//
//        activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//        feedsAdapter?.resumeCurrentFeed()
//    }
//
//    override fun onResume() {
//        super.onResume()
//
//        isHomeVisible = true
//        if (feedsAdapter != null)
//            feedsAdapter?.notifyDataSetChanged()
//
//        activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//        feedsAdapter?.resumeCurrentFeed()
//    }
//
//    override fun onPause() {
//        super.onPause()
//
//        isHomeVisible = false
//
//        activity!!.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//        feedsAdapter?.pauseCurrentFeed()
//    }
//
//    override fun onStop() {
//        super.onStop()
//
//        activity!!.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//        feedsAdapter?.pauseCurrentFeed()
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//
//    }
//
//    private fun handleCoinsBottomSheet() {
//        bottomSheetBehavior = BottomSheetBehavior.from<ConstraintLayout>(bottomSheetCoins)
//        bottomSheetBehavior.setBottomSheetCallback(object :
//            BottomSheetBehavior.BottomSheetCallback() {
//            override fun onSlide(bottomSheet: View, slideOffset: Float) {
//
//            }
//
//            override fun onStateChanged(bottomSheet: View, newState: Int) {
//                when (newState) {
//                    BottomSheetBehavior.STATE_COLLAPSED -> {
//                        bottomSheet.setBackgroundResource(R.drawable.drawable_topround)
//                    }
//                    BottomSheetBehavior.STATE_EXPANDED -> {
//                        bottomSheet.setBackgroundColor(activity!!.getColor(R.color.colorBlack))
//                    }
//                    BottomSheetBehavior.STATE_DRAGGING -> {
//                        bottomSheet.setBackgroundResource(R.drawable.drawable_topround)
//                    }
//                }
//            }
//        })
//    }
//
//    private fun handleMoreBottomSheet() {
//        bottomSheetBehaviorForMore = BottomSheetBehavior.from<ConstraintLayout>(bottomSheetForMore)
//        bottomSheetBehaviorForMore.setBottomSheetCallback(object :
//            BottomSheetBehavior.BottomSheetCallback() {
//            override fun onSlide(bottomSheet: View, slideOffset: Float) {
//
//            }
//
//            override fun onStateChanged(bottomSheet: View, newState: Int) {
//                when (newState) {
//                    BottomSheetBehavior.STATE_COLLAPSED -> {
//                        bottomSheetBehaviorForMore.peekHeight = 0
//                    }
//                }
//            }
//        })
//    }
//
////    override fun onItemClicked(position: Int, type: String) {
////        if (type == GIFT) {
////
////        } else if (type == HOME_CLICK) {
////            otherUserProfileId = "" + feedDataModels[position].userId._id
////            giftsBSFragment = GiftsBSFragment(activity!!)
////            giftsBSFragment.show(activity!!.supportFragmentManager, giftsBSFragment.tag)
////
////        } else if (type == "more") {
////
////        } else if (type == "comment") {
////            if (VrockkApplication.isLoggedIn()) {
////                pauseFeed()
////                val commentIntent = Intent(context, CommentActivity::class.java)
////                commentIntent.putExtra("_id", feedDataModels[position]._id)
////                commentIntent.putExtra("position", position)
////                startActivityForResult(commentIntent, 20)
////            } else
////                showLoginPopup()
////
////        } else if (type == "other") {
////            if (VrockkApplication.isLoggedIn()) {
////                pauseFeed()
////                otherUserProfileId = "" + feedDataModels[position].userId._id
////                val othersProfileIntent = Intent(context, OtherProfileActivity::class.java)
////                othersProfileIntent.putExtra("_id", feedDataModels[position].userId._id)
////                othersProfileIntent.putExtra("position", position)
////                startActivityForResult(othersProfileIntent, 30)
////            } else
////                showLoginPopup()
////
////        } else if (type == "share") {
////            settingsViewModel.shortenDeepLinkUrl(
////                "$BASE_URL/api/user/deeplink?url=vrockk%3A%2F%2F%26postId%3D${feedDataModels[position]._id}"
////            )
////
////        } else if (type == "duet") {
////            pauseFeed()
////            val duetIntent = Intent(context, DuetMainActivity::class.java)
////            if (feedDataModels[position].post.contains("media.vrockk"))
////                duetIntent.putExtra("videolink", feedDataModels[position].post)
////            else
////                duetIntent.putExtra("videolink", feedDataModels[position].post)
////            startActivityForResult(duetIntent, 30)
////        }
////    }
//
//    private fun shareDeepLinkUrl(shortUrl: String) {
//        val shareIntent = Intent()
//        shareIntent.action = Intent.ACTION_SEND
//        shareIntent.type = "text/plain"
//        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "vRockk")
//        shareIntent.putExtra(Intent.EXTRA_TEXT, shortUrl)
//        startActivity(Intent.createChooser(shareIntent, "Post"))
//    }
//
//    fun showReportDialog(_id: String) {
//        val dialog1 = Dialog(activity!!)
//        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog1.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
//        dialog1.setContentView(R.layout.dialog_report)
//        dialog1.show()
//
//        dialog1.tvSpam.setOnClickListener {
//            showConfirmReportDialog(_id, "it's spam", dialog1)
//            dialog1.dismiss()
//        }
//        dialog1.tvInformation.setOnClickListener {
//            showConfirmReportDialog(_id, "False Information", dialog1)
//            dialog1.dismiss()
//        }
//        dialog1.tvBullying.setOnClickListener {
//            showConfirmReportDialog(_id, "Bullying and harassment", dialog1)
//            dialog1.dismiss()
//        }
//        dialog1.tvJust.setOnClickListener {
//            showConfirmReportDialog(_id, "I just don't Like", dialog1)
//            dialog1.dismiss()
//        }
//        dialog1.tvInpropriate.setOnClickListener {
//            showConfirmReportDialog(_id, "Inappropriate Content", dialog1)
//            dialog1.dismiss()
//        }
//    }
//
//    private fun showConfirmReportDialog(
//        _id: String,
//        message: String,
//        dialog: Dialog
//    ) {
//        val dialog1 = Dialog(activity!!)
//        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog1.setContentView(R.layout.dialog_confirmation_report)
//        dialog1.show()
//
//        dialog1.tvTitle.text =
//            resources.getString(R.string.are_you_sure_you_want_to_report_this_post)
//        dialog1.tvNo.setOnClickListener {
//            dialog1.dismiss()
//        }
//        dialog1.tvYes.setOnClickListener {
//            reportViewModel.hitReport(
//                "SEC " + VrockkApplication.user_obj!!.authToken, _id, message, "Post"
//            )
//            dialog1.dismiss()
//            dialog.dismiss()
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == Activity.RESULT_OK && requestCode == 20) {
//            if (data?.hasExtra("count")!!) {
//                val count = data.getIntExtra("count", 0)
//                val position = data.getIntExtra("position", 0)
//                feedDataModels[position].totalComments = count
//                feedsAdapter?.notifyItemChanged(position)
//            }
//        } else if (resultCode == Activity.RESULT_OK && requestCode == 30) {
//            if (data?.hasExtra("isFollow")!! && data.getStringExtra("isFollow") == "block") {
//                playingFeedsPageNo = 1
//
//                requestFeeds(false)
//
//            } else {
//                if (data.hasExtra("followStatus")) {
//                    val followstatus = data.getStringExtra("followStatus")
//                    if (followstatus != "") {
//                        val position = data.getIntExtra("position", 0)
//                        feedDataModels[position].isFollowing = followstatus != "follow"
//                    }
//                }
//                feedsAdapter?.notifyDataSetChanged()
//            }
//        }
//    }
//
//    fun showDisablePopup() {
//        val myCustomDlg = Dialog(activity!!)
//        myCustomDlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        myCustomDlg.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
//        myCustomDlg.setContentView(R.layout.my_custom_alert)
//        myCustomDlg.tvTitleCustom.text = resources.getString(R.string.alert)
//        myCustomDlg.tvAlertMessage.text =
//            resources.getString(R.string.your_account_is_disabled_by_admin)
//        myCustomDlg.positiveBtn.text = resources.getString(R.string.ok)
//        myCustomDlg.noBtn.visibility = View.GONE
//        myCustomDlg.show()
//        myCustomDlg.positiveBtn.setOnClickListener {
//            myCustomDlg.dismiss()
//            val editor = VrockkApplication.prefs.edit()
//
//            val sharedPreference = PreferenceHelper.defaultPrefs(activity!!)
//            val deviceToken = sharedPreference.getString(PreferenceHelper.Key.FCMTOKEN, "")
//            editor!!.remove(PreferenceHelper.Key.REGISTEREDUSER)
//            editor.putString(PreferenceHelper.Key.FCMTOKEN, deviceToken)
//            editor.apply()
//            VrockkApplication.user_obj = null
//
//            activity!!.startActivity(Intent(activity!!, LoginActivity::class.java))
//            activity!!.finishAffinity()
//
//            try {
//                LoginManager.getInstance().logOut();
//            } catch (e: java.lang.Exception) {
//
//            }
//        }
//    }
}

