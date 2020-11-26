package com.vrockk.view.splash

import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.base.BaseActivity
import com.vrockk.view.dashboard.DashboardActivity
import com.vrockk.view.posts_play.PostsPlayAcivity
import com.vrockk.viewmodels.FeedsViewModel
import com.vrockk.viewmodels.viewmodels.HomePageViewModel
import kotlinx.android.synthetic.main.activity_splash_screen.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class SplashScreen : BaseActivity(), MediaPlayer.OnCompletionListener {

    private val feedsViewModel by viewModel<FeedsViewModel>()
    private val homePageViewModel by viewModel<HomePageViewModel>()
    var fcmToken = ""

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash_screen)

        val uri: Uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.qqqq)
        videoview.setVideoURI(uri)
        videoview.setOnCompletionListener(this)
        videoview.start()

        fcmToken()
        getLink()

        //######################To Get the KeyHash for Facebook#####################################
        try {
            val info = packageManager.getPackageInfo(
                "com.vrockk",
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
            }
        } catch (e: PackageManager.NameNotFoundException) {

        } catch (e: NoSuchAlgorithmException) {

        }
    }

    override fun onPause() {
        super.onPause()

        videoview.pause()
    }

    override fun onResume() {
        super.onResume()

        videoview.start()
    }

    private fun getLink() {
        VrockkApplication.uri_data_deep_linkind = intent.data
    }

    private fun fcmToken() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                fcmToken = task.result?.token!!
                VrockkApplication.fcmToken = task.result?.token!!
            })
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if (VrockkApplication.uri_data_deep_linkind != null) {
            var postId: String? = VrockkApplication.uri_data_deep_linkind!!.getQueryParameter("url")
            val intent = Intent(this, PostsPlayAcivity::class.java);
            postId = postId!!.replace("vrockk://&postId=", "")
            intent.putExtra("postId", "" + postId)
            intent.putExtra("notification", "")
            startActivity(intent)

            VrockkApplication.uri_data_deep_linkind = null
            finish()
        } else {
            navigateWithFinish(DashboardActivity::class.java)
        }
    }
}

