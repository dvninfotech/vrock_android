package com.vrockk.view.signup

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
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
import android.widget.CompoundButton
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
import com.squareup.picasso.Callback
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
import com.vrockk.view.dashboard.DashboardActivity
import com.vrockk.view.login.LoginActivity
import com.vrockk.view.settings.Terms_Privacy_About_Activity
import com.vrockk.viewmodels.UpdateProfileViewModel
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.model.AspectRatio
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.dialog_gender.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap

class SocailProfileUpdateActivity : BaseActivity() {

    var isChecked: Boolean = false

    var timeInMilliseconds: Long = 0

    var gender: String = ""

    var isSocailLogin: Boolean = false

    var currentAddress: String = ""

    var placeField: List<Place.Field> = Arrays.asList(
        Place.Field.ID,
        Place.Field.NAME,
        Place.Field.LAT_LNG,
        Place.Field.ADDRESS_COMPONENTS
    )

    val hashMap: HashMap<String, RequestBody> = HashMap()

    val updateProfileViewModel by viewModel<UpdateProfileViewModel>()

    private var verificationWith: String = "email"
    private var lockClick = false
    lateinit var imageFile: File
    var qwerty: String? = null
    var demoPicPath: String? = ""
    val calender = Calendar.getInstance()

    var date_of_birth: String = ""


    var latitude: Double = 0.0
    var longitude: Double = 0.0


    var token: String = ""

//    companion object {
//        var user_email = ""
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        setupPermissions()
        customTextView(tvTermsCondition)
        updateProfileViewModel.updateProfileSetupResponse()
            .observe(this, Observer<ApiResponse> { response ->
                this.updateData(response)
            })

        isSocailLogin = intent.getBooleanExtra("isSocailLogin", false)
        token = intent.getStringExtra("token") ?: ""

        etEmail.setText(LoginActivity.socail_email)
        etEmail.isEnabled = false
        etFirstName.setText(LoginActivity.socail_first_name)
        etLastName.setText(LoginActivity.socail_last_name)

        if (LoginActivity.social_user_profile != null) {
            Picasso.get().load(LoginActivity.social_user_profile!!)
                .into(ivImageProfile, object : Callback {
                    override fun onSuccess() {
                        val bitmapDrawable = ivImageProfile.drawable as BitmapDrawable
                        Thread {
                            imageFile = File(cacheDir, "VrockkUser_${System.currentTimeMillis()}")
                            imageFile.createNewFile()

                            val bos = ByteArrayOutputStream()
                            bitmapDrawable.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
                            val bitmapdata = bos.toByteArray();

                            val fos = FileOutputStream(imageFile)
                            fos.write(bitmapdata)
                            fos.flush()
                            fos.close()
                        }.start()
                    }

                    override fun onError(e: java.lang.Exception?) {

                    }
                })
        }

        tvPassword.visibility = View.GONE
        clPassword.visibility = View.GONE
        tvConPassword.visibility = View.GONE
        clConPassword.visibility = View.GONE
        edtSignupCountryCode.visibility = View.VISIBLE
        tvPhone.text = resources.getString(R.string.phone_cap)

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

                    Log.e("call", "starttime: " + timeInMilliseconds)
                    tvDob.text = "" + date_of_birth

                }, mYear, mMonth, mDay
            )
            datePickerDialog.show()
            c.add(Calendar.DATE, -1)
            // datePickerDialog.datePicker.maxDate = c.timeInMillis
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis() - 568025136000L
            datePickerDialog.show()

        }
        //   setEmailPhoneVisibilty()

        clLocation.setOnClickListener {
            ////**** Fetch Google places

//            setupPermissions()

            Places.initialize(this@SocailProfileUpdateActivity, getString(R.string.api_key))
            val placesClient = Places.createClient(this@SocailProfileUpdateActivity)
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
        checkbox.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                this.isChecked = true
            } else {
                this.isChecked = false
            }
        }
        )
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
                Log.e("signup_error", Gson().toJson(response.error))
                //showCustomToastMessage(this, "Something went wrong!")
            }
        }
    }

    private fun renderResponse(response: ApiResponse) {
        if (response != null) {
            Log.e("1111 data_response", Gson().toJson(response))
            val data: String = Utils.toJson(response.data)
            val gson1 = Gson();
            val loginResponse = gson1.fromJson(data, LoginResponse::class.java)

            if (loginResponse.success) {

                try {
                    val json = Gson().toJson(
                        Data(
                            loginResponse!!.data.__v,
                            loginResponse!!.data._id,
                            token,
                            loginResponse!!.data.bio,
                            loginResponse!!.data.countryCode,
                            loginResponse!!.data.createdAt,
                            loginResponse!!.data.dob,
                            loginResponse!!.data.email,
                            loginResponse!!.data.firstName,
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
                    editor.apply()

                    navigateFinishAffinity(DashboardActivity::class.java)
                } catch (e: Exception) {
                    Log.e("call", "1111 exp: " + e.message)
                }
            } else {
                showSnackbar("" + loginResponse.message)
            }

        }
    }

    fun updateProfile() {

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
        val request_referral_code = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            etReferralCode.text.toString()
        )
        val request_email = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            etEmail.text.toString()
        )
        val request_phone = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            etPhone.text.toString()
        )
        val request_country_code = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            "+" + edtSignupCountryCode.selectedCountryCode
        )
        val request_profile_status =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "1")
        val request_verified =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "true")
        val request_gender = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            tvGender.text.toString()
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


        val lat = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            latitude.toString()
        )
        val lng = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            longitude.toString()
        )

        hashMap.put("firstName", request_firstname)
        hashMap.put("lastName", request_lastname)
        hashMap.put("userName", request_username)
        hashMap.put("dob", request_dob)
        hashMap.put("bio", request_bio)
        hashMap.put("address", request_address)
        hashMap.put("countryCode", request_country_code)
        hashMap.put("phone", request_phone)
        hashMap.put("email", request_email)
        hashMap.put("profileStatus", request_profile_status)
        hashMap.put("referralCode", request_referral_code)
        hashMap.put("gender", request_gender)
        hashMap["facebook"] = facebook
        hashMap["instagram"] = instagram
        hashMap["youtube"] = youtube

        val requestFile: RequestBody

        try {

            if (imageFile != null) {
                Log.e("call", "image1111 " + imageFile.absolutePath)

                demoPicPath = imageFile.absolutePath
                qwerty = "profilePic" + "\"; filename=\"stitchlrappappapp" + demoPicPath
                requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile)
                hashMap.put(qwerty!!, requestFile)
            } else if (LoginActivity.social_user_profile != null) {

            }
        } catch (e: Exception) {
            Log.e("call", "exception image empty: " + e.message)
        }

        Log.e("call", "1111 callllllllll")


        updateProfileViewModel.hitUpdateProfileSetup("SEC " + token, hashMap)
    }

    private fun allBtnClicks() {
        clBack.setOnClickListener {
            finish()
        }

//        profileSetupImg.setOnClickListener {
//            showPictureDialog()
//        }

        ivImageProfile.setOnClickListener {
            showPictureDialog()
        }

        signUpBtn.setOnClickListener {

//            if (etFirstName.text.toString().equals(""))
//            {
//                showSnackbar(resources.getString(R.string.please_enter_first_name))
//            }
//            else if (etLastName.text.toString().equals(""))
//            {
//                showSnackbar(resources.getString(R.string.please_enter_last_name))
//            }
//            else
            if (etUsername.text.toString() == "") {
                showSnackbar(resources.getString(R.string.please_enter_username))
            }
//            else if (date_of_birth.equals("")) {
//                showSnackbar(getString(R.string.please_select_date_of_birth))
//            } else if (gender.equals("")) {
//                showSnackbar("Please select gender")
//            } else if (currentAddress.equals("")) {
//                showSnackbar(getString(R.string.please_select_current_address))
//            } else
            else if (etEmail.text.toString() == "" && etPhone.text.toString() == "") {
                showSnackbar(resources.getString(R.string.please_enter_email))
            } else {
                if (etEmail.text.isNotEmpty()) {
                    if (!isValidEmail(etEmail.text.toString())) {
                        showSnackbar(resources.getString(R.string.please_enter_valid_email_address))
                        return@setOnClickListener
                    }
                }
                if (etPhone.text.isNotEmpty()) {
                    if (etPhone.text.toString().toString().length < 7) {
                        showSnackbar(resources.getString(R.string.you_must_have_eight_characters_in_your_phone))
                        return@setOnClickListener
                    }
                }else if (etFacebookLink.text.isNotEmpty() && !URLUtil.isValidUrl(etFacebookLink.text.toString())) {
                    showSnackbar(resources.getString(R.string.please_enter_valid_fb_url))
                } else if (etInstagramLink.text.isNotEmpty() && !URLUtil.isValidUrl(etInstagramLink.text.toString())) {
                    showSnackbar(resources.getString(R.string.please_enter_valid_insta_url))
                } else if (etYoutubeLink.text.isNotEmpty() && !URLUtil.isValidUrl(etYoutubeLink.text.toString())) {
                    showSnackbar(resources.getString(R.string.please_enter_valid_yt_url))
                }else if (!getTimeDifference2(
                        System.currentTimeMillis(),
                        timeInMilliseconds,
                        this
                    )
                ) {
                    showSnackbar(resources.getString(R.string.you_should_be_18_plus_to_continue))
                } else {
                    if (checkbox.isChecked) {
                        updateProfile()
                    } else {
                        showSnackbar("Please accept the Terms and Conditions")
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        ////  camera pic ----

        if (requestCode == PermissionsCodes.CAMERAPERMISSIONCODE) {
//            try {
//                var thumbnail: Bitmap
//                thumbnail = MediaStore.Images.Media.getBitmap(this!!.getContentResolver(), imageUri)
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
////                    profileSetupImg.setImageBitmap(getRotatedImage(bitmap,input!!))
//                    profileSetupImg.visibility = View.INVISIBLE
//                    tvAddProfilePic.visibility = View.INVISIBLE
//                    ivImageProfile.setImageBitmap(getRotatedImage(bitmap, input!!))
//                } else {
////                    profileSetupImg.setImageBitmap(thumbnail)
//                    tvAddProfilePic.visibility = View.INVISIBLE
//                    profileSetupImg.visibility = View.INVISIBLE
//                    ivImageProfile.setImageBitmap(thumbnail)
//                }
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
            cropImage(Uri.fromFile(File(imagePathForCamera)))

        } else if (requestCode == PermissionsCodes.GALLERYREQUESTCODE) {
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
////                profileSetupImg.setImageURI(data?.data)
//                profileSetupImg.visibility = View.INVISIBLE
//                tvAddProfilePic.visibility = View.INVISIBLE
//                ivImageProfile.setImageURI(data?.data)
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

//                val geocoder = Geocoder(this)
//                val addresses = geocoder.getFromLocation(place.latLng!!.latitude, place.latLng!!.longitude, 1)

                try {
                    currentAddress = place.name.toString()
                    tvLocation.text = "" + currentAddress
                    latitude = place.latLng!!.latitude
                    longitude = place.latLng!!.longitude
                } catch (e: java.lang.Exception) {

                }
            } catch (e: IOException) {
                e.printStackTrace()
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
                    this@SocailProfileUpdateActivity,
                    Manifest.permission.CAMERA
                )
                && ActivityCompat.shouldShowRequestPermissionRationale(
                    this@SocailProfileUpdateActivity,
                    Manifest.permission.RECORD_AUDIO
                ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this@SocailProfileUpdateActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this@SocailProfileUpdateActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this@SocailProfileUpdateActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                onfailure(
                    this,
                    getString(R.string.pleasegiveallrequiredpermissions),
                    getString(R.string.gotosettings),
                    this@SocailProfileUpdateActivity
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


    internal fun showDialogGender() {
        val dialog1 = Dialog(this)
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog1.setContentView(R.layout.dialog_gender)
        dialog1.show()

        dialog1.tvmale.setOnClickListener {
            gender = "Male"
            tvGender.setText("" + gender)
            dialog1.dismiss()
        }

        dialog1.tvFemale.setOnClickListener {
            gender = "Female"
            tvGender.setText("" + gender)
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
                val i = Intent(
                    this@SocailProfileUpdateActivity,
                    Terms_Privacy_About_Activity::class.java
                )
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

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

}