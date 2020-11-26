package com.vrockk.view.settings

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.api.ApiResponse
import com.vrockk.api.Status
import com.vrockk.base.BaseActivity
import com.vrockk.common.PermissionsCodes
import com.vrockk.common.onfailure
import com.vrockk.models.login.Data
import com.vrockk.models.login.LoginResponse
import com.vrockk.utils.FileUtilsV4
import com.vrockk.utils.PreferenceHelper
import com.vrockk.utils.Utils
import com.vrockk.view.signup.EditEmailPhoneActivity
import com.vrockk.viewmodels.UpdateProfileViewModel
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.model.AspectRatio
import kotlinx.android.synthetic.main.activity_updateprofile.*
import kotlinx.android.synthetic.main.dialog_gender.*
import kotlinx.android.synthetic.main.layout_loader.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class UpdateProfileActivity : BaseActivity() {

    var gender: String = ""

    var currentAddress: String = ""

    var placeField: List<Place.Field> = Arrays.asList(
        Place.Field.ID,
        Place.Field.NAME,
        Place.Field.LAT_LNG,
        Place.Field.ADDRESS_COMPONENTS
    )

    lateinit var imageFile: File
    var qwerty: String? = null
    var demoPicPath: String? = ""

    val update by viewModel<UpdateProfileViewModel>()


    val hashMap: HashMap<String, RequestBody> = HashMap()

    companion object {
        var email_update: String = ""
        var phone_update: String = ""
        var countrycode_update: String = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_updateprofile)

        initProgress(contentLoadingProgressBar)

        email_update = ""
        phone_update = ""
        countrycode_update = ""

        if (!VrockkApplication.user_obj!!.profilePic.equals("") && VrockkApplication.user_obj!!.profilePic.contains(
                "https"
            )
        ) {
            tvAddProfilePic.visibility = View.INVISIBLE
            profileSetupImg.visibility = View.INVISIBLE
            ivImageProfile.visibility = View.VISIBLE
            var profileImageUrl: String = VrockkApplication.user_obj!!.profilePic

            Picasso
                .get()
                .load(profileImageUrl)
                .placeholder(R.drawable.user_placeholder)
                .error(R.drawable.user_placeholder)
                .into(ivImageProfile!!)

        } else {
            tvAddProfilePic.visibility = View.VISIBLE
            profileSetupImg.visibility = View.VISIBLE
            ivImageProfile.visibility = View.GONE
        }

        setupPermissions()

        currentAddress = VrockkApplication.user_obj!!.address
        tvLocation.setText("" + VrockkApplication.user_obj!!.address)

        update.updateProfileSetupResponse().observe(this, Observer<ApiResponse> { response ->
            this.updateData(response)
        })

        edtEmail.setOnClickListener {
            if (VrockkApplication.user_obj!!.loginType == "simple") {
                val i = Intent(this, EditEmailPhoneActivity::class.java)
                i.putExtra("verificationWith", "email")
                i.putExtra("email", "" + VrockkApplication.user_obj!!.email)
                startActivity(i)
            }

        }

        edtPhone.setOnClickListener {
            val i = Intent(this, EditEmailPhoneActivity::class.java)
            i.putExtra("verificationWith", "phone")
            i.putExtra("phone", "" + VrockkApplication.user_obj!!.phone)
            i.putExtra("country_code", "" + VrockkApplication.user_obj!!.countryCode)
            startActivity(i)
        }

        tvBack.setOnClickListener {
            finish()
        }

        tvUpdate.setOnClickListener {
            finish()
        }

        tvUpdate.setOnClickListener {

//            if (edtFirstName.text.toString().equals(""))
//            {
//                showSnackbar(resources.getString(R.string.please_enter_first_name))
//            }
//            else if (edtLastName.text.toString().equals(""))
//            {
//                showSnackbar(resources.getString(R.string.please_enter_last_name))
//            }
//            else if (currentAddress.equals(""))
//            {
//                showSnackbar(getString(R.string.please_select_current_address))
//            }
//            else if (gender.equals(""))
//            {
//                showSnackbar("Please select gender")
//            }
//            else
//            {
            if (edtEFacebookLink.text.isNotEmpty() && !URLUtil.isValidUrl(edtEFacebookLink.text.toString())) {
                showSnackbar(resources.getString(R.string.please_enter_valid_fb_url))
            } else if (edtEInstagramLink.text.isNotEmpty() && !URLUtil.isValidUrl(edtEInstagramLink.text.toString())) {
                showSnackbar(resources.getString(R.string.please_enter_valid_insta_url))
            } else if (edtEYoutubeLink.text.isNotEmpty() && !URLUtil.isValidUrl(edtEYoutubeLink.text.toString())) {
                showSnackbar(resources.getString(R.string.please_enter_valid_yt_url))
            } else {
                updateProfile()
            }
//            }
        }

        tvLocation.setOnClickListener {
            Places.initialize(this@UpdateProfileActivity, getString(R.string.api_key))
            val placesClient = Places.createClient(this@UpdateProfileActivity)
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, placeField)
                .build(this)
            startActivityForResult(intent, PermissionsCodes.LOCATION)
        }

        tvGender.setOnClickListener {
            showDialogGender()
        }


        profileSetupImg.setOnClickListener {
            showPictureDialog()
        }
        ivImageProfile.setOnClickListener {
            showPictureDialog()
        }

        setData()
    }

    private fun setData() {
        edtFirstName.setText("" + VrockkApplication.user_obj!!.firstName)
        edtLastName.setText("" + VrockkApplication.user_obj!!.lastName)
        if (!VrockkApplication.user_obj!!.userName.equals(""))
            tvUserName.setText("@" + VrockkApplication.user_obj!!.userName)

        edtBio.setText("" + VrockkApplication.user_obj!!.bio)

        tvGender.setText("" + VrockkApplication.user_obj!!.gender)
        gender = "" + VrockkApplication.user_obj!!.gender

        if (email_update.equals("")) {
            edtEmail.setText("" + VrockkApplication.user_obj!!.email)
        } else {
            edtEmail.setText("" + email_update)
        }

        if (phone_update.equals("")) {
            edtPhone.setText(
                VrockkApplication.user_obj!!.countryCode + " " +
                        VrockkApplication.user_obj!!.phone
            )
        } else {
            edtPhone.setText(countrycode_update + " " + phone_update)
        }

//        if(edtEmail.text == "" && edtPhone.text != ""){
//            tvPhone.text = resources.getString(R.string.phone_)
//        }
//
//        if(edtPhone.text == "" && edtEmail.text != ""){
//            tvemail.text = resources.getString(R.string.email_)
//        }

        edtEFacebookLink.setText("" + VrockkApplication.user_obj!!.facebook)
        edtEInstagramLink.setText("" + VrockkApplication.user_obj!!.instagram)
        edtEYoutubeLink.setText("" + VrockkApplication.user_obj!!.youtube)
    }

    override fun onResume() {
        super.onResume()
    }

    private fun updateData(response: ApiResponse) {
        when (response!!.status) {
            Status.LOADING -> {
                showProgress("")
            }
            Status.SUCCESS -> {
                hideProgress()
                renderResponse(response)
            }
            Status.ERROR -> {
                hideProgress()
                Log.e("error", Gson().toJson(response.error))
                //showCustomToastMessage(this, "Something went wrong!")
            }
        }
    }

    private fun renderResponse(response: ApiResponse) {
        if (response != null) {
            Log.e("data_response", Gson().toJson(response))
            val data: String = Utils.toJson(response.data)
            val gson1 = Gson();
            val loginResponse = gson1.fromJson(data, LoginResponse::class.java)

            if (loginResponse.success) {
                val json = Gson().toJson(
                    Data(
                        loginResponse!!.data.__v,
                        loginResponse!!.data._id,
                        VrockkApplication.user_obj!!.authToken,
                        loginResponse.data.bio,
                        loginResponse.data.countryCode,
                        loginResponse.data.createdAt,
                        loginResponse.data.dob,
                        loginResponse.data.email,
                        loginResponse.data.firstName,
                        loginResponse.data.id,
                        loginResponse.data.instagram,
                        loginResponse.data.isEmailVerified,
                        loginResponse.data.isPhoneVerified,
                        loginResponse.data.lastName,
                        loginResponse.data.phone,
                        loginResponse.data.profilePic,
                        loginResponse.data.profileStatus,
                        loginResponse.data.provider,
                        loginResponse.data.providerId,
                        loginResponse.data.referralCode,
                        loginResponse.data.updatedAt,
                        loginResponse.data.userName,
                        VrockkApplication.user_obj!!.loginType,
                        loginResponse.data.address,
                        loginResponse.data.gender,
                        loginResponse.data.level,
                        loginResponse.data.facebook,
                        loginResponse.data.youtube

                    )
                )

                //**  save data in shared prefrences class

                val editor = VrockkApplication.prefs.edit()

                VrockkApplication.user_obj =
                    VrockkApplication.gson!!.fromJson(json.toString(), Data::class.java) as Data
                editor.putString(PreferenceHelper.Key.REGISTEREDUSER, json)
                editor.commit()

//              navigateFinishAffinity(HomeActivity::class.java)

                showToast("profile updated successfully")
                setData()
                finish()

            } else {
                showSnackbar("" + loginResponse.message)
            }

        }
    }


    fun updateProfile() {

        val request_firstname = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            edtFirstName.text.toString()
        )
        val request_lastname = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            edtLastName.text.toString()
        )
        val request_address =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), currentAddress)
        val request_gender = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            tvGender.text.toString()
        )
        val request_facebook = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            edtEFacebookLink.text.toString()
        )
        val request_instagram = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            edtEInstagramLink.text.toString()
        )
        val request_youtube = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            edtEYoutubeLink.text.toString()
        )


        if (email_update.equals("")) {
            val request_email = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                VrockkApplication.user_obj!!.email
            )
            hashMap.put("email", request_email)
        } else {
            val request_email =
                RequestBody.create(MediaType.parse("application/json; charset=utf-8"), email_update)
            hashMap.put("email", request_email)
        }

        if (phone_update.equals("")) {
            val request_phone = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                VrockkApplication.user_obj!!.phone
            )
            hashMap.put("phone", request_phone)

        } else {
            val request_phone =
                RequestBody.create(MediaType.parse("application/json; charset=utf-8"), phone_update)
            hashMap.put("phone", request_phone)
        }

        if (countrycode_update.equals("")) {
            val request_country_code = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                VrockkApplication.user_obj!!.countryCode
            )
            hashMap.put("countryCode", request_country_code)
        } else {
            val request_country_code = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                countrycode_update
            )
            hashMap.put("countryCode", request_country_code)
        }

        hashMap.put("firstName", request_firstname)
        hashMap.put("lastName", request_lastname)
        hashMap.put("address", request_address)
        hashMap.put("gender", request_gender)
        hashMap.put("facebook", request_facebook)
        hashMap.put("instagram", request_instagram)
        hashMap.put("youtube", request_youtube)



        if (!edtBio.text.toString().equals("")) {
            val request_bio = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                edtBio.text.toString()
            )
            hashMap.put("bio", request_bio)
        } else {
            val request_bio =
                RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "")
            hashMap.put("bio", request_bio)
        }
        val requestFile: RequestBody

        try {

            if (imageFile != null) {
                Log.e("call", "image1111 " + imageFile.absolutePath)

                demoPicPath = imageFile.absolutePath
                qwerty = "profilePic" + "\"; filename=\"stitchlrappappapp" + demoPicPath
                requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile)
                hashMap.put(qwerty!!, requestFile)
            }
        } catch (e: Exception) {
            Log.e("call", "exception image empty: " + e.message)
        }

        var token: String
        token = "SEC " + VrockkApplication.user_obj!!.authToken

        update.hitUpdateProfileSetup(token, hashMap)
    }

    private fun showDialogGender() {
        val dialog1 = Dialog(this)
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog1.setContentView(R.layout.dialog_gender)
        dialog1.show()

        dialog1.tvmale.setOnClickListener {
            gender = "Male"
            tvGender.text = gender
            dialog1.dismiss()
        }

        dialog1.tvFemale.setOnClickListener {
            gender = "Female"
            tvGender.text = gender
            dialog1.dismiss()
        }

        dialog1.tvTrans.setOnClickListener {
            gender = "Transgender"
            tvGender.text = gender
            dialog1.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PermissionsCodes.CAMERAPERMISSIONCODE && resultCode == Activity.RESULT_OK) {
//            cropImage(Uri.fromFile(File(imagePathForCamera)))
//            if (!cropImageV2(/*Uri.fromFile*/FileProvider.getUriForFile(
//                    applicationContext, "${BuildConfig.APPLICATION_ID}.provider",
//                    File(imagePathForCamera))))
            cropImage(Uri.fromFile(File(imagePathForCamera)))

        } else if (requestCode == PermissionsCodes.GALLERYREQUESTCODE && resultCode == Activity.RESULT_OK) {
//            cropImage(Uri.fromFile(File(FileUtilsV4.getFileDataFromIntent(this, data))))
//            if (!cropImageV2(/*Uri.fromFile*/FileProvider.getUriForFile(
//                    applicationContext, "${BuildConfig.APPLICATION_ID}.provider",
//                    File(FileUtilsV4.getFileDataFromIntent(this, data)))))
            cropImage(Uri.fromFile(File(FileUtilsV4.getFileDataFromIntent(this, data))))

        } else if (requestCode == PermissionsCodes.LOCATION && resultCode == Activity.RESULT_OK) {
            try {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                val geocoder = Geocoder(this)
                try {
                    val addresses = geocoder.getFromLocation(
                        place.latLng!!.latitude,
                        place.latLng!!.longitude, 1
                    )
                    try {
                        currentAddress = place.name.toString()
                        tvLocation.setText("" + currentAddress)
                    } catch (e: java.lang.Exception) {

                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } catch (e: java.lang.Exception) {

            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val uri = UCrop.getOutput(data)
                if (uri != null)
                    onImageCropped(uri)
            }

        } else if (requestCode == 2345 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val extras = data.extras;
                if (extras != null) {
                    val bitmap = extras.getParcelable<Bitmap>("data")
                    if (bitmap != null) {
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 0, baos)

                        val file = File(cacheDir, "" + System.currentTimeMillis() + ".jpg")
                        file.createNewFile()

                        val fos = FileOutputStream(file)
                        fos.write(baos.toByteArray())
                        fos.flush()
                        fos.close()

                        imageFile = file
                        Picasso.get()
                            .load(imageFile)
                            .into(ivImageProfile)
                    }
                }
            }
        }
    }

    private fun onCameraImage() {
        try {
//                var thumbnail: Bitmap =
//                    MediaStore.Images.Media.getBitmap(this!!.contentResolver, imageUri)
//              imageFile = File(FileUtils.getPath(this,imageUri))

//                val file = File(saveImage(thumbnail))
//                imageFile = file

//                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
//                val input: InputStream? = contentResolver?.openInputStream(imageUri!!)

            imageFile = File(imagePathForCamera)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    profileSetupImg.setImageBitmap(getRotatedImage(bitmap,input!!))
                profileSetupImg.visibility = View.INVISIBLE
                tvAddProfilePic.visibility = View.INVISIBLE
//                    ivImageProfile.setImageBitmap(getRotatedImage(bitmap, input!!))
                Picasso.get()
                    .load(imagePathForCamera)
                    .into(ivImageProfile)
            } else {
//                    profileSetupImg.setImageBitmap(thumbnail)
                tvAddProfilePic.visibility = View.INVISIBLE
                profileSetupImg.visibility = View.INVISIBLE
//                    ivImageProfile.setImageBitmap(thumbnail)
                Picasso.get()
                    .load(imagePathForCamera)
                    .into(ivImageProfile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onGalleryImage(data: Intent?) {
        if (data != null) {
//                val selectedImageURI = data.getData()
            val imagePath = FileUtilsV4.getFileDataFromIntent(this, data)
            imageFile = File(imagePath)

            profileSetupImg.visibility = View.INVISIBLE
            tvAddProfilePic.visibility = View.INVISIBLE
//                ivImageProfile.setImageURI(data?.data)
            Picasso.get()
                .load(imagePath)
                .into(ivImageProfile)
        }
    }

    private fun onImageCropped(uri: Uri) {
        val imagePath = FileUtilsV4.getFileDataFromUri(this, uri)
        imageFile = File(imagePath)

        profileSetupImg.visibility = View.INVISIBLE
        tvAddProfilePic.visibility = View.INVISIBLE
//                ivImageProfile.setImageURI(data?.data)

        if (ivImageProfile != null) {
            ivImageProfile.visibility = View.VISIBLE
//            Picasso.get()
//                .load(imagePath)
//                .into(ivImageProfile)

            val inputStream = contentResolver?.openInputStream(uri)
            var bitmap = BitmapFactory.decodeFile(imagePath)
            if (inputStream != null)
                bitmap = getRotatedImage(bitmap, inputStream)
            ivImageProfile.setImageBitmap(bitmap)
        } else
            Toast.makeText(this, "ImageView null", Toast.LENGTH_SHORT).show()
    }

    private fun cropImage(sourceUri: Uri) {
        val destinationUri: Uri = Uri.fromFile(
            File(
                cacheDir,
                "" + System.currentTimeMillis() + ".jpg"
            )
        )

        val options: UCrop.Options = UCrop.Options()
//        options.setCompressionQuality(IMAGE_COMPRESSION)
        options.setHideBottomControls(false)
        options.setAspectRatioOptions(
            3,
            AspectRatio(null, 1F, 1F),
            AspectRatio(null, 3F, 4F),
            AspectRatio("Original", 0F, 0F),
            AspectRatio(null, 3F, 2F),
            AspectRatio(null, 16F, 9F)
        )
        options.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary))
        options.setActiveWidgetColor(ContextCompat.getColor(this, R.color.colorPrimary))

        UCrop.of(sourceUri, destinationUri)
            .withOptions(options)
            .withMaxResultSize(/*ivImageProfile.width*/ 450, /*ivImageProfile.height*/ 300)
            .useSourceImageAspectRatio()
            .start(this)
    }

    private fun cropImageV2(sourceUri: Uri): Boolean {
//        final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();

        val cropIntent = Intent("com.android.camera.action.CROP")
        cropIntent.setDataAndType(sourceUri, "image/*")

        val list = packageManager.queryIntentActivities(cropIntent, 0);
        val size = list.size;

        if (size == 0) {
            Log.e(TAG, "cropImageV2: default activities not found")
//            Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();
            return false
        } else {
            cropIntent.setDataAndType(sourceUri, "image/*")
            cropIntent.putExtra("outputX", /*ivImageProfile.width*/ 450)
            cropIntent.putExtra("outputY", /*ivImageProfile.height*/ 300)
            cropIntent.putExtra("aspectX", 1)
            cropIntent.putExtra("aspectY", 1)
            cropIntent.putExtra("crop", true)
            cropIntent.putExtra("scale", true)
            cropIntent.putExtra("return-data", true)
            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            if (size >= 1) {
                val checkCropIntent = Intent(cropIntent);
                val res = list[0];
                checkCropIntent.component =
                    ComponentName(res.activityInfo.packageName, res.activityInfo.name)
                grantUriPermission(
                    res.activityInfo.packageName,
                    sourceUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                startActivityForResult(checkCropIntent, 2345);
            }
        }
        return true
    }

    private fun setupPermissions() {
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
            Manifest.permission.WRITE_EXTERNAL_STORAGE + Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@UpdateProfileActivity,
                    Manifest.permission.CAMERA
                )
                && ActivityCompat.shouldShowRequestPermissionRationale(
                    this@UpdateProfileActivity,
                    Manifest.permission.RECORD_AUDIO
                ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this@UpdateProfileActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this@UpdateProfileActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this@UpdateProfileActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                onfailure(
                    this,
                    getString(R.string.pleasegiveallrequiredpermissions),
                    getString(R.string.gotosettings),
                    this@UpdateProfileActivity
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

        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {

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
}