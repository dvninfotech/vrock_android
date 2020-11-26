package com.vrockk.view.forgotPassword

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.vrockk.R
import com.vrockk.api.ApiResponse
import com.vrockk.api.LOGIN
import com.vrockk.api.Status
import com.vrockk.base.BaseActivity
import com.vrockk.models.signup.SignUpResponse
import com.vrockk.utils.Utils
import com.vrockk.view.home.HomeActivity
import com.vrockk.view.login.LoginActivity
import com.vrockk.view.otpVerification.OtpVerificationActivity
import com.vrockk.viewmodels.ForgotPasswordViewModel
import com.vrockk.viewmodels.SignUpViewModel
import kotlinx.android.synthetic.main.activity_forgot_password.*
import kotlinx.android.synthetic.main.activity_forgot_password.btnNext
import kotlinx.android.synthetic.main.activity_forgot_password.clEmail
import kotlinx.android.synthetic.main.activity_forgot_password.edtSignupCountryCode
import kotlinx.android.synthetic.main.activity_forgot_password.etEmail
import kotlinx.android.synthetic.main.activity_forgot_password.tvEmail
import org.koin.android.viewmodel.ext.android.viewModel

class ForgotPasswordActivity : BaseActivity() {

    var isEmail:Boolean = true

    var verificationWith: String = "email"

    val forgotPasswordViewModel by viewModel<ForgotPasswordViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        verificationWith = intent.getStringExtra("verificationWith")

        forgotPasswordViewModel.forgotPasswordResponse().observe(this, Observer<ApiResponse> { response ->
            this.dataResponse(response)
        })

        allBtnClicks()
        btnValidation()

    }


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun dataResponse(response: ApiResponse) {
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
        if(response != null){
            Log.e("signup_response: ", Gson().toJson(response))
            val data: String = Utils.toJson(response.data)
            val gson1 = Gson();
            val signupresponse = gson1.fromJson(data, SignUpResponse::class.java)

            if (signupresponse.success)
            {
                LoginActivity.user_email_phone = etEmail.text.toString()

                val i = Intent(this, OtpVerificationActivity::class.java)
                i.putExtra("fromClass","forgot")
                i.putExtra("verificationWith",verificationWith)
                startActivity(i)

                etEmail.setText("")
            }
            else
            {
                showSnackbar(""+signupresponse.message)
            }
        }
    }

    private fun allBtnClicks() {
        resetWithPhoneBtn.setOnClickListener {
          //  btnVisibility()
            btnValidation()
        }

        resetWithPhoneBtn.setOnClickListener {
            btnValidation()
        }


        btnNext.setOnClickListener {

            Log.e("call","visibility status: "+verificationWith)

            if(verificationWith.equals("phone"))
            {
                if (etEmail.text.toString().equals(""))
                {
                    showSnackbar(""+resources.getString(R.string.please_enter_phone_number))
                }
                else if(etEmail.text.toString().toString().length < 7)
                {
                    showSnackbar("Please enter a valid phone number")
                }
                else
                {

                    Log.e("call","phone: "+etEmail.text.toString())
                    Log.e("call","country code: "+etEmail.text.toString())

                    LoginActivity.user_country_code = ""+edtSignupCountryCode.selectedCountryCode
                    forgotPasswordViewModel.hitForgotPassword(null,etEmail.text.toString(),"+"+edtSignupCountryCode.selectedCountryCode)
                }
            }
            else
            {
                if (etEmail.text.toString().equals(""))
                {
                    showSnackbar(""+resources.getString(R.string.please_enter_email))
                }
                else if (!isValidEmail(etEmail.text.toString()))
                {
                    showSnackbar(getString(R.string.please_enter_valid_email_address))
                }
                else
                {
                    forgotPasswordViewModel.hitForgotPassword(etEmail.text.toString(),null,null)
                }
            }
        }

        clBack.setOnClickListener {
            finish()
        }

    }

    fun  btnValidation()
    {
        if (isEmail) {

            edtSignupCountryCode.visibility = View.VISIBLE

            tvEmail.setText(resources.getString(R.string.phone))
            etEmail.setText("")
            etEmail.hint = resources.getString(R.string.enter_phone_number)
            resetWithPhoneBtn.text = "RESET USING EMAIL"

            verificationWith = "phone"

            tvForgotDescription.setText("Enter the phone number you used to create your account and we will send you a otp to reset your password")
            etEmail.setInputType(InputType.TYPE_CLASS_NUMBER);
            isEmail = false

        } else {


            edtSignupCountryCode.visibility = View.GONE
            resetWithPhoneBtn.text = "RESET USING PHONE NUMBER"
            tvEmail.setText(resources.getString(R.string.email))
            etEmail.setText("")
            etEmail.hint = resources.getString(R.string.enter_email)
            verificationWith = "email"

            tvForgotDescription.setText("Enter the email address you used to create your account and we will email you a link to reset your password")

            etEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            isEmail = true


        }
    }

}
