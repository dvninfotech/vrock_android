package com.vrockk.view.settings

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.api.ApiResponse
import com.vrockk.api.Status
import com.vrockk.base.BaseActivity
import com.vrockk.models.change_password.ChangePasswordResponse
import com.vrockk.utils.Utils
import com.vrockk.viewmodels.ChangePasswordViewModel
import kotlinx.android.synthetic.main.activity_changepassword.*
import kotlinx.android.synthetic.main.activity_login.*
import org.koin.android.viewmodel.ext.android.viewModel

class ChangePasswordActivity : BaseActivity() {

    private val changePasswordViewModel by viewModel<ChangePasswordViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_changepassword)

        changePasswordViewModel.changePasswordResponse()
            .observe(this, Observer<ApiResponse> { response ->
                this.otpData(response)
            })

        tvBack.setOnClickListener {
            finish()
        }

        tvSave.setOnClickListener {
            if (etCurrentPassword.text.toString().equals(""))
            {
                showSnackbar(getString(R.string.please_enter_current_password))
            }
            else if (etNewPassword.text.toString().equals(""))
            {
                showSnackbar(getString(R.string.please_enter_new_password))
            }
            else if(etNewPassword.text.toString().toString().length < 7)
            {
                showSnackbar(resources.getString(R.string.you_must_have_eight_characters_in_your_new_password))
            }
            else if (etCurrentPassword.text.toString().equals(etNewPassword.text.toString()))
            {
                showSnackbar(getString(R.string.current_password_and_new_password_should_not_be_same))
            }
            else if(etCurrentPassword.text.toString().toString().length < 7)
            {
                showSnackbar(resources.getString(R.string.you_must_have_eight_characters_in_your_current_password))
            }
            else if (etConfirmPassword.text.toString().equals(""))
            {
                showSnackbar(getString(R.string.please_enter_confirm_password))
            }
            else if (!etConfirmPassword.text.toString().equals(etNewPassword.text.toString()))
            {
                showSnackbar(getString(R.string.confrim_password_not_match))
            }
            else
            {
                callChangePassword()
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

    private fun otpData(response: ApiResponse) {
        when (response.status) {
            Status.LOADING -> {
                showProgress("")
            }
            Status.SUCCESS -> {
                hideProgress()
                renderResponse(response)
            }
            Status.ERROR -> {
                hideProgress()
                Log.e("login_error", Gson().toJson(response.error))
                //showCustomToastMessage(this, "Something went wrong!")
            }
        }
    }

    private fun renderResponse(response: ApiResponse) {
        if (response != null) {
            Log.e("login_data_response", Gson().toJson(response))
            val data: String = Utils.toJson(response.data)
            val gson1 = Gson();
            val changePasswordResponse = gson1.fromJson(data, ChangePasswordResponse::class.java)

            if(changePasswordResponse.success)
            {

                val sharedPreference_remember =  getSharedPreferences("RembemberMe", Context.MODE_PRIVATE)
                var editor_remember = sharedPreference_remember.edit()
                editor_remember.putString("PASSWORD",etNewPassword.text.toString())
                editor_remember.commit()

                etCurrentPassword.setText("")
                etNewPassword.setText("")
                etConfirmPassword.setText("")

                showSnackbar(""+changePasswordResponse.message)
            }
            else
            {
                showSnackbar(""+changePasswordResponse.message)
            }

        }
    }

    fun callChangePassword()
    {
        var token:String = ""
        token = "SEC "+VrockkApplication.user_obj!!.authToken
        changePasswordViewModel.changePassword(
            token,
            etCurrentPassword.text.toString(),
            etNewPassword.text.toString(),
            etConfirmPassword.text.toString())
    }

}