package com.vrockk.view.dashboard

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.google.android.material.shape.CornerFamily
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.gson.Gson
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.VrockkApplication.Companion.context
import com.vrockk.adapter.ViewPagerAdapter
import com.vrockk.api.ApiResponse
import com.vrockk.api.Event
import com.vrockk.api.Status
import com.vrockk.base.BaseActivity
import com.vrockk.common.onfailure
import com.vrockk.models.upload_post.UploadPostResponse
import com.vrockk.utils.ConnectionStateMonitor
import com.vrockk.utils.ProgressRequestBody
import com.vrockk.utils.Utils
import com.vrockk.view.cameraactivity.RecordVideosActivity
import com.vrockk.viewmodels.viewmodels.CommonViewModel
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.my_custom_alert.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.File

class DashboardActivity : BaseActivity(), ProgressRequestBody.UploadCallbacks,
    ConnectionStateMonitor.ConnectivityChangedListener {
    companion object {
        const val REQUEST_APP_UPDATE_FLEXIBLE = 0x2345
        const val REQUEST_APP_UPDATE_IMMIDIATE = 0x2347

        const val TAB_HOME = "home"
        const val TAB_SEARCH = "search"
        const val TAB_CREATE_VIDEO = "create_video"
        const val TAB_NOTIFICATIONS = "challenge"
        const val TAB_PROFILE = "profile"

        const val EXTRA_NOTIFICATION = "notification"
        const val EXTRA_UPLOAD = "upload"
        const val EXTRA_DUET = "duet"

        const val CLASS_TYPE_OTHER_PROFILE = "other_profile"

        const val CLICKED_VIDEO = "video"

        var refreshProfile = true
        var refreshNotifications = true

        var fragmentTag: String = ""
        var instance: DashboardActivity? = null
        var classType: String = ""
    }

    val hashMap: HashMap<String, RequestBody> = HashMap()

    private val commonViewModel by viewModel<CommonViewModel>()

    private val fragments = ArrayList<Fragment>()
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    var absolute_path: String = ""
    var description_text: String = ""
    var song_id: String = ""
    var latitude: String = ""
    var longitude: String = ""
    var hashTags: String = ""
    var uploaded: String = ""
    var path: String = ""
    var type: String = ""
    var click = ""
    var isVideoPost: Boolean = false
    var pagerChangeByClick: Boolean = false

    var currentTabPosition: Int? = null

//    var postsFragment: FeedsFragment? = null
//    var postsFragment: HomeFragment? = null
    var postsFragment: PostsFragment? = null
    var searchFragment: SearchFragment? = null
    var notificationFragment: NotificationFragment? = null
    var profileFragment: ProfileFragment? = null

    lateinit var appUpdateManager: AppUpdateManager
    private val installStateUpdatedListener: InstallStateUpdatedListener? =
        InstallStateUpdatedListener {
            when (it.installStatus()) {
                InstallStatus.CANCELED -> showToast("Update installation cancelled")
                InstallStatus.DOWNLOADING -> showToast("Update downloading...")
                InstallStatus.DOWNLOADED -> {
                    showToast("Update downloaded")
                    appUpdateManager.completeUpdate()
                }
                InstallStatus.INSTALLING -> showToast("Update installing...")
                InstallStatus.INSTALLED -> showToast("Update installed...")
                else -> Log.i(TAG, "")
            }
        }

    private fun checkForUpdates() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask?.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                || appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            ) {
                try {
                    when {
                        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> {
                            appUpdateManager.registerListener(installStateUpdatedListener)
                            appUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo,
                                AppUpdateType.FLEXIBLE,
                                this,
                                REQUEST_APP_UPDATE_FLEXIBLE
                            )
                        }
                        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> {
//                        appUpdateManager.registerListener(installStateUpdatedListener)
                            appUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo,
                                AppUpdateType.IMMEDIATE,
                                this,
                                REQUEST_APP_UPDATE_IMMIDIATE
                            )
                        }
                        else -> Log.i(TAG, "checkForUpdates: No updates available")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if ((requestCode == REQUEST_APP_UPDATE_IMMIDIATE || requestCode == REQUEST_APP_UPDATE_FLEXIBLE)
            && resultCode != RESULT_OK
        ) {
            try {
                appUpdateManager.unregisterListener(installStateUpdatedListener)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun requestPermissions(s: String) {
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
        ) + ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@DashboardActivity,
                    Manifest.permission.CAMERA
                )
                && ActivityCompat.shouldShowRequestPermissionRationale(
                    this@DashboardActivity,
                    Manifest.permission.RECORD_AUDIO
                ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this@DashboardActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this@DashboardActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this@DashboardActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                onfailure(
                    this,
                    getString(R.string.pleasegiveallrequiredpermissions),
                    getString(R.string.gotosettings),
                    this@DashboardActivity
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    1
                )
            }
        } else {
            if (s == CLICKED_VIDEO)
                navigate(RecordVideosActivity::class.java)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (click == CLICKED_VIDEO)
                    navigate(RecordVideosActivity::class.java)
            }
        }
    }

    fun statusBarTransparent() {
        statusBarColor()
        transparentStatusBar()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkForUpdates()

        setContentView(R.layout.activity_dashboard)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN
        )

        init()

        if (intent.hasExtra("upload")) {
            absolute_path = intent.getStringExtra("absolutePath")
            description_text = intent.getStringExtra("description_text")
            song_id = intent.getStringExtra("song_id")
            latitude = intent.getStringExtra("latitude")
            longitude = intent.getStringExtra("longitude")
            hashTags = intent.getStringExtra("hashtaglist")
            uploaded = intent.getStringExtra("uploaded")
            path = intent.getStringExtra("path")
            type = intent.getStringExtra("type")

            val radius = resources.getDimensionPixelSize(R.dimen._2sdp)
            videoPlaceholder.shapeAppearanceModel = videoPlaceholder.shapeAppearanceModel
                .toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, radius.toFloat())
                .setTopRightCorner(CornerFamily.ROUNDED, radius.toFloat())
                .setBottomLeftCorner(CornerFamily.ROUNDED, radius.toFloat())
                .setBottomRightCorner(CornerFamily.ROUNDED, radius.toFloat())
                .build()

            if (type.equals(EXTRA_DUET)) {
                Glide.with(this)
                    .load(path)
                    .into(videoPlaceholder);
            } else {
                Glide.with(this)
                    .load(absolute_path)
                    .into(videoPlaceholder);
            }

            tvHashTags.text = "" + hashTags
            llVideoUpload.visibility = View.VISIBLE
            isVideoPost = true

            showSnackbar("uploading..")
            hitApi()
        }
    }

    fun init() {
        instance = this

        setClickListeners()
        observerApi()
        requestPermissions("")

        val titles = ArrayList<String>()

        postsFragment = PostsFragment()
        fragments.add(postsFragment!!)
        titles.add("Feeds")
        searchFragment = SearchFragment()
        fragments.add(searchFragment!!)
        titles.add("Search")
        notificationFragment = NotificationFragment()
        fragments.add(notificationFragment!!)
        titles.add("Notifications")
        profileFragment = ProfileFragment()
        fragments.add(profileFragment!!)
        titles.add("Profile")

        vpDashboard.offscreenPageLimit = 4
        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, fragments, titles)
        vpDashboard.adapter = viewPagerAdapter

        if (classType == CLASS_TYPE_OTHER_PROFILE) {
            if (fragmentTag == TAB_HOME)
                tvHome.performClick()
            if (fragmentTag == TAB_SEARCH)
                tvSearch.performClick()
            if (fragmentTag == TAB_NOTIFICATIONS)
                tvNotification.performClick()
            if (fragmentTag == TAB_PROFILE)
                tvProfile.performClick()

            llDashboardTabs.visibility = View.GONE
            tvProfile.performClick()

        } else if (intent.hasExtra(EXTRA_NOTIFICATION)) {
            if (isLoggedIn())
                tvNotification.performClick()
            else
                showLoginPopup()
        } else {
            llDashboardTabs.visibility = View.VISIBLE
            tvHome.performClick()
        }
    }

    private fun observerApi() {
        commonViewModel.uploadPostResponseLiveData().observe(this, Observer<Event<ApiResponse>> {
            this.uploadData(it.getContentIfNotHandled(), 1)
        })
    }

    private fun uploadData(response: ApiResponse?, type: Int) {
        when (response!!.status) {
            Status.LOADING -> {
                if (type == 1)
                    flUploadProgress.visibility = View.VISIBLE
            }
            Status.SUCCESS -> {
                if (type == 1)
                    flUploadProgress.visibility = View.GONE
                renderResponse(response, type)
            }
            Status.ERROR -> {
                if (type == 1)
                    flUploadProgress.visibility = View.GONE
            }
        }
    }

    private fun setClickListeners() {
        tvHome.setOnClickListener {
            if (fragmentTag != TAB_HOME) {
                PostsFragment.isPostDisplaying = true
                fragmentTag = TAB_HOME
                pagerChangeByClick = true
                showHome()
            }
        }
        tvSearch.setOnClickListener {
            if (fragmentTag != TAB_SEARCH) {
                PostsFragment.isPostDisplaying = false
                fragmentTag = TAB_SEARCH
                pagerChangeByClick = true
                showSearch()
            }
        }
        ivCreateVideo.setOnClickListener {
            if (isLoggedIn()) {
                PostsFragment.isPostDisplaying = false
                postsFragment!!.pauseFeed()
                click = CLICKED_VIDEO
                requestPermissions(CLICKED_VIDEO)
            } else
                showLoginPopup()
        }
        tvNotification.setOnClickListener {
            if (isLoggedIn()) {
                if (fragmentTag != TAB_NOTIFICATIONS) {
                    PostsFragment.isPostDisplaying = false
                    fragmentTag = TAB_NOTIFICATIONS
                    pagerChangeByClick = true
                    showNotification()
                }
            } else
                showLoginPopup()
        }
        tvProfile.setOnClickListener {
            if (isLoggedIn()) {
                if (fragmentTag != TAB_PROFILE) {
                    PostsFragment.isPostDisplaying = false
                    fragmentTag = TAB_PROFILE
                    classType = TAB_PROFILE
                    pagerChangeByClick = true
                    showProfile()
                }
            } else
                showLoginPopup()
        }

        vpDashboard.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                if (position == 0)
                    postsFragment!!.playFeed()
                else
                    postsFragment!!.pauseFeed()

                when (position) {
                    0 -> tvHome.performClick()
                    1 -> tvSearch.performClick()
                    2 -> {
                        if (!isLoggedIn()) {
                            showLoginPopup()
                            vpDashboard.setCurrentItem(1, false)
                        } else
                            tvNotification.performClick()
                    }
                    3 -> {
                        if (!isLoggedIn()) {
                            showLoginPopup()
                            vpDashboard.setCurrentItem(1, false)
                        } else
                            tvProfile.performClick()
                    }
                }
            }
        })
    }

    private fun showHome() {
        vpDashboard.setCurrentItem(0, true)
        postsFragment!!.onTabSwitched()
        postsFragment!!.onVisibilityChanged(true)

        toggleSelection(tvHome, R.drawable.home, R.color.color_red)
        toggleSelection(tvSearch, R.drawable.search_white, R.color.colorWhite)
        toggleSelection(tvNotification, R.drawable.notification, R.color.colorWhite)
        toggleSelection(tvProfile, R.drawable.profile_white, R.color.colorWhite)
    }

    private fun showSearch() {
        vpDashboard.setCurrentItem(1, true)
        searchFragment!!.onTabSwitched()
        postsFragment!!.onVisibilityChanged(false)

        toggleSelection(tvHome, R.drawable.home_white, R.color.colorWhite)
        toggleSelection(tvSearch, R.drawable.search_red, R.color.color_red)
        toggleSelection(tvNotification, R.drawable.notification, R.color.colorWhite)
        toggleSelection(tvProfile, R.drawable.profile_white, R.color.colorWhite)
    }

    private fun showNotification() {
        vpDashboard.setCurrentItem(2, true)
        notificationFragment!!.onTabSwitched()
        postsFragment!!.onVisibilityChanged(false)

        toggleSelection(tvHome, R.drawable.home_white, R.color.colorWhite)
        toggleSelection(tvSearch, R.drawable.search_white, R.color.colorWhite)
        toggleSelection(tvNotification, R.drawable.bell_orange, R.color.color_red)
        toggleSelection(tvProfile, R.drawable.profile_white, R.color.colorWhite)
    }

    private fun showProfile() {
        vpDashboard.setCurrentItem(3, true)
        profileFragment!!.onTabSwitched()
        postsFragment!!.onVisibilityChanged(false)

        toggleSelection(tvHome, R.drawable.home_white, R.color.colorWhite)
        toggleSelection(tvSearch, R.drawable.search_white, R.color.colorWhite)
        toggleSelection(tvNotification, R.drawable.notification, R.color.colorWhite)
        toggleSelection(tvProfile, R.drawable.profile_red, R.color.color_red)
    }

    private fun toggleSelection(textView: TextView, resDrawableId: Int, resColorId: Int) {
        textView.setCompoundDrawablesWithIntrinsicBounds(0, resDrawableId, 0, 0);
        textView.setTextColor(resources.getColor(resColorId))
    }

    fun switchToHomeTab() {
        llDashboardTabs.visibility = View.VISIBLE
        tvHome.performClick()
    }

    override fun onStart() {
        super.onStart()

        if (fragmentTag == TAB_HOME)
            postsFragment!!.playFeed()
        else
            postsFragment!!.pauseFeed()
    }

    override fun onResume() {
        super.onResume()

        ConnectionStateMonitor.connectivityChangedListener = this
        ConnectionStateMonitor.checkCapabilities(context!!)
    }

    override fun onStop() {
        super.onStop()

        postsFragment!!.pauseFeed()
        if (isFinishing) {
            postsFragment!!.destroyMyPlayer()
        }

        ConnectionStateMonitor.connectivityChangedListener = null
    }

    private fun hitApi() {
        val token = "SEC " + VrockkApplication.user_obj!!.authToken
        val requestFile = RequestBody.create(
            MediaType.parse("video/mp4"),
            File(absolute_path)
        )
        val text = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            description_text
        )
        val songIdd = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            song_id
        )
        val lat = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"), latitude
        )
        val lng = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"), longitude
        )
        val hash = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            hashTags
        )
        val uploaded = RequestBody.create(MediaType.parse("application/json"), uploaded)

        if (type.equals("duet")) {
            val fileBody = ProgressRequestBody(File(path), "video/mp4", this)
            val videoPath = "video\"; filename=\"$path"
            val requestFile = RequestBody.create(MediaType.parse("video/mp4"), File(path))
            hashMap[videoPath] = fileBody
        } else {
            val fileBody = ProgressRequestBody(File(absolute_path), "video/mp4", this)
            val videoPath = "video\"; filename=\"stitchlrvideo$path"
            hashMap[videoPath] = fileBody
        }

        hashMap["description"] = text
        hashMap["hashtags"] = hash
        hashMap["songId"] = songIdd
        hashMap["isUploaded"] = uploaded
        hashMap["latitude"] = lat
        hashMap["longitude"] = lng

        commonViewModel.uploadPost(token, hashMap)
    }

    private fun renderResponse(response: ApiResponse, type: Int) {
        val data: String = Utils.toJson(response.data)

        if (type == 1) {
            val uploadPostResponse = Gson().fromJson(data, UploadPostResponse::class.java)
            if (uploadPostResponse.success!!) {
                showSnackbar("Video uploaded successfully")

                llVideoUpload.visibility = View.GONE
                isVideoPost = false

                if (VrockkApplication.user_obj != null) {
                    if (fragmentTag == TAB_PROFILE) {
                        refreshProfile = true
                        tvProfile.performClick()
                    }
                }
            } else {
                llVideoUpload.visibility = View.GONE
                showSnackbar("" + uploadPostResponse.message)
            }
        }
    }

    override fun onProgressUpdate(percentage: Int) {
        if (isVideoPost) {
            circularProgressbar.setProgress(percentage)
            tvPercentage.text = "$percentage%"
        }
    }

    override fun onFinish() {
        Log.i(TAG, "uploadPost.onFinish: ")
    }

    override fun onError() {
        Log.e(TAG, "uploadPost.onError: ")
    }

    override fun onBackPressed() {
        if (classType == CLASS_TYPE_OTHER_PROFILE) {
//            llDashboardTabs.visibility = View.VISIBLE

            if (fragmentTag == TAB_HOME)
                tvHome.performClick()
            if (fragmentTag == TAB_SEARCH)
                tvSearch.performClick()
            if (fragmentTag == TAB_NOTIFICATIONS)
                tvNotification.performClick()
            if (fragmentTag == TAB_PROFILE)
                tvProfile.performClick()

        } else if (fragmentTag != TAB_HOME) {
            tvHome.performClick()

        } else {
            val myCustomDlg = Dialog(this!!)
            myCustomDlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
            myCustomDlg.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
            myCustomDlg.setContentView(R.layout.my_custom_alert)
            myCustomDlg.tvTitleCustom.text = resources.getString(R.string.confirm)
            myCustomDlg.tvAlertMessage.text =
                resources.getString(R.string.are_you_sure_you_want_to_exit)
            myCustomDlg.positiveBtn.text = resources.getString(R.string.yes)
            myCustomDlg.noBtn.text = resources.getString(R.string.no)
            myCustomDlg.show()
            myCustomDlg.noBtn.setOnClickListener {
                myCustomDlg.dismiss()
            }
            myCustomDlg.positiveBtn.setOnClickListener {
                myCustomDlg.dismiss()
                finishAffinity()
            }
        }
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (isConnected) {
            internetInfoTv.alpha = 0f
        } else {
            internetInfoTv.alpha = 1f
        }
    }
}
