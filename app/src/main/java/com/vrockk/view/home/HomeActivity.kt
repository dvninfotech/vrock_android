package com.vrockk.view.home

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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
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
import com.vrockk.api.ApiResponse
import com.vrockk.api.Event
import com.vrockk.api.Status
import com.vrockk.base.BaseActivity
import com.vrockk.common.onfailure
import com.vrockk.models.upload_post.UploadPostResponse
import com.vrockk.utils.ProgressRequestBody
import com.vrockk.utils.Utils
import com.vrockk.view.cameraactivity.RecordVideosActivity
import com.vrockk.view.dashboard.NotificationFragment
import com.vrockk.view.dashboard.ProfileFragment
import com.vrockk.view.dashboard.SearchFragment
import com.vrockk.viewmodels.viewmodels.CommonViewModel
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.my_custom_alert.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.File

class HomeActivity : BaseActivity(), ProgressRequestBody.UploadCallbacks {
    val REQUEST_APP_UPDATE_FLEXIBLE = 0x2345
    val REQUEST_APP_UPDATE_IMMIDIATE = 0x2347

    var isVideoPost: Boolean = false

    //**   video post
    val hashMap: HashMap<String, RequestBody> = HashMap()
    private val commonViewModel by viewModel<CommonViewModel>()
    var absolute_path: String = ""
    var description_text: String = ""
    var song_id: String = ""
    var latitude: String = ""
    var longitude: String = ""
    var hashtaglist: String = ""
    var uploaded: String = ""
    var path: String = ""
    var type: String = ""
    var click = ""

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
//            checkForUpdates()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkForUpdates()

        setContentView(R.layout.activity_home)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN
        )
        init()

        //***  set tab Button visibility
        clHome.isEnabled = true
        clSearch.isEnabled = true
        createVideoView.isEnabled = true
        clNotification.isEnabled = true
        clProfile.isEnabled = true

        if (intent.hasExtra("upload")) {
            absolute_path = intent.getStringExtra("absolutePath")
            description_text = intent.getStringExtra("description_text")
            song_id = intent.getStringExtra("song_id")
            latitude = intent.getStringExtra("latitude")
            longitude = intent.getStringExtra("longitude")
            hashtaglist = intent.getStringExtra("hashtaglist")
            uploaded = intent.getStringExtra("uploaded")
            path = intent.getStringExtra("path")
            type = intent.getStringExtra("type")

            Log.e("call", "3333 absolute path: " + absolute_path)
            Log.e("call", "3333 description_text: " + description_text)
            Log.e("call", "3333 song_id: " + song_id)
            Log.e("call", "3333  latitude: " + latitude)
            Log.e("call", "3333 longitude: " + longitude)
            Log.e("call", "3333 hashtaglist: " + hashtaglist)
            Log.e("call", "3333 uploaded: " + uploaded)
            Log.e("call", "3333 path: " + path)
            Log.e("call", "3333 type: " + type)

            val radius = resources.getDimensionPixelSize(R.dimen._2sdp)
            videoPlaceholder.shapeAppearanceModel = videoPlaceholder.shapeAppearanceModel
                .toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, radius.toFloat())
                .setTopRightCorner(CornerFamily.ROUNDED, radius.toFloat())
                .setBottomLeftCorner(CornerFamily.ROUNDED, radius.toFloat())
                .setBottomRightCorner(CornerFamily.ROUNDED, radius.toFloat())
                .build()

            if (type.equals("duet")) {
                Glide.with(this)
                    .load(path)
                    .into(videoPlaceholder);
            } else {
                Glide.with(this)
                    .load(absolute_path)
                    .into(videoPlaceholder);
            }

            tvHashTag_.setText("" + hashtaglist)
            clUploadingBarr.visibility = View.VISIBLE
            isVideoPost = true

            showSnackbar("uploading..")

            hitApi()
        }
    }

    private fun observerApi() {
        commonViewModel.uploadPostResponseLiveData().observe(this, Observer<Event<ApiResponse>> { response ->
            this.uploadData(response.getContentIfNotHandled(), 1)
        })
    }

    private fun uploadData(response: ApiResponse?, type: Int) {
        when (response!!.status) {
            Status.LOADING -> {
                if (type == 1)
                    clUploadingBarr.visibility = View.VISIBLE

            }
            Status.SUCCESS -> {
                if (type == 1)
                    clUploadingBarr.visibility = View.GONE

                renderResponse(response, type)

            }
            Status.ERROR -> {
                if (type == 1)
                    clUploadingBarr.visibility = View.GONE
                Log.e("response", Gson().toJson(response.error))
                //showCustomToastMessage(this, "Something went wrong!")
            }
        }
    }

    private fun renderResponse(response: ApiResponse, type: Int) {
        if (response != null) {
            Log.e("Upload_Response: ", Gson().toJson(response))
            val data: String = Utils.toJson(response.data)
            val gson1 = Gson()
            if (type == 1) {
                val uploadPostResponse = gson1.fromJson(data, UploadPostResponse::class.java)
                if (uploadPostResponse.success!!) {
                    showSnackbar("Video uploaded successfully")

                    clUploadingBarr.visibility = View.GONE
                    isVideoPost = false

                    if (VrockkApplication.user_obj != null) {
                        if (Tag.equals("profile")) {
                            var profileFragment: ProfileFragment = ProfileFragment()
                            loadFrag(profileFragment)
                        }
                    }
                } else {
                    clUploadingBarr.visibility = View.GONE
                    showSnackbar("" + uploadPostResponse.message)
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()

        //***  set tab Button visibility
        clHome.isEnabled = true
        clSearch.isEnabled = true
        createVideoView.isEnabled = true
        clNotification.isEnabled = true
        clProfile.isEnabled = true

    }


    companion object {
        var Tag: String = "home"
        var instance: HomeActivity? = null
        var classType: String = ""
    }

    fun init() {

        observerApi()

        setupPermissions("")

        instance = this

        onClick()

        if (classType == "other_profile") {
            if (Tag == "home") {
                visibleHome()
            }
            if (Tag == "search") {
                visibleSearch()
            }
            if (Tag == "challenge") {
                visibleNotification()
            }
            if (Tag == "profile") {
                visibleProfile()
            }
            clHeader.visibility = View.GONE
            loadFrag(ProfileFragment())
        } else if (intent.hasExtra("notification")) {
            if (VrockkApplication.user_obj != null) {
                Tag = "challenge"
                loadFrag(NotificationFragment())
                visibleNotification()
            } else {
                showLoginPopup()
            }
        } else {
            clHeader.visibility = View.VISIBLE
            loadFrag(HomeFragment())
        }
    }

    public fun loadFrag(fragment: Fragment) {

        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        transaction.replace(R.id.homeFrameLayout, fragment)
        //transaction.commit()
        transaction.commitAllowingStateLoss()
    }

    public fun loadFrag2(fragment: Fragment) {
        clHeader.visibility = View.VISIBLE
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        transaction.replace(R.id.homeFrameLayout, fragment)
        //transaction.commit()
        transaction.commitAllowingStateLoss()
    }

    fun statusBarTransparent() {
        statusBarColor()
        transparentStatusBar()
    }

    private fun onClick() {
        clHome.setOnClickListener {

            Tag = "home"

            loadFrag(HomeFragment())

            visibleHome()


            if (VrockkApplication.user_obj != null) {
                clHome.isEnabled = false
                clSearch.isEnabled = true
                createVideoView.isEnabled = true
                clNotification.isEnabled = true
                clProfile.isEnabled = true
            }

        }

        clSearch.setOnClickListener {

            Tag = "search"

            loadFrag(SearchFragment())

            visibleSearch()

            if (VrockkApplication.user_obj != null) {
                clHome.isEnabled = true
                clSearch.isEnabled = false
                createVideoView.isEnabled = true
                clNotification.isEnabled = true
                clProfile.isEnabled = true
            }


        }

        createVideoView.setOnClickListener {

            if (VrockkApplication.user_obj != null) {
                click = "video"
                setupPermissions("video")
                //   navigate(RecordVideosActivity::class.java)
            } else {
                showLoginPopup()
            }

            if (VrockkApplication.user_obj != null) {
                clHome.isEnabled = true
                clSearch.isEnabled = true
                createVideoView.isEnabled = false
                clNotification.isEnabled = true
                clProfile.isEnabled = true
            }


        }

        clNotification.setOnClickListener {

            if (VrockkApplication.user_obj != null) {
                Tag = "challenge"
                loadFrag(NotificationFragment())
                visibleNotification()
            } else {
                showLoginPopup()
            }

            if (VrockkApplication.user_obj != null) {
                clHome.isEnabled = true
                clSearch.isEnabled = true
                createVideoView.isEnabled = true
                clNotification.isEnabled = false
                clProfile.isEnabled = true
            }


        }

        clProfile.setOnClickListener {
            if (VrockkApplication.user_obj != null) {
                Tag = "profile"
                classType = "profile"
                var profileFragment: ProfileFragment = ProfileFragment()
                loadFrag(profileFragment)
                visibleProfile()
            } else {
                showLoginPopup()
            }

            if (VrockkApplication.user_obj != null) {
                clHome.isEnabled = true
                clSearch.isEnabled = true
                createVideoView.isEnabled = true
                clNotification.isEnabled = true
                clProfile.isEnabled = false
            }


        }
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
        ) + ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) + ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WAKE_LOCK
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@HomeActivity,
                    Manifest.permission.CAMERA
                )
                && ActivityCompat.shouldShowRequestPermissionRationale(
                    this@HomeActivity,
                    Manifest.permission.RECORD_AUDIO
                ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this@HomeActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this@HomeActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this@HomeActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                && ActivityCompat.shouldShowRequestPermissionRationale(
                    this@HomeActivity,
                    Manifest.permission.WAKE_LOCK
                )
            ) {
                onfailure(
                    this,
                    getString(R.string.pleasegiveallrequiredpermissions),
                    getString(R.string.gotosettings),
                    this@HomeActivity
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WAKE_LOCK
                    ),
                    1
                )
            }
        } else {
            if (s == "video")
                navigate(RecordVideosActivity::class.java)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        if (requestCode == 1) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (click == "video")
                    navigate(RecordVideosActivity::class.java)
            } else {

            }
        }
    }

    fun replaceProfileFragment() {
        classType = "search"
        loadFrag(ProfileFragment())
    }

    fun replaceSearchFragment() {
        clHeader.visibility = View.VISIBLE
        loadFrag(HomeFragment())
    }

    fun replaceSearchFragment2() {
        clHeader.visibility = View.VISIBLE
        loadFrag(SearchFragment())
    }


    override fun onBackPressed() {

        if (classType.equals("other_profile")) {
            clHeader.visibility = View.VISIBLE

            if (Tag.equals("home")) {
                loadFrag(HomeFragment())
            } else if (Tag.equals("search")) {
                loadFrag(SearchFragment())
            } else if (Tag.equals("challenge")) {
                loadFrag(NotificationFragment())
            } else {
                loadFrag(ProfileFragment())
            }

        } else {
            if (Tag.equals("search")) {
                showHomeTab()
            } else if (Tag.equals("challenge")) {
                showHomeTab()
            } else if (Tag.equals("profile")) {
                showHomeTab()
            } else {

                val myCustomDlg = Dialog(this!!)
                myCustomDlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
                myCustomDlg.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
                myCustomDlg.setContentView(R.layout.my_custom_alert)
                myCustomDlg.tvTitleCustom.text = resources.getString(R.string.confirm)
                myCustomDlg.tvAlertMessage.text = resources.getString(R.string.are_you_sure_you_want_to_exit)
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

//                AlertDialog.Builder(this).setTitle(resources.getString(R.string.confirm))
//                    .setMessage(getString(R.string.are_you_sure_you_want_to_exit))
//                    .setPositiveButton(
//                        getString(R.string.ok)
//                    ) { p0, p1 -> finishAffinity() }
//                    .setNeutralButton(getString(R.string.close), null)
//                    .show()
            }

        }
    }


    fun visibleHome() {
        tvHome.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.home, 0, 0);
        tvHome.setTextColor(resources.getColor(R.color.color_red))
        tvSearch.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.search_white, 0, 0)
        tvSearch.setTextColor(resources.getColor(R.color.colorWhite))
        tvNotification.setCompoundDrawablesWithIntrinsicBounds(
            0,
            R.drawable.notification,
            0,
            0
        );
        tvNotification.setTextColor(resources.getColor(R.color.colorWhite))
        tvProfile.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.profile_white, 0, 0)
        tvProfile.setTextColor(resources.getColor(R.color.colorWhite))
    }

    fun visibleSearch() {
        tvHome.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.home_white, 0, 0)
        tvHome.setTextColor(resources.getColor(R.color.colorWhite))
        tvSearch.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.search_red, 0, 0)
        tvSearch.setTextColor(resources.getColor(R.color.color_red))
        tvNotification.setCompoundDrawablesWithIntrinsicBounds(
            0,
            R.drawable.notification,
            0,
            0
        )
        tvNotification.setTextColor(resources.getColor(R.color.colorWhite))
        tvProfile.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.profile_white, 0, 0)
        tvProfile.setTextColor(resources.getColor(R.color.colorWhite))
    }

    fun visibleNotification() {
        tvHome.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.home_white, 0, 0)
        tvHome.setTextColor(resources.getColor(R.color.colorWhite))
        tvSearch.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.search_white, 0, 0)
        tvSearch.setTextColor(resources.getColor(R.color.colorWhite))
        tvNotification.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.bell_orange, 0, 0)
        tvNotification.setTextColor(resources.getColor(R.color.color_red))
        tvProfile.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.profile_white, 0, 0)
        tvProfile.setTextColor(resources.getColor(R.color.colorWhite))
    }

    fun visibleProfile() {
        tvHome.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.home_white, 0, 0)
        tvHome.setTextColor(resources.getColor(R.color.colorWhite))
        tvSearch.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.search_white, 0, 0)
        tvSearch.setTextColor(resources.getColor(R.color.colorWhite))
        tvNotification.setCompoundDrawablesWithIntrinsicBounds(
            0,
            R.drawable.notification,
            0,
            0
        );
        tvNotification.setTextColor(resources.getColor(R.color.colorWhite))
        tvProfile.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.profile_red, 0, 0);
        tvProfile.setTextColor(resources.getColor(R.color.color_red))
    }


    fun showHomeTab() {
        Tag = "home"
        classType = "home"

        loadFrag(HomeFragment())

        visibleHome()

        if (VrockkApplication.user_obj != null) {
            clHome.isEnabled = false
            clSearch.isEnabled = true
            clCreateVideo.isEnabled = true
            clNotification.isEnabled = true
            clProfile.isEnabled = true
        }
    }

    ////****  hit upload api

    private fun hitApi() {

        val token = "SEC " + VrockkApplication.user_obj!!.authToken
        val requestFile = RequestBody.create(MediaType.parse("video/mp4"), File(absolute_path))
        val text =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), description_text)
        val songIdd =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), song_id)
        val lat = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            latitude.toString()
        )
        val lng = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            longitude.toString()
        )
        val hash =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), hashtaglist)
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

    override fun onFinish() {

    }

    override fun onProgressUpdate(percentage: Int) {
        Log.e(TAG, percentage.toString())

        if (isVideoPost) {
            circularProgressbar.setProgress(percentage)
            tvPercentage.setText("" + percentage.toString() + "%")
        }

    }

    override fun onError() {
        Log.e("call", "44444")
    }

}
