package com.vrockk.view.resetPassword

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
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
import com.vrockk.viewmodels.ForgotPasswordViewModel
import com.vrockk.viewmodels.ResetPasswordViewModel
import kotlinx.android.synthetic.main.activity_forgot_password.*
import kotlinx.android.synthetic.main.activity_reset_password.*
import kotlinx.android.synthetic.main.activity_reset_password.tvBack
import org.koin.android.viewmodel.ext.android.viewModel

class ResetPasswordActivity : BaseActivity() {

    val resetPasswordViewModel by viewModel<ResetPasswordViewModel>()

    var verificationWith:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        verificationWith = intent.getStringExtra("verificationWith")

        resetPasswordViewModel.ResetPasswordResponse().observe(this, Observer<ApiResponse> { response ->
            this.dataResponse(response)
        })

        tvBack.setOnClickListener {
            finish()
        }

        tvSave.setOnClickListener {

            if (edtNewPassword.text.toString().equals(""))
            {
                showSnackbar("Please enter new password")
            }
            else if(edtNewPassword.text.toString().toString().length < 7)
            {
                showSnackbar(resources.getString(R.string.you_must_have_eight_characters_in_your_new_password))
            }
            else if (etConfirmPassword.text.toString().equals(""))
            {
                showSnackbar("Please enter confirm password")
            }
            else if(etConfirmPassword.text.toString().toString().length < 7)
            {
                showSnackbar(resources.getString(R.string.you_must_have_eight_characters_in_your_confirm_password))
            }
            else if (!etConfirmPassword.text.toString().equals(edtNewPassword.text.toString()))
            {
                showSnackbar(getString(R.string.confrim_password_not_match))
            }
            else
            {

                if (verificationWith.equals("phone"))
                {
                    resetPasswordViewModel.hitResetPassword(edtNewPassword.text.toString(),null,LoginActivity.user_email_phone,"+"+LoginActivity.user_country_code)
                }
                else
                {
                    resetPasswordViewModel.hitResetPassword(edtNewPassword.text.toString(),LoginActivity.user_email_phone,null,null)
                }
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
                val i = Intent(this, LoginActivity::class.java)
                startActivity(i)
            }
            else
            {
                showSnackbar(""+signupresponse.message)
            }
        }
    }

}