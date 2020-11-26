package com.vrockk.view.signup

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputType
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.api.ApiResponse
import com.vrockk.api.Status
import com.vrockk.base.BaseActivity
import com.vrockk.models.login.Data
import com.vrockk.models.login.LoginResponse
import com.vrockk.models.signup.SignUpResponse
import com.vrockk.models.verify.VerifyOtpResponse
import com.vrockk.utils.PreferenceHelper
import com.vrockk.utils.Utils
import com.vrockk.view.dashboard.DashboardActivity
import com.vrockk.view.settings.Terms_Privacy_About_Activity
import com.vrockk.viewmodels.EmailOtpVerficationViewModel
import com.vrockk.viewmodels.ForgotPasswordViewModel
import com.vrockk.viewmodels.ProfileSetupViewModel
import com.vrockk.viewmodels.SignUpViewModel
import kotlinx.android.synthetic.main.activity_sign_up_v2.*
import kotlinx.android.synthetic.main.activity_sign_up_v2.checkbox
import kotlinx.android.synthetic.main.activity_sign_up_v2.edtSignupCountryCode
import kotlinx.android.synthetic.main.activity_sign_up_v2.etEmail
import kotlinx.android.synthetic.main.activity_sign_up_v2.etPassword
import kotlinx.android.synthetic.main.activity_sign_up_v2.tvEmail
import kotlinx.android.synthetic.main.activity_sign_up_v2.tvTermsCondition
import kotlinx.android.synthetic.main.my_custom_alert.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*

class SignUpV2Activity : BaseActivity() {
    private companion object {
        const val TYPE_GET_OTP = 1
        const val TYPE_VERIFY_OTP = 2
        const val TYPE_RESEND_OTP = 3
        const val TYPE_SIGNUP = 4
    }

    private var isChecked: Boolean = false
    private var isOtpVerified: Boolean = false
    private var isOtpReceived: Boolean = false
    private var verificationWith: String = "email"
    private var isEmail: Boolean = true
    private val signUpViewModel by viewModel<SignUpViewModel>()
    private val otpVerficationViewModel by viewModel<EmailOtpVerficationViewModel>()
    private val forgotPasswordViewModel by viewModel<ForgotPasswordViewModel>()
    private val profileSetupViewModel by viewModel<ProfileSetupViewModel>()
    private var timer: Timer? = null
    private var countDownTimer: CountDownTimer? = null

    private var isSocialLogin: Boolean = false
    private var socialEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_v2)
        initProgress(contentLoadingProgressBar)
        hideProgress()

        customTextView(tvTermsCondition)
        pinview.setTextColor(Color.WHITE)
        signUpViewModel.signupResponse().observe(this, Observer<ApiResponse> { response ->
            this.responseData(response, TYPE_GET_OTP)
        })
        otpVerficationViewModel.verifyOtpResponse()
            .observe(this, Observer<ApiResponse> { response ->
                this.responseData(response, TYPE_VERIFY_OTP)
            })

        forgotPasswordViewModel.forgotPasswordResponse()
            .observe(this, Observer<ApiResponse> { response ->
                this.responseData(response, TYPE_RESEND_OTP)
            })

        profileSetupViewModel.profileSetupResponse()
            .observe(this, Observer<ApiResponse> { response ->
                this.responseData(response, TYPE_SIGNUP)
            })

        allBtnClicks()

        isSocialLogin = intent.getBooleanExtra("isSocialLogin", false)
        if(isSocialLogin){
            socialEmail = intent.getStringExtra("socialEmail") ?: ""
            if(socialEmail == ""){
                showSnackbar(resources.getString(R.string.social_email_not_found))
                finish()
            }else{
                isOtpReceived = true
                isOtpVerified = true
                isEmail = false
                changePhoneEmailView()
                etEmail.setText(socialEmail)
                etEmail.visibility = View.GONE
                etDisabledEmailPhone.visibility = View.VISIBLE
                etDisabledEmailPhone.setText(socialEmail)
                loginWithPhoneBtn.visibility = View.GONE
                return
            }
        }
        changePhoneEmailView()

    }

    override fun onDestroy() {
        super.onDestroy()
        if (timer != null) timer!!.cancel()
    }

    override fun onBackPressed() {
        if ((isOtpReceived || isOtpVerified) && !isSocialLogin) {
            doOnProcessReinitiate()
        } else
            super.onBackPressed()
    }


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun responseData(response: ApiResponse, type: Int) {
        when (response.status) {
            Status.LOADING -> {
                showProgress("")
            }
            Status.SUCCESS -> {
                hideProgress()
                renderResponse(response, type)
            }
            Status.ERROR -> {
                hideProgress()
                Log.e("signup_error", Gson().toJson(response.error))
                //showCustomToastMessage(this, "Something went wrong!")
            }
        }
    }

    private fun renderResponse(response: ApiResponse, type: Int) {

        Log.e("signup_TYPE_OTP_response: ", Gson().toJson(response))
        val data: String = Utils.toJson(response.data)
        val gson1 = Gson();

        if (type == TYPE_GET_OTP) {
            val signupresponse = gson1.fromJson(data, SignUpResponse::class.java)
            if (signupresponse.success) {
                isOtpReceived = true
                isOtpVerified = false
                otpContainer.visibility = View.VISIBLE
                if (timer != null) timer!!.cancel()
                if (countDownTimer != null) countDownTimer!!.cancel()
                showTimer()
                setButtonVisibility()

            } else {
                isOtpReceived = false
                loginWithPhoneBtn.visibility = View.VISIBLE
                etEmail.visibility = View.VISIBLE
                etDisabledEmailPhone.visibility = View.GONE
                showSnackbar("" + signupresponse.message)
            }
        } else if (type == TYPE_VERIFY_OTP) {
            val verifyOtpResponse = gson1.fromJson(data, VerifyOtpResponse::class.java)
            if (verifyOtpResponse.success) {
                isOtpVerified = true
                otpContainer.visibility = View.GONE
                btnNext.text = resources.getString(R.string.signup_)
                if (timer != null) timer!!.cancel()
                if (countDownTimer != null) countDownTimer!!.cancel()
                performNextValidations()
            } else {
                showSnackbar(verifyOtpResponse.message)
                isOtpVerified = false
            }
        }else if(type == TYPE_SIGNUP){
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

    private fun performNextValidations() {
        if (etVrockkId.text.toString() == "")
            showSnackbar(resources.getString(R.string.enter_vrock_id))
        else if (etPassword.text.isEmpty()) {
            showSnackbar(resources.getString(R.string.please_enter_password))
        } else if (etPassword.text.toString().length < 8) {
            showSnackbar(resources.getString(R.string.you_must_have_eight_characters_in_your_password))
        } else if (!checkbox.isChecked) {
            showSnackbar(resources.getString(R.string.accept_terms_and_condition))
        } else if (!isOtpReceived && !isOtpVerified) {
            getOtp()
        } else if (isOtpReceived && !isOtpVerified) {
            verifyOtp()
        } else if (isOtpReceived && isOtpVerified) {
            signUp()
        }
    }

    private fun signUp() {
//        showSnackbar("sign up called")
        val hashMap: HashMap<String, RequestBody> = HashMap()


        val request_profile_status =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "1")
        val request_username = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            etVrockkId.text.toString()
        )
        val request_verified =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "true")

        val request_password = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            etPassword.text.toString()
        )
        val request_confirm_password = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            etPassword.text.toString()
        )
        val deviceType =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "android")
        val deviceToken = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            VrockkApplication.fcmToken
        )

        if (verificationWith == "phone") {
            val request_phone = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                etEmail.text.toString()
            )

            val request_country_code = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                "+" + edtSignupCountryCode.selectedCountryCode
            )
            hashMap["countryCode"] = request_country_code
            hashMap["phone"] = request_phone
            hashMap.put("isPhoneVerified", request_verified)
        }else{
            val request_email = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                etEmail.text.toString()
            )
            hashMap["email"] = request_email
            hashMap.put("isEmailVerified", request_verified)
        }

        hashMap["userName"] = request_username
        hashMap["profileStatus"] = request_profile_status
        hashMap["password"] = request_password
        hashMap["confirmPassword"] = request_confirm_password
        hashMap["deviceType"] = deviceType
        hashMap["deviceId"] = deviceToken
        profileSetupViewModel.hitprofileSetup(hashMap)
    }


    private fun allBtnClicks() {

        btnNext.setOnClickListener {
            if (verificationWith.equals("phone")) {
                if (etEmail.text.toString().equals("")) {
                    showSnackbar(getString(R.string.please_enter_phone_number))
                } else if (etEmail.text.toString().length < 7) {
                    showSnackbar("Please enter a valid phone number")
                } else {
                    performNextValidations()
                }
            } else {
                if (etEmail.text.toString().equals("")) {
                    showSnackbar(getString(R.string.please_enter_email))
                } else if (!isValidEmail(etEmail.text.toString())) {
                    showSnackbar(getString(R.string.please_enter_valid_email_address))
                } else {
                    performNextValidations()
                }
            }
        }

        loginWithPhoneBtn.setOnClickListener {
            changeSignupEntity()
        }

        tvLogin.setOnClickListener {
            finish()
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

        resendOtp.setOnClickListener {
            getOtp()
        }

    }

    private fun changePhoneEmailView() {
        if (isEmail) {
            edtSignupCountryCode.visibility = View.VISIBLE
            tvEmail.text = resources.getString(R.string.phone_)
            etEmail.setText("")
            etEmail.hint = resources.getString(R.string.enter_phone_number)
            loginWithPhoneBtn.text = resources.getString(R.string.signup_with_email)
            etEmail.setInputType(InputType.TYPE_CLASS_NUMBER);

            verificationWith = "phone"

            isEmail = false

        } else {
            edtSignupCountryCode.visibility = View.GONE
            tvEmail.text = resources.getString(R.string.email_)
            etEmail.setText("")
            etEmail.hint = resources.getString(R.string.enteremail)
            loginWithPhoneBtn.text = resources.getString(R.string.signup_with_phone)
            verificationWith = "email"

            isEmail = true

            etEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)

        }
    }

    private fun changeSignupEntity() {
        if (isOtpReceived || isOtpVerified) {
            val myCustomDlg = getDialog()
            myCustomDlg.show()

            myCustomDlg.positiveBtn.setOnClickListener {
                myCustomDlg.dismiss()
                isOtpReceived = false
                isOtpVerified = false
                btnNext.text = resources.getString(R.string.next_)
                clearPinview()
                otpContainer.visibility = View.GONE
                changeSignupEntity()
            }

            myCustomDlg.noBtn.setOnClickListener {
                myCustomDlg.dismiss()
            }
        } else {
            changePhoneEmailView()
        }
    }

    private fun doOnProcessReinitiate() {
        val myOnBackPressDlg = getDialog()
        myOnBackPressDlg.show()
        myOnBackPressDlg.positiveBtn.setOnClickListener {
            myOnBackPressDlg.dismiss()
            isOtpReceived = false
            isOtpVerified = false
            if (timer != null) timer!!.cancel()
            if (countDownTimer != null) countDownTimer!!.cancel()
            btnNext.text = resources.getString(R.string.next_)
            btnNext.isEnabled = true
            btnNext.background = resources.getDrawable(R.drawable.orange_bg, null)
            clearPinview()
            otpContainer.visibility = View.GONE
            loginWithPhoneBtn.visibility = View.VISIBLE
            etEmail.visibility = View.VISIBLE
            etDisabledEmailPhone.visibility = View.GONE
            etDisabledEmailPhone.setText("")
//                super.onBackPressed()
        }
        myOnBackPressDlg.noBtn.setOnClickListener {
            myOnBackPressDlg.dismiss()
        }
    }

    private fun customTextView(view: TextView) {
        val spanTxt: SpannableStringBuilder = SpannableStringBuilder(
            "I agree to the"
        );
        spanTxt.append(" Terms & Conditions");
        spanTxt.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                val i = Intent(this@SignUpV2Activity, Terms_Privacy_About_Activity::class.java)
                i.putExtra("type", "terms-and-conditions")
                startActivity(i)
            }
        }, spanTxt.length - "Terms & Conditions".length, spanTxt.length, 0);
        // spanTxt.setSpan(ForegroundColorSpan(Color.BLACK), 32, spanTxt.length, 0);
        view.movementMethod = LinkMovementMethod.getInstance();
        view.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }

    private fun showTimer() {
        countDownTimer = object : CountDownTimer(30000, 1000) {

            override fun onTick(millisUntilFinished: Long) {

                resendOtp.isEnabled = false
                resendOtp.setText("00:" + millisUntilFinished / 1000)
            }

            override fun onFinish() {
                resendOtp.isEnabled = true
                resendOtp.setText("" + resources.getString(R.string.resend_otp))
                btnNext.isEnabled = false
                btnNext.background =
                    resources.getDrawable(R.drawable.button_corner_gray_otp, null)
                if (timer != null) timer!!.cancel()

            }
        }
        countDownTimer!!.start()

    }

    private fun setButtonVisibility() {
        btnNext.text = resources.getString(R.string.verify_)
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    if (pinview.value.toString().length < 4) {
                        btnNext.isEnabled = false
                        btnNext.background =
                            resources.getDrawable(R.drawable.button_corner_gray_otp, null)
                    }

                    if (pinview.value.toString().length == 4) {
                        btnNext.isEnabled = true
                        btnNext.background = resources.getDrawable(R.drawable.orange_bg, null)
                    }
                }
            }
        }, 0, 200)
    }

    private fun getOtp() {
        isOtpReceived = false
        isOtpVerified = false

        clearPinview()
        loginWithPhoneBtn.visibility = View.GONE
        etEmail.visibility = View.GONE
        etDisabledEmailPhone.visibility = View.VISIBLE

        if (verificationWith.equals("phone")) {
            tvStatement.text = ("Enter 4 digit verification code\nsent to your number")
            etDisabledEmailPhone.setText(edtSignupCountryCode.selectedCountryCode + "-" + etEmail.text.toString())
            signUpViewModel.hitRegisterUser(
                null,
                etEmail.text.toString(),
                "+" + edtSignupCountryCode.selectedCountryCode
            )
        } else {
            tvStatement.text = ("Enter 4 digit verification code\nsent to your Email")
            etDisabledEmailPhone.setText(etEmail.text.toString())
            signUpViewModel.hitRegisterUser(etEmail.text.toString(), null, null)
        }
    }

    private fun verifyOtp() {
        if (verificationWith.equals("phone")) {
            otpVerficationViewModel.verifyOtp(
                null,
                pinview.value,
                etEmail.text.toString(),
                "+" + edtSignupCountryCode.selectedCountryCode
            )
        } else {
            otpVerficationViewModel.verifyOtp(
                etEmail.text.toString(),
                pinview.value,
                null,
                null
            )
        }
    }

    private fun clearPinview() {
        for (i in 0 until pinview.childCount) {
            val child: EditText = pinview.getChildAt(i) as EditText
            child.text.clear()
        }
    }

    private fun getDialog(): Dialog {
        val myCustomDlg = Dialog(this)
        myCustomDlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        myCustomDlg.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        myCustomDlg.setContentView(R.layout.my_custom_alert)
        myCustomDlg.tvAlertMessage.text =
            resources.getString(R.string.you_will_have_to_verify_otp_again)
        myCustomDlg.tvTitleCustom.text = resources.getString(R.string.confirm)
        myCustomDlg.positiveBtn.text = resources.getString(R.string.ok)
        myCustomDlg.noBtn.text = resources.getString(R.string.cancel)

        return myCustomDlg
    }
}