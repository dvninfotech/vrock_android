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
import com.vrockk.view.dashboard.DashboardActivity
import com.vrockk.view.home.HomeActivity
import com.vrockk.view.login.LoginActivity
import com.vrockk.view.resetPassword.ResetPasswordActivity
import com.vrockk.view.signup.SignUpActivity
import com.vrockk.viewmodels.*
import kotlinx.android.synthetic.main.activity_otp_verification.*
import kotlinx.android.synthetic.main.activity_sign_up.clBack
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*

class OtpVerificationActivity : BaseActivity() {

    var classType:String = ""
    var verificationWith:String = ""
    val otpVerficationViewModel by viewModel<EmailOtpVerficationViewModel>()

    val forgotPasswordViewModel by viewModel<ForgotPasswordViewModel>()
    val signUpViewModel by viewModel<SignUpViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification)

        showTimer()

        otpVerficationViewModel.verifyOtpResponse().observe(this, Observer<ApiResponse> { response ->
            this.otpData(1,response)
        })

        forgotPasswordViewModel.forgotPasswordResponse().observe(this, Observer<ApiResponse> { response ->
            this.otpData(2,response)
        })

        signUpViewModel.signupResponse().observe(this, Observer<ApiResponse> { response ->
            this.otpData(3,response)
        })

        classType = intent.getStringExtra("fromClass")
        verificationWith = intent.getStringExtra("verificationWith")

        if (verificationWith.equals("email"))
        {
            tvStatement.setText("Enter 4 digit verification code\nsent to your Email")
            tvPhone.setText(LoginActivity.user_email_phone)
        }
        else
        {
            tvStatement.setText("Enter 4 digit verification code\nsent to your number")
            tvPhone.setText("+"+LoginActivity.user_country_code+" "+LoginActivity.user_email_phone)
        }

        allBtnClicks()
        initBlock()
        setButtonVisibility()


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
            Log.e("otp_data_response", Gson().toJson(response))
            val data: String = Utils.toJson(response.data)
            val gson1 = Gson();
            val verifyOtpResponse = gson1.fromJson(data, VerifyOtpResponse::class.java)

            Log.e(
                "call",
                "otp_data_response " + verifyOtpResponse.success + " " + verifyOtpResponse.message
            )

            if (type == 1)
            {
                if (verifyOtpResponse.success)
                {
                    if (classType.equals("forgot"))
                    {
                        val i = Intent(this, ResetPasswordActivity::class.java)
                        i.putExtra("verificationWith",verificationWith)
                        startActivity(i)

                        finish()

                    }
                    else if (classType.equals("login"))
                    {
                        val i = Intent(this, DashboardActivity::class.java)
                        startActivity(i)

                        finish()

                    }
                    else
                    {
                        val i = Intent(this, SignUpActivity::class.java)
                        i.putExtra("isSocailLogin",false)
                        i.putExtra("verificationWith",verificationWith)
                        startActivity(i)

                        finish()

                    }
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

        if (classType.equals("forgot"))
        {
            if (verificationWith.equals("phone"))
            {
                forgotPasswordViewModel.hitForgotPassword(null,LoginActivity.user_email_phone,"+"+LoginActivity.user_country_code)
            }
            else
            {
                forgotPasswordViewModel.hitForgotPassword(LoginActivity.user_email_phone,null,null)
            }
        }
        else if (classType.equals("signup"))
        {
            if (verificationWith.equals("phone"))
            {
                signUpViewModel.hitRegisterUser(null,LoginActivity.user_email_phone,"+"+LoginActivity.user_country_code)
            }
            else
            {
                signUpViewModel.hitRegisterUser(LoginActivity.user_email_phone,null,null)
            }
        }
        else
        {

        }
    }

    private fun initBlock() {
        pinview.setTextColor(Color.WHITE)
        if (pinview.value.length == 4){
//            DoneBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#e50914"))
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
                otpVerficationViewModel.verifyOtp(null,pinview.value,LoginActivity.user_email_phone,"+"+LoginActivity.user_country_code)
            }
            else
            {
                otpVerficationViewModel.verifyOtp(LoginActivity.user_email_phone,pinview.value,null,null)

            }
        }

        resendOtp.setOnClickListener {
            callResendOtp()
        }
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