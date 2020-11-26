package com.vrockk.view.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.gson.JsonObject
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.api.ApiResponse
import com.vrockk.api.IMAGE_BASE_URL
import com.vrockk.api.Status
import com.vrockk.api.parsers.ProfileParser
import com.vrockk.base.BaseActivity
import com.vrockk.base.BaseFragment
import com.vrockk.models.profile.profile_page.Post
import com.vrockk.view.following.FollowersActivity
import com.vrockk.view.following.FollowingActivity
import com.vrockk.view.profile.FavouriteFragment
import com.vrockk.view.profile.GalleryFragment
import com.vrockk.view.settings.SettingsActivity
import com.vrockk.view.settings.ViewProfileActivity
import com.vrockk.viewmodels.*
import kotlinx.android.synthetic.main.fragment_gallery.*
import kotlinx.android.synthetic.main.fragment_profile.*
import org.koin.android.viewmodel.ext.android.viewModel


class ProfileFragment : BaseFragment() {
    companion object {
        var posts: ArrayList<Post> = ArrayList()
        var dataList = ArrayList<Post>()
        var dataListTemp = ArrayList<Post>()

    }

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
    var refreshFromResume: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initProgress(contentLoadingProgressBar)

        pageCount = 1
        dataList.clear()
        dataListTemp.clear()
        posts.clear()

        profilePageViewModel.profilePageResponse()
            .observe(viewLifecycleOwner, Observer<ApiResponse> { response ->
                this.dataResponse(1, response)
            })

        getUserProfileViewModel.getUserProfileResponse()
            .observe(viewLifecycleOwner, Observer<ApiResponse> { response ->
                this.dataResponse(2, response)
            })

        postFavouriteProfileViewModel.postFavProfileResponse()
            .observe(viewLifecycleOwner, Observer<ApiResponse> { response ->
                this.dataResponse(3, response)
            })

        followUnfollowViewModel.followUnfollowResponse()
            .observe(viewLifecycleOwner, Observer<ApiResponse> { response ->
                this.dataResponse(3, response)
            })

        userBlockViewModel.userBlockResponse()
            .observe(viewLifecycleOwner, Observer<ApiResponse> { response ->
                this.dataResponse(4, response)
            })

//        addScrollListener()
    }

    fun init() {
        try {
            val pagerAdapter = ScreenSlidePagerAdapter(childFragmentManager)

            var galleryFragment = GalleryFragment()
            pagerAdapter.addFragment(galleryFragment, "")
            pagerAdapter.addFragment(FavouriteFragment(), "")
            vpMyOrder?.adapter = pagerAdapter

            myOrdersTab!!.setupWithViewPager(vpMyOrder)
            myOrdersTab.getTabAt(0)!!.icon = resources.getDrawable(R.drawable.gallary)
            myOrdersTab.getTabAt(1)!!.icon = resources.getDrawable(R.drawable.heart)
        } catch (e: Exception) {

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

    override fun onTabSwitched() {
        if (dataList.size == 0) {
            profilePageViewModel.profilePagePost(
                getMyAuthToken(),
                pageCount,
                15,
                ""
            )
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()

        DashboardActivity.classType = "profile"

        pageCount = 1

        if (dataList.size > 0)
            dataList.clear()

        if (dataListTemp.size > 0)
            dataListTemp.clear()

        if (posts.size > 0)
            posts.clear()

        ivSetting.setImageResource(R.drawable.settings)

        tvProfile.text = "" + activity!!.resources.getString(R.string.profile)
        ivHeart.visibility = View.GONE
        ivGift.visibility = View.GONE
        ibBack.visibility = View.GONE

        ivSetting.setOnClickListener {
            (activity as DashboardActivity).navigate(SettingsActivity::class.java)
        }

        refreshFromResume = true
        profilePageViewModel.profilePagePost(
            getMyAuthToken(),
            pageCount,
            15,
            ""
        )

        tvProfile.setOnClickListener {
            startActivity(Intent(context, ViewProfileActivity::class.java))
        }
    }

    private fun handleClickEvents() {
        try {
            linearFollowing.setOnClickListener {
                startActivity(
                    Intent(context, FollowingActivity::class.java)
                        .putExtra("profileId", profileId)
                )
            }

            linearFollowers.setOnClickListener {
                startActivity(
                    Intent(context, FollowersActivity::class.java)
                        .putExtra("profileId", profileId)
                )
            }
        } catch (e: Exception) {

        }
    }

    private fun dataResponse(type: Int, response: ApiResponse) {
        when (response.status) {
            Status.LOADING -> {
                if (pageCount == 1 && !refreshFromResume) {
                    showProgress()
                }
            }
            Status.SUCCESS -> {
                hideProgress()
                renderResponse(type, response)
            }
            Status.ERROR -> {
                hideProgress()
                var errJsonStr = Gson().toJson(response.error)
                val errJsonObject: JsonObject = Gson().fromJson(errJsonStr, JsonObject::class.java)

                if(errJsonObject.get("code")?.toString() == "401"){
                    (context as BaseActivity).showLoginPopup()
                }
            }
        }
    }

    private fun renderResponse(type: Int, response: ApiResponse) {
        Log.e("home_response: ", Gson().toJson(response))

        if (type == 1) { //profilePageViewModel
            try {
                val profilePageResponse = ProfileParser.parseResponse(response.data)
                if (profilePageResponse != null) {
                    pagesFromApi = profilePageResponse.data.total / 15.0
                    totalPages = Math.ceil(pagesFromApi).toInt()

                    if (pageCount == 1)
                        dataList.clear()
                    dataList.addAll(profilePageResponse.data.posts)
                    if (pageCount > 1) {
                        rvGallery.adapter!!.notifyDataSetChanged()
                    }

                    if (profilePageResponse.success) {
                        if (VrockkApplication.user_obj?._id == profilePageResponse.data.profile[0]._id) {
                            ivHeart.visibility = View.GONE
                            ivGift.visibility = View.GONE
                            ibBack.visibility = View.GONE
                        } else {
                            ivHeart.visibility = View.VISIBLE
                            ivGift.visibility = View.VISIBLE
                            ibBack.visibility = View.VISIBLE
                        }

                        tvTotalLikes.setText("" + profilePageResponse.data.profile.get(0).totalLikes)
                        tvTotalFollowrs.setText("" + profilePageResponse.data.profile.get(0).followers)
                        tvTotalFollowing.setText("" + profilePageResponse.data.profile.get(0).following)

                        if (profilePageResponse.data.profile.get(0).profilePic.contains("media.vrockk")) {
                            var profileImageUrl: String =
                                profilePageResponse.data.profile.get(0).profilePic
                            Glide.with(activity!!).load(profileImageUrl)
                                .placeholder(context!!.resources.getDrawable(R.drawable.user_placeholder))
                                .error(context!!.resources.getDrawable(R.drawable.user_placeholder))
                                .into(ivProfile!!)
                        } else {
                            var profileImageUrl: String =
                                IMAGE_BASE_URL + profilePageResponse.data.profile.get(0).profilePic
                            Glide.with(activity!!).load(profileImageUrl)
                                .placeholder(context!!.resources.getDrawable(R.drawable.user_placeholder))
                                .error(context!!.resources.getDrawable(R.drawable.user_placeholder))
                                .into(ivProfile!!)
                        }

                        tvName.setText(
                            "" + profilePageResponse.data.profile.get(0).firstName + " " + profilePageResponse.data.profile.get(
                                0
                            ).lastName
                        )
                        tvUserBio.text = profilePageResponse.data.profile[0].bio
                        if (!profilePageResponse.data.profile.get(0).equals(""))
                            tvUserName.text = "@" + profilePageResponse.data.profile.get(0).userName

                        val facebookUrl: String = profilePageResponse.data.profile[0].facebook
                        val instagramUrl = profilePageResponse.data.profile[0].instagram
                        val youtubeUrl = profilePageResponse.data.profile[0].youtube

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

                        Handler().postDelayed({
                            posts = profilePageResponse.data.posts
                            init()
                            handleClickEvents()

                        }, 150)
                    }
                }
            } catch (e: Exception) {

            }
        }

        if (type == 4) {
            Toast.makeText(activity!!, "User blocked Successfully", Toast.LENGTH_SHORT).show()
            (activity as DashboardActivity).switchToHomeTab()
        }
    }

    class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(
        fm,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {
        override fun getCount(): Int = 2
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

    private fun popUpClickArtist() {
        val popupMenu = PopupMenu(activity, ivSetting)
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

    fun addScrollListener() {
        nestedScrollview.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            val nestedScrollView = checkNotNull(v) {
                return@setOnScrollChangeListener
            }
            val lastChild = nestedScrollView.getChildAt(nestedScrollView.childCount - 1)
            if (lastChild != null) {
                if ((scrollY >= (lastChild.measuredHeight - nestedScrollView.measuredHeight)) && scrollY > oldScrollY) {
                    pageCount++
                    profilePageViewModel.profilePagePost(
                        getMyAuthToken(),
                        pageCount,
                        15,
                        ""
                    )
                }
            }
        }
    }
}