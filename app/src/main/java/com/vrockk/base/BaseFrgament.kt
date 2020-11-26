package com.vrockk.base

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.common.onfailure
import com.vrockk.services.LocationService

open class BaseFragment: Fragment() {
    val handler = Handler()
    lateinit var progressBar: /*CustomProgressDialog*/ContentLoadingProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        progressBar = /*CustomProgressDialog*/ContentLoadingProgressBar(activity!!)
//        progressBar.isIndeterminate = true
//        progressBar.setCancelable(true)
//        progressBar.setCanceledOnTouchOutside(true)
    }

    open fun onTabSwitched() {

    }

    fun showLoginPopup() {
        (activity as BaseActivity).showLoginPopup()
    }

    fun getMyLatitude(): Double {
        return if (LocationService.instance != null) LocationService.instance!!.getLatitude() else 0.0
    }

    fun getMyLongitude(): Double {
        return if (LocationService.instance != null) LocationService.instance!!.getLongitude() else 0.0
    }

    fun getMyAuthToken(): String {
        return if (isLoggedIn()) "SEC " + VrockkApplication.user_obj!!.authToken else ""
    }

    fun getMyUserId(): String {
        return if (isLoggedIn()) VrockkApplication.user_obj!!.id else ""
    }

    fun isLoggedIn() : Boolean {
        return VrockkApplication.user_obj != null
    }

    public fun initProgress(progressBar: ContentLoadingProgressBar) {
        this.progressBar = progressBar
    }

    public fun showProgress() {
        progressBar.show()
    }

    public fun showProgress(message: String) {
        progressBar.show()
    }

    public fun hideProgress() {
        try {
//            if (progressBar!!.isShowing) {
//                progressBar!!.dismiss()
//            }
//            if (progressBar.isShown)
                progressBar.hide()
        } catch (e: Exception) {
        }
    }

    fun showSnackbar(message: String) {
        if (message != null && !message.isEmpty()) {
            Snackbar.make(activity!!.findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
        }
    }

    fun toast(message: String) {
        Toast.makeText(context, "" + message, Toast.LENGTH_SHORT).show()
    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(
            activity!!,
            Manifest.permission.RECORD_AUDIO
        ) + ContextCompat.checkSelfPermission(
            activity!!,
            Manifest.permission.CAMERA
        ) + ContextCompat.checkSelfPermission(
            activity!!,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) + ContextCompat.checkSelfPermission(
            activity!!,
            Manifest.permission.WRITE_EXTERNAL_STORAGE + Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity!!,
                    Manifest.permission.CAMERA
                )
                && ActivityCompat.shouldShowRequestPermissionRationale(
                    activity!!,
                    Manifest.permission.RECORD_AUDIO
                ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    activity!!,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    activity!!,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    activity!!,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                onfailure(
                    activity!!,
                    getString(R.string.pleasegiveallrequiredpermissions),
                    getString(R.string.gotosettings),
                    activity!!
                )
            } else {
                ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ), 1
                )
            }
        }
    }
}