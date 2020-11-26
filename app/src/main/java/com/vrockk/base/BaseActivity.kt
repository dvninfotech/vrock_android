package com.vrockk.base

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.media.ExifInterface
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.ContentLoadingProgressBar
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import com.facebook.login.LoginManager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.vrockk.BuildConfig
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.common.PermissionsCodes
import com.vrockk.models.LogoutResponse
import com.vrockk.services.LocationService
import com.vrockk.socket.AppSocketListener
import com.vrockk.socket.SocketListener
import com.vrockk.utils.ConnectionStateMonitor
import com.vrockk.utils.FileUtilsV4
import com.vrockk.utils.PreferenceHelper
import com.vrockk.view.home.HomeFragment
import com.vrockk.view.login.LoginActivity
import com.vrockk.view.posts_play.Posts2PlayAcivity
import com.vrockk.view.posts_play.PostsPlayAcivity
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.my_custom_alert.*
import java.io.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


open class BaseActivity : AppCompatActivity(), SocketListener {
    val TAG = "BaseActivity"

    val handler: Handler = Handler()
    lateinit var progressBar: /*CustomProgressDialog*/ContentLoadingProgressBar

    lateinit var locationServiceIntent: Intent

    internal lateinit var tvchoosemediaCamera: TextView
    internal lateinit var ivchoosemediaCamera: ImageView
    internal lateinit var tvchoosemediaGallery: TextView
    internal lateinit var ivchoosemediaGallery: ImageView
    internal lateinit var tvchoosemediaCancel: TextView

    lateinit var imageUri: Uri
    lateinit var imagePathForCamera: String
    private val IMAGE_DIRECTORY = "/stitchlrappappapp"

    protected lateinit var outputDirectory: File // The Folder where all the files will be stored
    var connectionMonitor: ConnectionStateMonitor? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (VrockkApplication.user_obj != null) {
            AppSocketListener.getInstance().setActiveSocketListener(this)
        }

        outputDirectory = File(
            getExternalFilesDir(Environment.DIRECTORY_DCIM)?.absolutePath
                ?: externalMediaDirs.first().absolutePath
        )

        progressBar = /*CustomProgressDialog*/ContentLoadingProgressBar(this@BaseActivity)
//        progressBar.setCancelable(false)
//        progressBar.setCanceledOnTouchOutside(false)

        locationServiceIntent = Intent(this, LocationService.javaClass)

        if (connectionMonitor == null)
            connectionMonitor = ConnectionStateMonitor(context = applicationContext)

    }

    fun getMyLatitude(): Double {
        return if (LocationService.instance != null) LocationService.instance!!.getLatitude() else 0.0
    }

    fun getMyLongitude(): Double {
        return if (LocationService.instance != null) LocationService.instance!!.getLongitude() else 0.0
    }

    fun getMyAuthToken(): String {
        return "SEC " + VrockkApplication.user_obj!!.authToken
    }

    fun getMyUserId(): String {
        return VrockkApplication.user_obj!!.id
    }

    override fun onStart() {
        super.onStart()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(locationServiceIntent)
        else
            startService(locationServiceIntent)
//        LocationService.instance.startLocationUpdates()
        connectionMonitor!!.enable()
    }

    override fun onStop() {
        super.onStop()

        if (LocationService.instance != null) {
            LocationService.instance!!.stopLocationUpdates()
            stopService(locationServiceIntent)
        }

        connectionMonitor!!.disable()
    }

    fun isLoggedIn(): Boolean {
        return VrockkApplication.user_obj != null
    }

    fun isSocialLogIn(): Boolean {
        return isLoggedIn() &&
                (VrockkApplication.user_obj!!.provider.toLowerCase(Locale.ROOT) == "google"
                        || VrockkApplication.user_obj!!.provider.toLowerCase(Locale.ROOT) == "facebook")
    }

    fun isValidEmail(target: CharSequence): Boolean {
        return (target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target)
            .matches())
    }

    public fun initProgress(progressBar: ContentLoadingProgressBar) {
        this.progressBar = progressBar
    }

    fun showProgress() {
        if (!progressBar.isShown)
            progressBar.show()
    }

    fun showProgress(message: String) {
        if (!progressBar.isShown)
            progressBar.show()
    }

    public fun hideProgress() {
        try {
//            if (progressBar.isShowing) {
//                progressBar.dismiss()
//            }
            if (progressBar.isShown)
                progressBar.hide()
        } catch (e: Exception) {
        }
    }


    fun showSnackbar(message: String) {
        if (message != null && !message.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
        }
    }

    public fun transparentStatusBar() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        window.statusBarColor = Color.TRANSPARENT;
    }

    public fun statusBarColor() {
        val window = window// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)// finally change the color
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    public fun hideStatusBar() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
    }


    fun navigate(destination: Class<*>) {
        startActivity(Intent(this@BaseActivity, destination))
    }

    fun navigateWithFinish(destination: Class<*>) {
        startActivity(Intent(this@BaseActivity, destination))
        finish()
    }

    fun navigateFinishAffinity(destination: Class<*>) {
        startActivity(Intent(this@BaseActivity, destination))
        finishAffinity()
    }

    fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    fun showPictureDialog() {

        val mDialogView = LayoutInflater.from(this).inflate(R.layout.choosemedia_layout, null)

        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
        val mAlertDialog = mBuilder.show()
        mAlertDialog.setCanceledOnTouchOutside(true)
        mAlertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        tvchoosemediaCamera = mDialogView.findViewById<View>(R.id.tvchoosemediaCamera) as TextView
        ivchoosemediaCamera = mDialogView.findViewById<View>(R.id.ivchoosemediaCamera) as ImageView
        tvchoosemediaGallery = mDialogView.findViewById<View>(R.id.tvchoosemediaGallery) as TextView
        ivchoosemediaGallery =
            mDialogView.findViewById<View>(R.id.ivchoosemediaGallery) as ImageView
        tvchoosemediaCancel = mDialogView.findViewById<View>(R.id.tvchoosemediaCancel) as TextView

        tvchoosemediaCamera.setOnClickListener {
            openCamera()
            mAlertDialog.dismiss()
        }
        ivchoosemediaCamera.setOnClickListener {
            openCamera()
            mAlertDialog.dismiss()
        }

        tvchoosemediaGallery.setOnClickListener {
            openGallery()
            mAlertDialog.dismiss()
        }
        ivchoosemediaGallery.setOnClickListener {
            openGallery()
            mAlertDialog.dismiss()
        }

        tvchoosemediaCancel.setOnClickListener {
            mAlertDialog.dismiss()
        }
    }

    fun openGallery() {
        if (isGalleryPermissions()) {
            val pickPhoto = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            if (pickPhoto.resolveActivity(getPackageManager()) != null) {
                pickPhoto.type = "image/*"
                startActivityForResult(
                    pickPhoto,
                    PermissionsCodes.GALLERYREQUESTCODE
                )
            } else
                Toast.makeText(
                    this,
                    "No application found to perform the action.",
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

    //Open camera with check permission
    fun openCamera() {
        if (checkAndRequestPermissions2(PermissionsCodes.CAMERAPERMISSIONCODE)) {
            camera()
        }
    }

    //open camera if permision granted
    private fun camera() {
//        val values = ContentValues()
//        values.put(MediaStore.Images.Media.TITLE, "New Picture");
//        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
//
//        imageUri = getContentResolver().insert(
//            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
//        )!!

        var file: File? = null
        try {
            file = FileUtilsV4.createFile(
                "PROF_"
                        + System.currentTimeMillis(), ".jpg"
            )
            imagePathForCamera = file.absolutePath
            if (file != null) {
                imageUri = FileProvider.getUriForFile(
                    applicationContext,
                    BuildConfig.APPLICATION_ID + ".provider", file
                )

//        grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

                val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(takePicture, PermissionsCodes.CAMERAPERMISSIONCODE)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    public fun isGalleryPermissions(): Boolean {

        var isGranted: Boolean = false
        val permission = ContextCompat.checkSelfPermission(
            this@BaseActivity,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            //  Log.i(TAG, "Permission to get image from gallery denied")
            makeGalleryRequest()
            isGranted = false

        } else {
            isGranted = true
        }
        return isGranted
    }

    public fun makeGalleryRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            PermissionsCodes.READSTORAGEPERMISSIONCODE
        )
    }

    //Check multiple permissions
    fun checkAndRequestPermissions2(REQUEST_ID_MULTIPLE_PERMISSIONS: Int): Boolean {
        val camera = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
        val storage = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val read = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val listPermissionsNeeded = ArrayList<String>()
        if (camera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.CAMERA)
        }
        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (read != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (read != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toArray(arrayOfNulls<String>(listPermissionsNeeded.size)),
                REQUEST_ID_MULTIPLE_PERMISSIONS
            )
            return false
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getRotatedImage(bitmap: Bitmap, input: InputStream): Bitmap {
        var exif: ExifInterface? = null
        try {
            exif = ExifInterface(input!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.w("ProfileActivity", "Exception: $e")
        }
        val orientation = exif?.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )
        var rotatedBitmap: Bitmap?
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 ->
                rotatedBitmap = rotateImage(bitmap, 90F)
            ExifInterface.ORIENTATION_ROTATE_180 ->
                rotatedBitmap = rotateImage(bitmap, 180F)
            ExifInterface.ORIENTATION_ROTATE_270 ->
                rotatedBitmap = rotateImage(bitmap, 270F)
            else ->
                rotatedBitmap = bitmap
        }
        return rotatedBitmap!!
    }

    fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }

    fun saveImage(myBitmap: Bitmap): String {
        val bytes = ByteArrayOutputStream()
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val wallpaperDirectory = File(
            (Environment.getExternalStorageDirectory()).toString() + IMAGE_DIRECTORY
        )
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs()
        }
        try {
            val f = File(
                wallpaperDirectory, ((Calendar.getInstance()
                    .getTimeInMillis()).toString() + ".jpg")
            )
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(
                this,
                arrayOf(f.getPath()),
                arrayOf("image/jpeg"), null
            )
            fo.close()
            return f.getAbsolutePath()
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
        return ""
    }

    var formatter = DecimalFormat("00")
    fun calculateSecMinHour(milliSeconds: Long): String {
        var timeRemaining = milliSeconds / 1000
        val hour = timeRemaining / 3600
        timeRemaining %= 3600
        val minutes = timeRemaining / 60
        timeRemaining %= 60
        val seconds = timeRemaining

        return "${formatter.format(hour)}:${formatter.format(minutes)}:${formatter.format(seconds)}"
    }

    var mCurrSongCover: Bitmap? = null
    fun getMediaImageofSong(uri: String): Bitmap? {
        val mediaMetaDataRetriever = MediaMetadataRetriever()

        mediaMetaDataRetriever.setDataSource(uri, HashMap<String, String>())
        val data = mediaMetaDataRetriever.embeddedPicture


        if (data != null) {
            mCurrSongCover = BitmapFactory.decodeByteArray(data, 0, data.size)
            Log.e("mCurrSongCover ", " of Song $mCurrSongCover")
            mediaMetaDataRetriever.release()
        }

        return mCurrSongCover!!
    }

    //to get milliseconds from specicfic format date string
    public fun getMilliseconds(dateString: String): Long {
        val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        var date = originalFormat.parse(dateString);
        return date.time
    }

    fun showLoginPopup() {
        val myCustomDlg = Dialog(this!!)
        myCustomDlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        myCustomDlg.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        myCustomDlg.setContentView(R.layout.my_custom_alert)
        myCustomDlg.tvAlertMessage.text = resources.getString(R.string.need_to_login_to_access_this)
        myCustomDlg.tvTitleCustom.text = resources.getString(R.string.login)
        myCustomDlg.positiveBtn.text = resources.getString(R.string.login)
        myCustomDlg.noBtn.text = resources.getString(R.string.no)
        myCustomDlg.show()

        myCustomDlg.positiveBtn.setOnClickListener {
            myCustomDlg.dismiss()

            val editor = VrockkApplication.prefs.edit()

            val sharedPreference = PreferenceHelper.defaultPrefs(this)
            val deviceToken = sharedPreference.getString(PreferenceHelper.Key.FCMTOKEN, "")
            editor!!.remove(PreferenceHelper.Key.REGISTEREDUSER)
            editor.putString(PreferenceHelper.Key.FCMTOKEN, deviceToken)
            editor.apply()
            VrockkApplication.user_obj = null
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()

            if (HomeFragment.exoPlayer != null) {
                HomeFragment.exoPlayer!!.stop()
            }

            if (Posts2PlayAcivity.player != null) {
                Posts2PlayAcivity.player!!.stop()
            }

            if (PostsPlayAcivity.player != null) {
                PostsPlayAcivity.player!!.stop()
            }

            try {
                LoginManager.getInstance().logOut();
            } catch (e: Exception) {

            }
        }

        myCustomDlg.noBtn.setOnClickListener {
            myCustomDlg.dismiss()
        }

//        android.app.AlertDialog.Builder(this).setTitle(resources.getString(R.string.confirm))
//            .setMessage(getString(R.string.you_need_to_login_first))
//            .setPositiveButton(getString(R.string.login)
//            ) { p0, p1 ->
//                val editor = VrockkApplication.prefs.edit()
//
//                val sharedPreference = PreferenceHelper.defaultPrefs(this)
//                val deviceToken = sharedPreference.getString(PreferenceHelper.Key.FCMTOKEN, "")
//                editor!!.remove(PreferenceHelper.Key.REGISTEREDUSER)
//                editor.putString(PreferenceHelper.Key.FCMTOKEN, deviceToken)
//                editor.apply()
//                VrockkApplication.user_obj = null
//                startActivity(Intent(this, LoginActivity::class.java))
//                finishAffinity()
//
//                if(HomeFragment.player != null ){
//                    HomeFragment.player!!.stop()
//                }
//
//                if(Posts2PlayAcivity.player != null ){
//                    Posts2PlayAcivity.player!!.stop()
//                }
//
//                if(PostsPlayAcivity.player != null ){
//                    PostsPlayAcivity.player!!.stop()
//                }
//
//                try {
//                    LoginManager.getInstance().logOut();
//                }catch (e:Exception) {
//
//                }
//            }
//            .setNeutralButton(getString(R.string.no), null)
//            .show()
    }

    fun loadThumbnail(imageUrl: String, imageView: ImageView) {
        Picasso.get()
            .load(imageUrl)
            .placeholder(R.drawable.user_placeholder)
            .error(R.drawable.user_placeholder)
            .into(imageView)
    }

    fun loadThumbnailWitLoader(imageUrl: String, imageView: ImageView) {
        Picasso.get()
            .load(imageUrl)
            .placeholder(getImageLoader())
            .error(R.drawable.user_placeholder)
            .into(imageView)
    }

    fun getImageRequest(imageUrl: String): RequestBuilder<Any> {
        // Create glide request manager
        val requestManager = Glide.with(this)
        val requestOptions = RequestOptions()
        requestOptions.placeholder(getImageLoader())
        requestOptions.error(R.drawable.user_placeholder)
        requestOptions.fallback(R.drawable.user_placeholder)
        // Create request builder and load image.
        val requestBuilder = requestManager
            .load(imageUrl).thumbnail(0.1f).apply(requestOptions) as RequestBuilder<Any>
        return requestBuilder
    }

    fun getImageLoader(): CircularProgressDrawable {
        val circularProgressDrawable = CircularProgressDrawable(this)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()
        return circularProgressDrawable
    }

    ////**** Socket Connection..
    override fun onSocketConnected() {
        if (VrockkApplication.user_obj != null) {
            AppSocketListener.getInstance()
                .off(VrockkApplication.user_obj!!._id + "-" + "logout", onGetMessages)
            AppSocketListener.getInstance()
                .addOnHandler(VrockkApplication.user_obj!!._id + "-" + "logout", onGetMessages)
        }
    }

    override fun onSocketDisconnected() {
        Log.e("call", "SOCKET DISCONNECTED")
    }

    override fun onSocketConnectionError() {
        Log.e("call", "SOCKET CONNECTION ERROR")
    }

    override fun onSocketConnectionTimeOut() {
        Log.e("call", "SOCKET CONNECTION TIMEOUT")
    }

    ////**** Listener==> get Draw Details...
    private val onGetMessages = Emitter.Listener { args ->

        val gson1 = Gson()
        val logoutResponse =
            gson1.fromJson<LogoutResponse>(args[0].toString(), LogoutResponse::class.java!!)
        Log.e("call ", "SOCKET onGetMessages=>>>>    " + logoutResponse.toString())

        runOnUiThread {
            if (VrockkApplication.user_obj != null) {
                showDisablePopup()
            }
        }
    }


    fun showDisablePopup() {

        val myCustomDlg = Dialog(this)
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

            val sharedPreference = PreferenceHelper.defaultPrefs(this@BaseActivity)
            val deviceToken = sharedPreference.getString(PreferenceHelper.Key.FCMTOKEN, "")
            editor!!.remove(PreferenceHelper.Key.REGISTEREDUSER)
            editor.putString(PreferenceHelper.Key.FCMTOKEN, deviceToken)
            editor.commit()
            VrockkApplication.user_obj = null

            startActivity(Intent(this@BaseActivity, LoginActivity::class.java))
            finishAffinity()

            try {
                LoginManager.getInstance().logOut();
            } catch (e: java.lang.Exception) {

            }

            try {
                LoginManager.getInstance().logOut();
            } catch (e: Exception) {

            }
        }

//        AlertDialog.Builder(this).setTitle(resources.getString(R.string.alert))
//            .setMessage(getString(R.string.your_account_is_disabled_by_admin))
//            .setCancelable(false)
//            .setPositiveButton(getString(R.string.ok)
//            ) { p0, p1 ->
//                //  call
//                val editor = VrockkApplication.prefs.edit()
//
//                val sharedPreference = PreferenceHelper.defaultPrefs(this@BaseActivity)
//                val deviceToken = sharedPreference.getString(PreferenceHelper.Key.FCMTOKEN, "")
//                editor!!.remove(PreferenceHelper.Key.REGISTEREDUSER)
//                editor.putString(PreferenceHelper.Key.FCMTOKEN, deviceToken)
//                editor.commit()
//                VrockkApplication.user_obj = null
//
//                startActivity(Intent(this@BaseActivity, LoginActivity::class.java))
//                finishAffinity()
//
//                try {
//                    LoginManager.getInstance().logOut();
//                }catch (e: java.lang.Exception) {
//
//                }
//
//            }
//            .show()
    }

}