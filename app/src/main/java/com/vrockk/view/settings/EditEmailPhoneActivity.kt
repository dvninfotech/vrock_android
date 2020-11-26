package com.vrockk.view.signup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.vrockk.R
import com.vrockk.api.ApiResponse
import com.vrockk.api.Status
import com.vrockk.base.BaseActivity
import com.vrockk.models.signup.SignUpResponse
import com.vrockk.utils.Utils
import com.vrockk.view.login.LoginActivity
import com.vrockk.view.otpVerification.OtpVerificationUpdateActivity
import com.vrockk.viewmodels.SignUpViewModel
import kotlinx.android.synthetic.main.activity_edit_emailphone.*
import org.koin.android.viewmodel.ext.android.viewModel


class EditEmailPhoneActivity : BaseActivity() {

    var verificationWith: String = "email"
    val signUpViewModel by viewModel<SignUpViewModel>()
    var country_code: String = ""

    companion object {
        var instance: EditEmailPhoneActivity? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_emailphone)

        instance = this

        verificationWith = intent.getStringExtra("verificationWith")

        if (verificationWith.equals("email")) {
            tvHeaderName.setText("" + resources.getString(R.string.edit_email))
            var email: String = intent.getStringExtra("email")
            etEmail.setText("" + email)
        } else {
            tvHeaderName.setText("" + resources.getString(R.string.edit_phone_number))
            var phone: String = intent.getStringExtra("phone")
            etEmail.setText("" + phone)
            country_code = intent.getStringExtra("country_code")
            try {
                val countryCode = LoginActivity.user_country_code
                if (countryCode.contains("+"))
                    countryCode.replace("+", "")
                edtSignupCountryCode.setCountryForPhoneCode(countryCode.toInt())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        signUpViewModel.signupResponse().observe(this, Observer<ApiResponse> { response ->
            this.signupData(response)
        })

        allBtnClicks()
        btnValidation()

        tvBack.setOnClickListener {
            finish()
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
                Log.e("signup_error", Gson().toJson(response.error))
                //showCustomToastMessage(this, "Something went wrong!")
            }
        }
    }

    private fun renderResponse(response: ApiResponse) {
        if (response != null) {
            Log.e("signup_response: ", Gson().toJson(response))
            val data: String = Utils.toJson(response.data)
            val gson1 = Gson();
            val signupresponse = gson1.fromJson(data, SignUpResponse::class.java)

            if (signupresponse.success) {
                if (verificationWith.equals("email")) {
                    val i = Intent(this, OtpVerificationUpdateActivity::class.java)
                    i.putExtra("verificationWith", verificationWith)
                    i.putExtra("email", "" + etEmail.text.toString())
                    startActivity(i)
                } else {
                    val i = Intent(this, OtpVerificationUpdateActivity::class.java)
                    i.putExtra("verificationWith", verificationWith)
                    i.putExtra("phone", "" + etEmail.text.toString())
                    i.putExtra("country_code", "" + edtSignupCountryCode.selectedCountryCode)
                    startActivity(i)
                }
            } else {
                showSnackbar("" + signupresponse.message)
            }
        }
    }

    fun signUp() {

        if (verificationWith.equals("phone")) {
            LoginActivity.user_country_code = "" + edtSignupCountryCode.selectedCountryCode
            signUpViewModel.hitRegisterUser(
                null,
                etEmail.text.toString(),
                "+" + edtSignupCountryCode.selectedCountryCode
            )
        } else {
            signUpViewModel.hitRegisterUser(etEmail.text.toString(), null, null)
        }
    }

    private fun allBtnClicks() {

        tvUpdate.setOnClickListener {

            if (verificationWith.equals("phone")) {
                if (etEmail.text.toString().equals("")) {
                    showSnackbar(getString(R.string.please_enter_phone_number))
                } else if (etEmail.text.toString().toString().length < 7) {
                    showSnackbar("Please enter a valid phone number")
                } else {
                    signUp()
                }
            } else {
                if (etEmail.text.toString().equals("")) {
                    showSnackbar(getString(R.string.please_enter_email))
                } else if (!isValidEmail(etEmail.text.toString())) {
                    showSnackbar(getString(R.string.please_enter_valid_email_address))
                } else if (etEmail.text.toString().toString().length < 7) {
                    showSnackbar(resources.getString(R.string.you_must_have_eight_characters_in_your_phone))
                } else {
                    signUp()
                }
            }
        }


    }

    fun btnValidation() {
        if (verificationWith.equals("email")) {

            edtSignupCountryCode.visibility = View.GONE
            tvEmail.text = resources.getString(R.string.email)
            etEmail.hint = resources.getString(R.string.enteremail)
            etEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)

        } else {

            edtSignupCountryCode.visibility = View.VISIBLE
            tvEmail.text = resources.getString(R.string.phone)
            etEmail.hint = resources.getString(R.string.enter_phone_number)
            etEmail.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
    }
}