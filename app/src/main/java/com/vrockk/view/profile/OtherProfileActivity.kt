package com.vrockk.view.profile


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.api.ApiResponse
import com.vrockk.api.IMAGE_BASE_URL
import com.vrockk.api.Status
import com.vrockk.api.parsers.ProfileParser
import com.vrockk.base.BaseActivity
import com.vrockk.utils.Utils
import com.vrockk.view.dashboard.DashboardActivity
import com.vrockk.view.dashboard.ProfileFragment.Companion.dataList
import com.vrockk.view.dashboard.ProfileFragment.Companion.dataListTemp
import com.vrockk.view.dashboard.ProfileFragment.Companion.posts
import com.vrockk.view.following.FollowersActivity
import com.vrockk.view.following.FollowingActivity
import com.vrockk.view.fragment.GiftsBSFragment
import com.vrockk.view.home.HomeFragment
import com.vrockk.view.settings.SettingsActivity
import com.vrockk.view.settings.ViewProfileActivity
import com.vrockk.viewmodels.*
import kotlinx.android.synthetic.main.activity_other_profile.*
import kotlinx.android.synthetic.main.fragment_gallery.*
import kotlinx.android.synthetic.main.layout_loader.*
import org.koin.android.viewmodel.ext.android.viewModel


class OtherProfileActivity : BaseActivity() {

    var _id: String? = ""

    var pageCount: Int = 1
    var totalPages: Int = 1
    var pagesFromApi: Double = 0.00

    var profileId: String = ""

    val profilePageViewModel by viewModel<ProfilePageViewModel>()

    val getUserProfileViewModel by viewModel<GetUserProfileViewModel>()

    val postFavouriteProfileViewModel by viewModel<PostFavouriteProfileViewModel>()

    val followUnfollowViewModel by viewModel<FollowUnfollowViewModel>()

    val userBlockViewModel by viewModel<UserBlockViewModel>()

    var isLikedTabSelected: Boolean = false

    var followStatus: String = ""

    private lateinit var giftsBSFragment: GiftsBSFragment

    companion object {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_profile)

        initProgress(contentLoadingProgressBar)

        _id = intent.getStringExtra("_id")
        if (_id == null)
            _id = ""
        DashboardActivity.classType = ""
        pageCount = 1
        dataList.clear()
        dataListTemp.clear()
        posts.clear()

        profilePageViewModel.profilePageResponse().observe(this, Observer<ApiResponse> { response ->
            this.dataResponse(1, response)
        })

        getUserProfileViewModel.getUserProfileResponse()
            .observe(this, Observer<ApiResponse> { response ->
                this.dataResponse(2, response)
            })

        postFavouriteProfileViewModel.postFavProfileResponse()
            .observe(this, Observer<ApiResponse> { response ->
                this.dataResponse(3, response)
            })

        followUnfollowViewModel.followUnfollowResponse()
            .observe(this, Observer<ApiResponse> { response ->
                this.dataResponse(3, response)
            })

        userBlockViewModel.userBlockResponse()
            .observe(this, Observer<ApiResponse> { response ->
                this.dataResponse(4, response)
            })


        pagination()

        ivGift.setOnClickListener {
            if (VrockkApplication.user_obj != null) {
                // showSnackbar(resources.getString(R.string.send_gifts_to_your_loved))
                HomeFragment.otherUserProfileId = "" + profileId
                GiftsBSFragment.otherUserProfileId = profileId
                giftsBSFragment = GiftsBSFragment(this)
                giftsBSFragment.show(supportFragmentManager, giftsBSFragment.tag)
            } else {
                showLoginPopup()
            }
        }

        getUserProfileViewModel.getUserProfilePost(
            _id!!,
            pageCount,
            15,
            VrockkApplication.user_obj?.id ?: "",
            ""
        )

    }


    fun init() {

        myOrdersTab?.visibility = View.GONE

        try {
            val pagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)

            var galleryFragment: GalleryFragment = GalleryFragment()
            pagerAdapter.addFragment(galleryFragment, "")
//            pagerAdapter.addFragment(FavouriteFragment(), "")
            vpMyOrder?.adapter = pagerAdapter

            myOrdersTab!!.setupWithViewPager(vpMyOrder)
            myOrdersTab.getTabAt(0)!!.icon = resources.getDrawable(R.drawable.gallary)
//            myOrdersTab.getTabAt(1)!!.icon = resources.getDrawable(R.drawable.heart)
        } catch (e: Exception) {
            Log.e("call", "exption:" + e.toString())
        }

        try {
            myOrdersTab!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    vpMyOrder!!.currentItem = tab.position

                }

                override fun onTabUnselected(tab: TabLayout.Tab) {

                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                }
            })

        } catch (e: Exception) {

        }

    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()

        DashboardActivity.classType = ""

//      pageCount = 1
//        totalPages = 1
//        totalPages = 1
//
//        if (dataList.size > 0)
//            dataList.clear()
//
//        if (dataListTemp.size > 0)
//            dataListTemp.clear()
//
//        if (posts.size > 0)
//            posts.clear()

        ivSetting.setImageResource(R.drawable.three_dots)

//        tvProfile.text = resources.getString(R.string.following)
        ivHeart.visibility = View.VISIBLE
        ivGift.visibility = View.VISIBLE
        ibBack.visibility = View.VISIBLE

        ibBack.setOnClickListener {
            onbackIntent()
        }


        ivSetting.setOnClickListener {

            if (VrockkApplication.user_obj != null) {
                popUpClickArtist()
            } else {
                showLoginPopup()
            }
        }

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }


    private fun dataResponse(type: Int, response: ApiResponse) {
        when (response.status) {
            Status.LOADING -> {
                if (pageCount == 1)
                    showProgress("")
            }
            Status.SUCCESS -> {
                hideProgress()
                renderResponse(type, response)
            }
            Status.ERROR -> {

                hideProgress()
                Log.e("home_error", Gson().toJson(response.error))
                //showCustomToastMessage(this, "Something went wrong!")
            }
        }
    }

    private fun renderResponse(type: Int, response: ApiResponse) {
        Log.e("home_response: ", Gson().toJson(response))

        if (type == 2) {
            val data: String = Utils.toJson(response.data)
            val gson1 = Gson();

            try {

//                    val userProfileResponse = gson1.fromJson(data, ProfilePageResponse::class.java)
                val userProfileResponse = ProfileParser.parseResponse(response.data)
                if (userProfileResponse != null && userProfileResponse.success) {

                    try {
                        pagesFromApi = userProfileResponse.data.total!! / 15.0
                        totalPages = Math.ceil(pagesFromApi).toInt()

                        if (pageCount > 1) {
                            dataList.addAll(userProfileResponse.data.posts)
                        } else {
                            dataList.clear()
                            dataList.addAll(userProfileResponse.data.posts)
                        }

                        // dataList = userProfileResponse.data.posts

                        Log.e("call", "response total pages: " + totalPages)
                        Log.e("call", "response total: " + userProfileResponse.data.total!!)
                        Log.e("call", "response list size: " + dataList!!.size)
                        Log.e("call", "response count size: " + pageCount)

                        if (pageCount > 1) {

                            rvGallery.adapter!!.notifyDataSetChanged()

//                                if (dataListTemp.size > 0)
//                                {
//                                    Log.e("call","lisssstt temp "+dataListTemp.size)
//
//                                    dataListTemp!!.addAll(dataList!!)
//                                    rvGallery.adapter!!.notifyDataSetChanged()
//                                }
                        }

                    } catch (e: Exception) {
                        Log.e("call", "excepion: " + e.toString())
                    }

                    tvProfile.visibility = View.VISIBLE
                    ivSetting.visibility = View.VISIBLE

                    if (userProfileResponse.data.profile.get(0).profilePic.equals("media.vrockk")) {
                        var profileImageUrl: String =
                            userProfileResponse.data.profile.get(0).profilePic
                        Glide.with(this).load(profileImageUrl)
                            .placeholder(resources.getDrawable(R.drawable.user_placeholder))
                            .error(resources.getDrawable(R.drawable.user_placeholder))
                            .into(ivProfile!!)
                    } else {
                        var profileImageUrl: String =
                            IMAGE_BASE_URL + userProfileResponse.data.profile.get(0).profilePic
                        Glide.with(this).load(profileImageUrl)
                            .placeholder(resources.getDrawable(R.drawable.user_placeholder))
                            .error(resources.getDrawable(R.drawable.user_placeholder))
                            .into(ivProfile!!)
                    }

                    tvName.setText(
                        "" + userProfileResponse.data.profile.get(0).firstName + " " + userProfileResponse.data.profile.get(
                            0
                        ).lastName
                    )

                    tvUserBio.text = userProfileResponse.data.profile[0].bio

                    if (!userProfileResponse.data.profile.get(0).equals(""))
                        tvUserName.text = "@" + userProfileResponse.data.profile.get(0).userName

                    if (VrockkApplication.user_obj?._id == userProfileResponse.data.profile[0]._id) {
                        ivHeart.visibility = View.GONE
                        ivGift.visibility = View.GONE
                        ibBack.visibility = View.GONE
                    } else {
                        ivHeart.visibility = View.VISIBLE
                        ivGift.visibility = View.VISIBLE
                        ibBack.visibility = View.VISIBLE

                        val facebookUrl: String = userProfileResponse.data.profile[0].facebook
                        val instagramUrl = userProfileResponse.data.profile[0].instagram
                        val youtubeUrl = userProfileResponse.data.profile[0].youtube

                        if (facebookUrl.isEmpty()) {
                            ivFacebook.visibility = View.GONE
                        } else {
                            ivFacebook.visibility = View.VISIBLE
                            ivFacebook.setOnClickListener {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("fb://facewebmodal/f?href=" + facebookUrl)
                                )
                                try {
                                    startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        if (instagramUrl.isEmpty()) {
                            ivInstagram.visibility = View.GONE
                        } else {
                            ivInstagram.visibility = View.VISIBLE
                            ivInstagram.setOnClickListener {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(instagramUrl)
                                )
                                try {
                                    startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        if (youtubeUrl.isEmpty()) {
                            ivYoutube.visibility = View.GONE
                        } else {
                            ivYoutube.visibility = View.VISIBLE
                            ivYoutube.setOnClickListener {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(youtubeUrl)
                                )
                                try {
                                    startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }



                    profileId = userProfileResponse.data.profile.get(0)._id

                    if (profileId == VrockkApplication.user_obj?._id) {

                        tvProfile.text = getString(R.string.profile)
                        tvProfile.setOnClickListener {

                            if (VrockkApplication.user_obj != null) {
                                startActivity(Intent(this, ViewProfileActivity::class.java))
                            } else {
                                showLoginPopup()
                            }
                        }

                        ivSetting.setImageResource(R.drawable.settings)

                        ivSetting.setOnClickListener {
                            navigate(SettingsActivity::class.java)
                        }


                    } else {
                        if (userProfileResponse.data.profile[0].isFollowing) {
                            tvProfile.text = getString(R.string.unfollow)
                        } else {
                            tvProfile.text = getString(R.string.follow)
                        }

                        tvProfile.setOnClickListener {

                            if (VrockkApplication.user_obj != null) {
                                if (userProfileResponse.data.profile[0].isFollowing) {
                                    tvProfile.text = getString(R.string.follow)
                                    followStatus = "follow"
                                    userProfileResponse.data.profile[0].isFollowing = false
                                    if (tvTotalFollowrs.text.toString().toInt() == 0) {
                                    } else {
                                        var like: Int = 0
                                        like = tvTotalFollowrs.text.toString().toInt() - 1
                                        tvTotalFollowrs.text = "" + like
                                    }
                                } else {
                                    tvProfile.text = getString(R.string.unfollow)
                                    followStatus = "unfollow"
                                    userProfileResponse.data.profile[0].isFollowing = true
                                    var like: Int = 0
                                    like = tvTotalFollowrs.text.toString().toInt() + 1
                                    tvTotalFollowrs.text = "" + like
                                }
                                followUnfollowViewModel.followUnfollowPost(
                                    "SEC " + VrockkApplication.user_obj!!.authToken,
                                    userProfileResponse.data.profile.get(0)._id
                                )

                            } else {
                                showLoginPopup()
                            }

                        }

                        if (userProfileResponse.data.profile.get(0).isFavourite) {
                            ivHeart.setImageResource(R.drawable.yellowbox_heart_black)
                        } else {
                            ivHeart.setImageResource(R.drawable.yellowbox_heart)
                        }

                        ivHeart.setOnClickListener {
                            if (VrockkApplication.user_obj != null) {
                                if (userProfileResponse.data.profile.get(0).isFavourite) {
                                    ivHeart.setImageResource(R.drawable.yellowbox_heart)
                                    userProfileResponse.data.profile.get(0).isFavourite = false
                                } else {
                                    ivHeart.setImageResource(R.drawable.yellowbox_heart_black)
                                    userProfileResponse.data.profile.get(0).isFavourite = true
                                }

                                postFavouriteProfileViewModel.postFavProfilePost(
                                    "SEC " + VrockkApplication.user_obj!!.authToken,
                                    userProfileResponse.data.profile.get(0)._id
                                )

                            } else {
                                showLoginPopup()
                            }
                        }

                    }


                    tvTotalLikes.setText("" + userProfileResponse.data.profile.get(0).totalLikes)
                    tvTotalFollowrs.setText("" + userProfileResponse.data.profile.get(0).followers)
                    tvTotalFollowing.setText("" + userProfileResponse.data.profile.get(0).following)

                    Handler().postDelayed({
                        posts = userProfileResponse.data.posts
                        init()
                        handleClickEvents()

                    }, 150)

                }
            } catch (e: Exception) {
                Log.e("call", "exp: " + e.toString())
            }

        }

        if (type == 4) {
            Toast.makeText(this, "User blocked Successfully", Toast.LENGTH_SHORT).show()
            //onbackIntent()
            val ii = Intent()
            ii.putExtra("isFollow", "block")
            if (intent.hasExtra("position"))
                ii.putExtra("position", intent.getIntExtra("position", 0))

            setResult(Activity.RESULT_OK, ii)
            finish()
            // startActivity(Intent(context, BlockUserListActivity::class.java))
        }
    }

    class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(
        fm,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {
        override fun getCount(): Int = 1
        private val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getPageTitle(position: Int): CharSequence {
            return mFragmentTitleList[position]
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }
    }

    private fun onbackIntent() {
        val ii = Intent()
        ii.putExtra("isFollow", "")
        ii.putExtra("followStatus", followStatus)
        if (intent.hasExtra("position"))
            ii.putExtra("position", intent.getIntExtra("position", 0))
        setResult(Activity.RESULT_OK, ii)
        finish()
    }

    override fun onBackPressed() {
        onbackIntent()
    }

    private fun handleClickEvents() {

        linearFollowing.setOnClickListener {

            if (VrockkApplication.user_obj != null) {
                startActivity(
                    Intent(this, FollowingActivity::class.java)
                        .putExtra("profileId", profileId)
                )
            } else {
                showLoginPopup()
            }

        }

        linearFollowers.setOnClickListener {

            if (VrockkApplication.user_obj != null) {
                startActivity(
                    Intent(this, FollowersActivity::class.java)
                        .putExtra("profileId", profileId)
                )
            } else {
                showLoginPopup()
            }
        }
    }

    private fun popUpClickArtist() {
        val popupMenu = PopupMenu(this, ivSetting)
        popupMenu.menuInflater.inflate(R.menu.poupup_share, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.block -> {
                    userBlockViewModel.userBlock(
                        "SEC " + VrockkApplication.user_obj!!.authToken,
                        profileId
                    )
                }
            }
            true
        }
        popupMenu.show()
    }

    fun pagination() {
        nestedScrollview.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            val nestedScrollView = checkNotNull(v) {
                return@setOnScrollChangeListener
            }
            val lastChild = nestedScrollView.getChildAt(nestedScrollView.childCount - 1)
            if (lastChild != null) {
                if ((scrollY >= (lastChild.measuredHeight - nestedScrollView.measuredHeight)) && scrollY > oldScrollY) {
                    pageCount++

                    Log.e("call", "PAGING count-----> " + pageCount)
                    Log.e("call", "PAGING class type " + DashboardActivity.classType)

                    getUserProfileViewModel.getUserProfilePost(
                        _id!!,
                        pageCount,
                        15,
                        VrockkApplication.user_obj?.id ?: "",
                        ""
                    )

                }
            }
        }
    }

}
