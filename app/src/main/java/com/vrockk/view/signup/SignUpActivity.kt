package com.vrockk.view.signup

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.gson.Gson
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
import com.vrockk.view.dashboard.DashboardActivity
import com.vrockk.view.login.LoginActivity
import com.vrockk.view.settings.Terms_Privacy_About_Activity
import com.vrockk.viewmodels.ProfileSetupViewModel
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.model.AspectRatio
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.dialog_gender.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap

class SignUpActivity : BaseActivity() {

    var isChecked: Boolean = false

    var timeInMilliseconds: Long = 0
    var timeDifference: Long = 0

    var gender: String = ""

    var isSocailLogin: Boolean = false

    var currentAddress: String = ""

    var latitude: Double = 0.0
    var longitude: Double = 0.0

    var placeField: List<Place.Field> = listOf(
        Place.Field.ID,
        Place.Field.NAME,
        Place.Field.LAT_LNG,
        Place.Field.ADDRESS_COMPONENTS
    )

    val hashMap: HashMap<String, RequestBody> = HashMap()

    val profileSetupViewModel by viewModel<ProfileSetupViewModel>()

    private var verificationWith: String = "email"
    private var lockClick = false
    lateinit var imageFile: File
    var qwerty: String? = null
    var demoPicPath: String? = ""
    val calender = Calendar.getInstance()

    private var date_of_birth: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        setupPermissions()
        customTextView(tvTermsCondition)
        profileSetupViewModel.profileSetupResponse()
            .observe(this, Observer<ApiResponse> { response ->
                this.signupData(response)
            })

        verificationWith = intent.getStringExtra("verificationWith") ?: ""
        isSocailLogin = intent.getBooleanExtra("isSocailLogin", false)

        if (verificationWith == "phone") {
            etPhone.isEnabled = false
            etPhone.setText(LoginActivity.user_email_phone)
            try {
                val countryCode = LoginActivity.user_country_code
                if (countryCode.contains("+"))
                    countryCode.replace("+", "")
                edtSignupCountryCode.setCountryForPhoneCode(countryCode.toInt())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            edtSignupCountryCode.visibility = View.GONE

            etPhoneCountryCode.visibility = View.VISIBLE
            etPhoneCountryCode.isEnabled = false
            etPhoneCountryCode.setText(LoginActivity.user_country_code)
            tvEmail.text = resources.getString(R.string.emailcap)
        } else {
            etEmail.isEnabled = false
            etEmail.setText(LoginActivity.user_email_phone)
            tvPhone.text = resources.getString(R.string.phone_cap)
        }

        allBtnClicks()

        tvDob.setOnClickListener {

            val c = Calendar.getInstance()
            val mYear = c.get(Calendar.YEAR)
            val mMonth = c.get(Calendar.MONTH)
            val mDay = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    date_of_birth =
                        year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth.toString()

                    val cal = Calendar.getInstance()
                    cal[Calendar.DAY_OF_MONTH] = dayOfMonth
                    cal[Calendar.MONTH] = monthOfYear
                    cal[Calendar.YEAR] = year


                    timeInMilliseconds = cal.timeInMillis
                    Log.e("call", "time Selected starttime: $timeInMilliseconds")

                    Log.e("call", "time CURRENT: ${System.currentTimeMillis()}")

                    tvDob.text = "" + date_of_birth
                }, mYear, mMonth, mDay
            )
            datePickerDialog.show()
            c.add(Calendar.DATE, -1)
            datePickerDialog.datePicker.maxDate = c.timeInMillis
            // datePickerDialog.datePicker.maxDate = System.currentTimeMillis() - 568025136000L
            datePickerDialog.show()

        }

        //  setEmailPhoneVisibilty()

        clLocation.setOnClickListener {
            ////**** Fetch Google places

            Places.initialize(this@SignUpActivity, getString(R.string.api_key))
            val placesClient = Places.createClient(this@SignUpActivity)
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, placeField)
                .build(this)
            startActivityForResult(intent, PermissionsCodes.LOCATION)
        }


        clGender.setOnClickListener {
            showDialogGender()
        }

        tvTermsCondition.setOnClickListener {
            if (isChecked) {
                checkbox.isChecked = false
                isChecked = false
            } else {
                checkbox.isChecked = true
                isChecked = true
            }
        }
        checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            this.isChecked = isChecked
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }


    private fun signupData(response: ApiResponse) {
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
                Log.e("signup error", Gson().toJson(response.error))
                //showCustomToastMessage(this, "Something went wrong!")
            }
        }
    }

    private fun renderResponse(response: ApiResponse) {
        if (response != null) {


            Log.e("signup data_response", Gson().toJson(response))
            val data: String = Utils.toJson(response.data)
            val gson1 = Gson();
            val loginResponse = gson1.fromJson(data, LoginResponse::class.java)

            if (loginResponse.success) {
                val json = Gson().toJson(
                    Data(
                        loginResponse!!.data.__v,
                        loginResponse.data._id,
                        loginResponse.data.authToken,
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
                        loginResponse.data.loginType,
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

                navigateFinishAffinity(DashboardActivity::class.java)

            } else {
                showSnackbar("" + loginResponse.message)
            }

        }
    }

    fun signUp() {

        val request_firstname = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            etFirstName.text.toString()
        )
        val request_lastname = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            etLastName.text.toString()
        )
        val request_username = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            etUsername.text.toString()
        )
        val request_dob = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            timeInMilliseconds.toString()
        )
        val request_address =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), currentAddress)
        val request_bio = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            etBio.text.toString()
        )
        val request_verified =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "true")
        val request_email = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            etEmail.text.toString()
        )
        val request_phone = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            etPhone.text.toString()
        )
        val request_profile_status =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "1")
        val request_password = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            etPassword.text.toString()
        )
        val request_confirm_password = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            etConfirmPassword.text.toString()
        )
        val facebook = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            etFacebookLink.text.toString()
        )
        val instagram = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            etInstagramLink.text.toString()
        )
        val youtube = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            etYoutubeLink.text.toString()
        )
        val request_gender = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            tvGender.text.toString()
        )
        val deviceType =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "android")
        val deviceToken = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            VrockkApplication.fcmToken
        )
        val lat = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            latitude.toString()
        )
        val lng = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            longitude.toString()
        )

        if (verificationWith == "phone") {

            Log.e("call", "country code 1111: " + LoginActivity.user_country_code.toString())
            val request_country_code = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                "+" + LoginActivity.user_country_code.toString()
            )
            hashMap.put("countryCode", request_country_code)
        } else {
            Log.e("call", "country code 2222: " + edtSignupCountryCode.selectedCountryCode)
            val request_country_code = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                "+" + edtSignupCountryCode.selectedCountryCode
            )
            hashMap["countryCode"] = request_country_code
        }

        hashMap["firstName"] = request_firstname
        hashMap["lastName"] = request_lastname
        hashMap["userName"] = request_username
        hashMap["dob"] = request_dob
        hashMap["bio"] = request_bio
        hashMap["address"] = request_address

        hashMap["phone"] = request_phone
        hashMap["email"] = request_email
        hashMap["profileStatus"] = request_profile_status
        hashMap["password"] = request_password
        hashMap["confirmPassword"] = request_confirm_password
        hashMap["gender"] = request_gender
        hashMap["deviceType"] = deviceType
        hashMap["deviceId"] = deviceToken
        hashMap["latitude"] = lat
        hashMap["longitude"] = lng
        hashMap["facebook"] = facebook
        hashMap["instagram"] = instagram
        hashMap["youtube"] = youtube


        if (etReferralCode.text.toString() != "") {
            val request_referral_code = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                etReferralCode.text.toString()
            )

            hashMap.put("referralCode", request_referral_code)
        } else {
            val request_referral_code =
                RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "")

            hashMap.put("referralCode", request_referral_code)
        }

        if (verificationWith.equals("phone")) {
            hashMap.put("isPhoneVerified", request_verified)
        } else {
            hashMap.put("isEmailVerified", request_verified)
        }

        val requestFile: RequestBody

        try {

            if (imageFile != null) {
                Log.e("call", "image1111 " + imageFile.absolutePath)

                demoPicPath = imageFile.absolutePath
                qwerty = "profilePic\"; filename=\"stitchlrappappapp$demoPicPath"
                requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile)
                hashMap.put(qwerty!!, requestFile)
            }
        } catch (e: Exception) {
            Log.e("call", "exception image empty: " + e.message)
        }


        profileSetupViewModel.hitprofileSetup(hashMap)
    }

    private fun allBtnClicks() {
        clBack.setOnClickListener {
            finish()
        }

        ivImageProfile.setOnClickListener {
            showPictureDialog()
        }

//        profileSetupImg.setOnClickListener {
//            showPictureDialog()
//        }

        signUpBtn.setOnClickListener {

            //getTimeDifference2(System.currentTimeMillis(), timeInMilliseconds, this)

            Log.e("2323", timeInMilliseconds.toString())

//            if (etFirstName.text.toString() == "") {
//                showSnackbar(resources.getString(R.string.please_enter_first_name))
//            } else if (etLastName.text.toString() == "") {
//                showSnackbar(resources.getString(R.string.please_enter_last_name))
//            } else
            if (etUsername.text.toString() == "") {
                showSnackbar(resources.getString(R.string.please_enter_username))
            }
//            else if (date_of_birth == "") {
//                showSnackbar(getString(R.string.please_select_date_of_birth))
//            } else if (gender == "") {
//                showSnackbar("Please Select Gender")
//            } else if (currentAddress == "") {
//                showSnackbar(getString(R.string.please_select_current_address))
//            }
            else if (verificationWith == "email") {
                if (etEmail.text.isEmpty()) {
                    if (etPhone.text.toString() == "") {
                        showSnackbar(resources.getString(R.string.please_enter_phone_number))
                    } else if (etPhone.text.toString().toString().length < 7) {
                        showSnackbar("Please enter a valid phone number")
                    }
                }
                if (etPassword.text.isEmpty()) {
                    showSnackbar(resources.getString(R.string.please_enter_password))
                } else if (etPassword.text.toString().length < 8) {
                    showSnackbar(resources.getString(R.string.you_must_have_eight_characters_in_your_password))
                } else if (etConfirmPassword.text.toString() == "") {
                    showSnackbar(resources.getString(R.string.please_enter_confirm_password))
                } else if (etConfirmPassword.text.toString().length < 8) {
                    showSnackbar(resources.getString(R.string.you_must_have_eight_characters_in_your_confirm_password))
                } else if (!getTimeDifference2(
                        System.currentTimeMillis(),
                        timeInMilliseconds,
                        this
                    )
                ) {
                    showSnackbar(resources.getString(R.string.you_should_be_18_plus_to_continue))
                } else {
                    if (checkbox.isChecked) {
                        signUp()
                    } else {
                        showSnackbar(resources.getString(R.string.accept_terms_and_condition))
                    }
                }
            } else {
                if (etPhone.text.isEmpty()) {
                    if (etEmail.text.toString() == "") {
                        showSnackbar(resources.getString(R.string.please_enter_email))
                    } else if (!isValidEmail(etEmail.text.toString())) {
                        showSnackbar(resources.getString(R.string.please_enter_valid_email_address))
                    }
                } else if (etPassword.text.isEmpty()) {
                    showSnackbar(resources.getString(R.string.please_enter_password))
                } else if (etPassword.text.toString().length < 8) {
                    showSnackbar(resources.getString(R.string.you_must_have_eight_characters_in_your_password))
                } else if (etConfirmPassword.text.toString().equals("")) {
                    showSnackbar(resources.getString(R.string.please_enter_confirm_password))
                } else if (etConfirmPassword.text.toString().length < 8) {
                    showSnackbar(resources.getString(R.string.you_must_have_eight_characters_in_your_confirm_password))
                } else if (!etPassword.text.toString()
                        .equals(etConfirmPassword.text.toString())
                ) {
                    showSnackbar(resources.getString(R.string.passwords_not_matching))
                } else if (!getTimeDifference2(
                        System.currentTimeMillis(),
                        timeInMilliseconds,
                        this
                    )
                ) {
                    showSnackbar(resources.getString(R.string.you_should_be_18_plus_to_continue))
                } else if (etFacebookLink.text.isNotEmpty() && !URLUtil.isValidUrl(etFacebookLink.text.toString())) {
                    showSnackbar(resources.getString(R.string.please_enter_valid_fb_url))
                } else if (etInstagramLink.text.isNotEmpty() && !URLUtil.isValidUrl(etInstagramLink.text.toString())) {
                    showSnackbar(resources.getString(R.string.please_enter_valid_insta_url))
                } else if (etYoutubeLink.text.isNotEmpty() && !URLUtil.isValidUrl(etYoutubeLink.text.toString())) {
                    showSnackbar(resources.getString(R.string.please_enter_valid_yt_url))
                } else {
                    if (checkbox.isChecked) {
                        //  if(currentDateTimeString > timeInMilliseconds!! )
                        signUp()
                    } else {
                        showSnackbar("Please accept the Terms and Conditions")
                    }

                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PermissionsCodes.CAMERAPERMISSIONCODE && resultCode == Activity.RESULT_OK) {
//            try {
//                var thumbnail: Bitmap
//                thumbnail = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
//
////              imageFile = File(FileUtils.getPath(this,imageUri))
//
//                val file = File(saveImage(thumbnail))
//                imageFile = file
//
//                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
//                val input: InputStream? = contentResolver?.openInputStream(imageUri!!)
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    // profileSetupImg.setImageBitmap(getRotatedImage(bitmap,input!!))
//                    profileSetupImg.visibility = View.INVISIBLE
//                    tvAddProfilePic.visibility = View.INVISIBLE
//                    ivImageProfile.setImageBitmap(getRotatedImage(bitmap, input!!))
//                } else {
//                    // profileSetupImg.setImageBitmap(thumbnail)
//                    tvAddProfilePic.visibility = View.INVISIBLE
//                    profileSetupImg.visibility = View.INVISIBLE
//                    ivImageProfile.setImageBitmap(thumbnail)
//                }
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
            cropImage(Uri.fromFile(File(imagePathForCamera)))

        } else if (requestCode == PermissionsCodes.GALLERYREQUESTCODE && resultCode == Activity.RESULT_OK) {
//            Log.e("GALLERYREQUESTCODE", "Here")
//            if (data != null) {
//
//                Log.e("call", "data: " + data!!.data)
//                val selectedImageURI = data.getData()
//
//                imageFile = File(FileUtils.getPath(this, selectedImageURI))
//
//                Log.e("call", "image file: " + imageFile)
//
//                // profileSetupImg.setImageURI(data?.data)
//                profileSetupImg.visibility = View.INVISIBLE
//                tvAddProfilePic.visibility = View.INVISIBLE
//                ivImageProfile.setImageURI(data.data)
//            }
            cropImage(Uri.fromFile(File(FileUtilsV4.getFileDataFromIntent(this, data))))

        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val uri = UCrop.getOutput(data)
                if (uri != null)
                    onImageCropped(uri)
            }

        } else if (requestCode == PermissionsCodes.LOCATION) {
            try {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                Log.i(
                    "call",
                    "Place: " + place.name + ", " + place.id + " , " + place.latLng + " , " + place.addressComponents
                )
                val geocoder = Geocoder(this)
                try {
                    val addresses =
                        geocoder.getFromLocation(
                            place.latLng!!.latitude,
                            place.latLng!!.longitude,
                            1
                        )

                    try {
                        Log.e("call", "current address: " + addresses[0].locality)
                        currentAddress = place.name.toString()
                        tvLocation.text = "" + currentAddress
                        latitude = place.latLng!!.latitude
                        longitude = place.latLng!!.longitude
                        Log.e("call", "current latitude: $latitude")
                        Log.e("call", "current longitude: $longitude")
                    } catch (e: java.lang.Exception) {

                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } catch (e: java.lang.Exception) {

            }
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
            .withMaxResultSize(ivImageProfile.width, ivImageProfile.height)
            .useSourceImageAspectRatio()
            .start(this)
    }

    override fun onResume() {
        super.onResume()
        lockClick = false
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
                    this@SignUpActivity,
                    Manifest.permission.CAMERA
                )
                && ActivityCompat.shouldShowRequestPermissionRationale(
                    this@SignUpActivity,
                    Manifest.permission.RECORD_AUDIO
                ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this@SignUpActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this@SignUpActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this@SignUpActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                onfailure(
                    this,
                    getString(R.string.pleasegiveallrequiredpermissions),
                    getString(R.string.gotosettings),
                    this@SignUpActivity
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
        // If request is cancelled, the result arrays are empty.
        if (requestCode == 1) if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        } else {
        }
    }


    internal fun showDialogGender() {
        val dialog1 = Dialog(this)
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog1.setContentView(R.layout.dialog_gender)
        dialog1.show()

        dialog1.tvmale.setOnClickListener {
            gender = "Male"
            tvGender.text = "" + gender
            dialog1.dismiss()
        }

        dialog1.tvFemale.setOnClickListener {
            gender = "Female"
            tvGender.text = "" + gender
            dialog1.dismiss()
        }

        dialog1.tvTrans.setOnClickListener {
            gender = "Transgender"
            tvGender.text = "" + gender
            dialog1.dismiss()
        }

    }


    private fun customTextView(view: TextView) {
        val spanTxt: SpannableStringBuilder = SpannableStringBuilder(
            "I agree to the"
        );
        spanTxt.append(" Terms & Conditions");
        spanTxt.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                val i = Intent(this@SignUpActivity, Terms_Privacy_About_Activity::class.java)
                i.putExtra("type", "terms-and-conditions")
                startActivity(i)
            }
        }, spanTxt.length - "Terms & Conditions".length, spanTxt.length, 0);
        // spanTxt.setSpan(ForegroundColorSpan(Color.BLACK), 32, spanTxt.length, 0);
        view.movementMethod = LinkMovementMethod.getInstance();
        view.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }


    fun getTimeDifference2(currentDate: Long, selectedDate: Long, context: Context): Boolean {
        val checkWith = Calendar.getInstance()
        val old = Calendar.getInstance()
        old.timeInMillis = selectedDate
        Log.e("111", selectedDate.toString())

        var currentDate = checkWith.timeInMillis
        checkWith.add(Calendar.YEAR, -18)
        Log.e("222", selectedDate.toString())
        if (selectedDate < checkWith.timeInMillis) {
            Log.e("333", selectedDate.toString())
            return true
        } else {
            Log.e("444", selectedDate.toString())
            return false
        }

        return false

    }


}