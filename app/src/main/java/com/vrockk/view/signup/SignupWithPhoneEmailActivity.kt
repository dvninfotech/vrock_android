package com.vrockk.view.signup


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
import com.vrockk.api.Status
import com.vrockk.base.BaseActivity
import com.vrockk.models.signup.SignUpResponse
import com.vrockk.utils.Utils
import com.vrockk.view.login.LoginActivity
import com.vrockk.view.otpVerification.OtpVerificationActivity
import com.vrockk.viewmodels.SignUpViewModel
import kotlinx.android.synthetic.main.activity_signup_with_phoneemail.*
import kotlinx.android.synthetic.main.activity_signup_with_phoneemail.btnNext
import kotlinx.android.synthetic.main.activity_signup_with_phoneemail.edtSignupCountryCode
import kotlinx.android.synthetic.main.activity_signup_with_phoneemail.etEmail
import kotlinx.android.synthetic.main.activity_signup_with_phoneemail.tvEmail
import okhttp3.MediaType
import okhttp3.RequestBody
import org.koin.android.viewmodel.ext.android.viewModel


class SignupWithPhoneEmailActivity : BaseActivity(){

    var verificationWith:String = "email"
    var isEmail:Boolean = true
    val signUpViewModel by viewModel<SignUpViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_with_phoneemail)

        signUpViewModel.signupResponse().observe(this, Observer<ApiResponse> { response ->
            this.signupData(response)
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
        if(response != null){
            Log.e("signup_response: ", Gson().toJson(response))
            val data: String = Utils.toJson(response.data)
            val gson1 = Gson();
            val signupresponse = gson1.fromJson(data, SignUpResponse::class.java)

            if (signupresponse.success)
            {
                LoginActivity.user_email_phone = etEmail.text.toString()

                val i = Intent(this, OtpVerificationActivity::class.java)
                i.putExtra("fromClass","signup")
                i.putExtra("verificationWith",verificationWith)
                startActivity(i)
            }
            else
            {
                showSnackbar(""+signupresponse.message)
            }
        }
    }

    fun signUp(){

        if(verificationWith.equals("phone"))
        {
            LoginActivity.user_country_code = ""+edtSignupCountryCode.selectedCountryCode
            signUpViewModel.hitRegisterUser(null,etEmail.text.toString(),"+"+edtSignupCountryCode.selectedCountryCode)
        }
        else
        {
            signUpViewModel.hitRegisterUser(etEmail.text.toString(),null,null)
        }
    }





    private fun allBtnClicks() {

        btnNext.setOnClickListener {

            if(verificationWith.equals("phone"))
            {
                if (etEmail.text.toString().equals(""))
                {
                    showSnackbar(getString(R.string.please_enter_phone_number))
                }
                else if(etEmail.text.toString().toString().length < 7)
                {
                    showSnackbar("Please enter a valid phone number")
                }
                else
                {
                    signUp()
                }
            }
            else
            {
                if (etEmail.text.toString().equals(""))
                {
                    showSnackbar(getString(R.string.please_enter_email))
                }
                else if (!isValidEmail(etEmail.text.toString()))
                {
                    showSnackbar(getString(R.string.please_enter_valid_email_address))
                }
                else if(etEmail.text.toString().toString().length < 7)
                {
                    showSnackbar(resources.getString(R.string.you_must_have_eight_characters_in_your_phone))
                }
                else
                {
                    signUp()
                }
            }
        }

        loginWithPhoneBtn.setOnClickListener {
            btnValidation()
        }

        tvLogin.setOnClickListener {
            finish()
        }

    }

    fun  btnValidation()
    {
        if (isEmail) {
            edtSignupCountryCode.visibility = View.VISIBLE
            tvEmail.text = resources.getString(R.string.phone)
            etEmail.setText("")
            etEmail.hint = resources.getString(R.string.enter_phone_number)
            loginWithPhoneBtn.text = resources.getString(R.string.signup_with_email)
            etEmail.setInputType(InputType.TYPE_CLASS_NUMBER);

            verificationWith = "phone"

            isEmail = false

        } else {
            edtSignupCountryCode.visibility = View.GONE
            tvEmail.text = resources.getString(R.string.email)
            etEmail.setText("")
            etEmail.hint = resources.getString(R.string.enteremail)
            loginWithPhoneBtn.text = resources.getString(R.string.signup_with_phone)
            verificationWith = "email"

            isEmail = true

            etEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)

        }
    }
}