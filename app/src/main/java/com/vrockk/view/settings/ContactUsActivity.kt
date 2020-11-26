package com.vrockk.view.settings

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.vrockk.R
import com.vrockk.api.ApiResponse
import com.vrockk.api.Status
import com.vrockk.base.BaseActivity
import com.vrockk.viewmodels.viewmodels.CommonViewModel
import kotlinx.android.synthetic.main.activity_contactus.*
import org.koin.android.viewmodel.ext.android.viewModel

class ContactUsActivity : BaseActivity() {

    private val commonViewModel by viewModel<CommonViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contactus)

        tvBack.setOnClickListener {
            finish()
        }

        tvSend.setOnClickListener {
            if(edtYourName.text.toString().isEmpty()){
                showSnackbar("Please enter your name")
            }else if(edtYourEmail.text.toString().isEmpty()){
                showSnackbar("Please enter your email address")
            }else if(etYourMessage.text.toString().isEmpty()){
                showSnackbar("Please write something")
            }else if (!isValidEmail(edtYourEmail.text.toString())) {
                 showSnackbar(resources.getString(R.string.please_enter_valid_email_address))
             }else{
                commonViewModel.contactUs(
                    edtYourName.text.toString(),
                    edtYourEmail.text.toString(),
                    etYourMessage.text.toString()
                )
            }
        }
        hitApi()
        observer()
    }

    private fun hitApi() {

    }

    private fun observer() {
        commonViewModel.contactUsResponseLiveData().observe(this, Observer<ApiResponse> { response ->
            this.dataResponse(1,response)
        })
    }
    private fun dataResponse(type:Int , response: ApiResponse) {
        when (response.status) {
            Status.LOADING -> {
                if (type == 1)
                    showProgress("")
            }
            Status.SUCCESS -> {
                if (type == 1)
                    hideProgress()

                renderResponse(type,response)
            }
            Status.ERROR -> {
                if (type == 1)
                    hideProgress()
                Log.e("Contact_us", Gson().toJson(response.error))
                //showCustomToastMessage(this, "Something went wrong!")
            }
        }
    }
    private fun renderResponse(type:Int ,response: ApiResponse) {
            Log.e("Contact_us: ", Gson().toJson(response))

            if (type == 1) {
                     showToast("Your message successfully sent")
                finish()
            }

    }
}