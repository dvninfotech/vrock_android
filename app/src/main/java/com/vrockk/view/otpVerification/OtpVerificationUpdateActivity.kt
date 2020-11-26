package com.vrockk.view.otpVerification

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.vrockk.R
import com.vrockk.api.ApiResponse
import com.vrockk.api.LOGIN
import com.vrockk.api.Status
import com.vrockk.base.BaseActivity
import com.vrockk.models.verify.VerifyOtpResponse
import com.vrockk.utils.Utils
import com.vrockk.view.home.HomeActivity
import com.vrockk.view.login.LoginActivity
import com.vrockk.view.resetPassword.ResetPasswordActivity
import com.vrockk.view.settings.UpdateProfileActivity
import com.vrockk.view.signup.EditEmailPhoneActivity
import com.vrockk.view.signup.SignUpActivity
import com.vrockk.viewmodels.*
import kotlinx.android.synthetic.main.activity_edit_emailphone.*
import kotlinx.android.synthetic.main.activity_otp_verification.*
import kotlinx.android.synthetic.main.activity_sign_up.clBack
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*

class OtpVerificationUpdateActivity : BaseActivity() {

    var classType:String = ""
    var verificationWith:String = ""
    val otpVerficationViewModel by viewModel<EmailOtpVerficationViewModel>()

    val signUpViewModel by viewModel<SignUpViewModel>()

    var country_code:String = ""
    var email_phone:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification)

        verificationWith = intent.getStringExtra("verificationWith")

        showTimer()

        otpVerficationViewModel.verifyOtpResponse().observe(this, Observer<ApiResponse> { response ->
            this.otpData(1,response)
        })


        signUpViewModel.signupResponse().observe(this, Observer<ApiResponse> { response ->
            this.otpData(3,response)
        })


        if (verificationWith.equals("email"))
        {
            email_phone =  intent.getStringExtra("email")
            tvStatement.setText("Enter 4 digit verification code\nsent to your Email")
            tvPhone.setText(email_phone)
        }
        else
        {
            email_phone =  intent.getStringExtra("phone")
            country_code = intent.getStringExtra("country_code")
            tvStatement.setText("Enter 4 digit verification code\nsent to your number")
            tvPhone.setText("+"+country_code+" "+email_phone)
        }

        allBtnClicks()
        initBlock()
        setButtonVisibility()

    }

    fun showTimer()
    {
        object : CountDownTimer(30000, 1000) {

            override fun onTick(millisUntilFinished: Long) {

                resendOtp.isEnabled = false
                resendOtp.setText("00:" + millisUntilFinished / 1000)
            }

            override fun onFinish() {
                resendOtp.isEnabled = true
                resendOtp.setText(""+resources.getString(R.string.resend_otp))
            }
        }.start()
    }


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun otpData(type:Int,response: ApiResponse) {
        when (response.status) {
            Status.LOADING -> {
                showProgress("")
            }
            Status.SUCCESS -> {
                hideProgress()
                renderResponse(type,response)
            }
            Status.ERROR -> {
                hideProgress()
                Log.e("otp_error", Gson().toJson(response.error))
                //showCustomToastMessage(this, "Something went wrong!")
            }
        }
    }

    private fun renderResponse(type:Int,response: ApiResponse) {

        if(response != null) {
            val data: String = Utils.toJson(response.data)
            val gson1 = Gson();
            val verifyOtpResponse = gson1.fromJson(data, VerifyOtpResponse::class.java)

            if (type == 1)
            {
                if (verifyOtpResponse.success)
                {
                    if (verificationWith.equals("email"))
                    {
                        UpdateProfileActivity.email_update = email_phone
                    }
                    else
                    {
                        UpdateProfileActivity.phone_update = email_phone
                        UpdateProfileActivity.countrycode_update = "+"+country_code
                    }

                    finish()
                    EditEmailPhoneActivity.instance!!.finish()
                }
                else
                {
                    showSnackbar(verifyOtpResponse.message)
                }
            }
            else
            {
                showTimer()
                showSnackbar(verifyOtpResponse.message)
            }

        }
    }

    fun callResendOtp()
    {
        if (verificationWith.equals("phone"))
        {
            signUpViewModel.hitRegisterUser(null,email_phone,"+"+country_code)
        }
        else
        {
            signUpViewModel.hitRegisterUser(email_phone,null,null)
        }
    }

    private fun initBlock() {
        pinview.setTextColor(Color.WHITE)
        if (pinview.value.length == 4){
        }
    }

    private fun allBtnClicks() {

        clBack.setOnClickListener {
            finish()
        }

        btnDone.setOnClickListener {

            Log.e("call","pin value:  "+pinview.value)

            if (verificationWith.equals("phone"))
            {
                otpVerficationViewModel.verifyOtp(null,pinview.value,email_phone,"+"+country_code)
            }
            else
            {
                otpVerficationViewModel.verifyOtp(email_phone,pinview.value,null,null)

            }
        }

        resendOtp.setOnClickListener {
            callResendOtp()
        }
    }


    fun setButtonVisibility()
    {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {  if (pinview.value.toString().length < 4){
                    btnDone.isEnabled = false
                    btnDone.background = resources.getDrawable(R.drawable.button_corner_gray_otp)
                }

                    if(pinview.value.toString().length == 4)
                    {
                        btnDone.isEnabled = true
                        btnDone.background = resources.getDrawable(R.drawable.round_store_red)
                    }
                }
            }
        }, 0, 200)
    }


}