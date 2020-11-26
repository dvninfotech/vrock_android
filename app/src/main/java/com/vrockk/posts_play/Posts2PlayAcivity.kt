package com.vrockk.view.posts_play

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.adapter.GiftsAdapter
import com.vrockk.adapter.Post2PlayAdapter
import com.vrockk.api.ApiResponse
import com.vrockk.api.BASE_URL
import com.vrockk.api.Status
import com.vrockk.api.parsers.ProfileParser
import com.vrockk.api.parsers.SettingsParser
import com.vrockk.base.BaseActivity
import com.vrockk.interfaces.ItemClickListernerWithType
import com.vrockk.models.coins.DataItem
import com.vrockk.models.profile.profile_page.Post
import com.vrockk.utils.Constant
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

class Posts2PlayAcivity : BaseActivity(), ItemClickListernerWithType {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var bottomSheetBehaviorForMore: BottomSheetBehavior<ConstraintLayout>
    private lateinit var giftsAdapter: GiftsAdapter
    private var coinList = ArrayList<DataItem>()

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

    var listPosts = ArrayList<Post>()

    lateinit var postPlayAdapter: Post2PlayAdapter

    private val homePageViewModel by viewModel<HomePageViewModel>()
    private val commonViewModel by viewModel<CommonViewModel>()

    val getFollowingViewModel by viewModel<FollowingViewModel>()

    private val likePostViewModel by viewModel<LikePostViewModel>()

    private val followUnfollowViewModel by viewModel<FollowUnfollowViewModel>()

    val profilePageViewModel by viewModel<ProfilePageViewModel>()

    val getUserProfileViewModel by viewModel<GetUserProfileViewModel>()

    private val reportViewModel by viewModel<ReportViewModel>()

    val addViewToPostViewModel by viewModel<AddViewToPostViewModel>()
    private val settingsViewModel by viewModel<SettingsViewModel>()

    private var userId = ""
    private var postId = ""
    private var position = 0

    private var totalPages: Int = 0
    private var pageNo: Int = 1
    var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts_play_acivity)

        initProgress(contentLoadingProgressBar)

        giftsBSFragment = GiftsBSFragment(this)

        instanceObj = this
        isHomeVisible = true
        index = 0
        userId = intent.getStringExtra("userId")!!
        postId = intent.getStringExtra("postId")!!
        position = intent.getIntExtra("position", 0)
        handleBottomSheetSlide()
        handleBottomSheetSlideForMore()
        setCoinAdapter()
        Log.e("position", "position $postId  ${postId} ")

        //   Log.e("hashTag", " $hashTag ")
        observer()

        ibBack.setOnClickListener {

            if (player != null) {
                player!!.stop()
                player!!.release()
            }
            finish()
        }

        ibCrossFromMore.setOnClickListener {
            bottomSheetBehaviorForMore.peekHeight = 0
            bottomSheetBehaviorForMore.state = BottomSheetBehavior.STATE_COLLAPSED
        }

    }



    private fun observer() {


        addViewToPostViewModel.uploadSongResponse()
            .observe(this, Observer<ApiResponse> { response ->
                this.dataResponse(7, response)
            })

        reportViewModel.reportResponse().observe(this, Observer<ApiResponse> { response ->
            this.dataResponse(6, response)
        })

        profilePageViewModel.profilePageResponse().observe(this, Observer<ApiResponse> { response ->
            this.dataResponse(4, response)
        })

        commonViewModel.hashTagsResponseLiveData().observe(this, Observer<ApiResponse> { response ->
            this.dataResponse(1, response)
        })
        /*getFollowingViewModel.getFollowingResponse().observe(this, Observer<ApiResponse> { response ->
            this.dataResponse(1,response)
        })
        */

        getUserProfileViewModel.getUserProfileResponse()
            .observe(this, Observer<ApiResponse> { response ->
                this.dataResponse(5, response)
            })
        likePostViewModel.likePostResponse().observe(this, Observer<ApiResponse> { response ->
            this.dataResponse(2, response)
        })

        followUnfollowViewModel.followUnfollowResponse()
            .observe(this, Observer<ApiResponse> { response ->
                this.dataResponse(3, response)
            })

        settingsViewModel.shortenUrlResponseLiveData().observe(this, Observer {
            val shortUrlData = SettingsParser.parseShortUrlResponse(it.data)
            if (shortUrlData != null) {
                shareDeepLinkUrl(shortUrlData.shortUrl)
            }
        })
    }

    private fun hitApi() {
        index = 0
        // commonViewModel.getHashTags(1,10,hashTag,"",VrockkApplication.user_obj?._id?:"")
        Log.e("postId", " $postId ")
        if (DashboardActivity.classType == "profile") {
            // profilePageViewModel.profilePagePost("SEC "+VrockkApplication.user_obj!!.authToken,"1","10",postId)
            getUserProfileViewModel.getUserProfilePost(
                VrockkApplication.user_obj?.id ?: "",
                1,
                10,
                VrockkApplication.user_obj?.id ?: "",
                postId
            )
        } else {
            getUserProfileViewModel.getUserProfilePost(
                userId,
                1,
                10,
                VrockkApplication.user_obj?.id ?: "",
                postId
            )
        }

    }

    private fun dataResponse(type: Int, response: ApiResponse?) {
        when (response?.status) {
            Status.LOADING -> {
                isLoading = true
                if (type == 5)
                    showProgress("")
            }
            Status.SUCCESS -> {
                isLoading = false
                if (type == 5)
                    hideProgress()

                if (response != null)
                    renderResponse(type, response)
            }
            Status.ERROR -> {
                isLoading = false
                if (type == 5)
                    hideProgress()
            }
        }
    }

    private fun renderResponse(type: Int, response: ApiResponse) {
        Log.e("home_response: ", Gson().toJson(response))

        if (type == 4 || type == 5) {

//                val data: String = Utils.toJson(response.data)
//                val gson1 = Gson();
//                val userProfileResponse = gson1.fromJson(data, ProfilePageResponse::class.java)
            val userProfileResponse = ProfileParser.parseResponse(response.data)
            if (userProfileResponse != null && userProfileResponse.success) {
                if (userProfileResponse.data.posts.size > 0) {

                    if (player != null && player!!.isPlaying)
                        player!!.playWhenReady = false

                    val pagesFromApi = userProfileResponse.data.total / 10.0
                    totalPages = ceil(pagesFromApi).toInt()
                    Log.e("totalPages", " $totalPages")

                    if (pageNo == 1) {
                        listPosts.clear()
                        listPosts.addAll(userProfileResponse.data.posts)
                        postPlayAdapter = Post2PlayAdapter(this, listPosts, instanceObj, this)
                        rvHome.adapter = postPlayAdapter
                        rvHome.layoutManager =
                            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                    } else {
                        listPosts.addAll(userProfileResponse.data.posts)
                    }

                    val snapHelper = PagerSnapHelper()
                    rvHome.onFlingListener = null
                    snapHelper.attachToRecyclerView(rvHome)

                    if (VrockkApplication.user_obj != null) {
                        Log.e("call", "111 token: " + VrockkApplication.user_obj!!.authToken)
                        Log.e("call", "111 post id :" + userProfileResponse.data.posts[index]._id)
                        if (userProfileResponse.data.posts[index].userId.id ?: "" != VrockkApplication.user_obj?._id ?: "")
                            addViewToPostViewModel.addViewToPost(
                                "SEC " + VrockkApplication.user_obj!!.authToken,
                                userProfileResponse.data.posts[index]._id
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
                                    val centerView = snapHelper.findSnapView(rvHome.layoutManager)
                                    val pos = rvHome.layoutManager!!.getPosition(centerView!!)
                                    Log.e("call", "itemPosition : $pos")
                                    player!!.playWhenReady = false
                                    index = pos
                                    rvHome.adapter!!.notifyDataSetChanged()

                                    if (VrockkApplication.user_obj != null) {
                                        if (userProfileResponse.data.posts[index].userId.id ?: "" != VrockkApplication.user_obj?._id ?: "")
                                            addViewToPostViewModel.addViewToPost(
                                                "SEC " + VrockkApplication.user_obj!!.authToken,
                                                userProfileResponse.data.posts.get(index)._id
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
                                    if (DashboardActivity.classType == "profile") {
                                        // profilePageViewModel.profilePagePost("SEC "+VrockkApplication.user_obj!!.authToken,"1","10",postId)
                                        getUserProfileViewModel.getUserProfilePost(
                                            VrockkApplication.user_obj?.id ?: "",
                                            pageNo,
                                            10,
                                            VrockkApplication.user_obj?.id ?: "",
                                            postId
                                        )
                                    } else {
                                        getUserProfileViewModel.getUserProfilePost(
                                            userId,
                                            pageNo,
                                            10,
                                            VrockkApplication.user_obj?.id ?: "",
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

        if (type == 6) {
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

    override fun onResume() {
        super.onResume()
        index = 0
        hitApi()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onStop() {
        super.onStop()
        if (isFinishing) {
            if (player != null) {
                player!!.stop()
                player!!.release()
            }
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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

        dialog1.tvTitle.text =
            "" + resources.getString(R.string.are_you_sure_you_want_to_report_this_post)

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

        /*for(i in coinImgs.indices){
            val model = CoinsModel()
            model.icon = coinImgs[i]
            model.name = coinNames[i]
            model.coinValue = coinCount[i]
            coinList.add(model)
        }*/
        /* giftsAdapter = GiftsAdapter(this,coinList,this)
         rvGiftsList.adapter = giftsAdapter
         rvGiftsList.layoutManager = GridLayoutManager(this,3)

         ibCross.setOnClickListener {
             bottomSheetBehavior.peekHeight = 0
             bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
         }*/
    }

    override fun onItemClicked(position: Int, type: String) {
        if (type == Constant.ADAPTER_CLICK) {
            showSnackbar("Send gifts to your loved ones that they can redeem, coming soon")
            // bottomSheetBehavior.peekHeight = 1200
            //bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        } else if (type == Constant.HOME_CLICK) {
            HomeFragment.otherUserProfileId = "" + listPosts[position].userId.id
            giftsBSFragment = GiftsBSFragment(this)
            giftsBSFragment.show(supportFragmentManager, giftsBSFragment.tag)
        } else if (type == "more") {

        } else if (type == "comment") {

            if (VrockkApplication.user_obj != null) {
                val i = Intent(this, CommentActivity::class.java)
                i.putExtra("_id", listPosts.get(position)._id)
                Log.e("position", " $position ")
                i.putExtra("position", position)
                startActivityForResult(i, 20)
            } else {
                showLoginPopup()
            }

        } else if (type == "other") {
            if (VrockkApplication.user_obj != null) {
                HomeFragment.otherUserProfileId = "" + listPosts[position].userId.id
                val i = Intent(this, OtherProfileActivity::class.java)
                i.putExtra("_id", listPosts[position].userId.id)
                i.putExtra("position", position)
                startActivityForResult(i, 30)
            } else {
                showLoginPopup()
            }
        } else if (type == "share") {
//            val link: String =
//                "$BASE_URL/api/user/deeplink?url=vrockk%3A%2F%2F%26postId%3D${listPosts[position]._id}"
//            val shareIntent = Intent()
//            shareIntent.action = Intent.ACTION_SEND
//            shareIntent.type = "text/plain"
//            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "vRockk")
//            shareIntent.putExtra(Intent.EXTRA_TEXT, link)
//            startActivity(Intent.createChooser(shareIntent, "Post"))
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
                postPlayAdapter.notifyItemChanged(position)
            }
        } else if (resultCode == Activity.RESULT_OK && requestCode == 30) {

            if (data?.hasExtra("isFollow")!! && data.getStringExtra("isFollow") == "block") {
                hitApi()
            } else {
                postPlayAdapter.notifyDataSetChanged()
            }
        }
    }
}