package com.vrockk.view.settings

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.api.ApiResponse
import com.vrockk.api.Status
import com.vrockk.api.parsers.ProfileParser
import com.vrockk.base.BaseActivity
import com.vrockk.models.login.Data
import com.vrockk.utils.PreferenceHelper
import com.vrockk.utils.Utils
import com.vrockk.viewmodels.ProfilePageViewModel
import kotlinx.android.synthetic.main.activity_viewprofile.*
import org.koin.android.viewmodel.ext.android.viewModel

class ViewProfileActivity : BaseActivity() {

    val profilePageViewModel by viewModel<ProfilePageViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewprofile)

        profilePageViewModel.profilePageResponse().observe(this, Observer<ApiResponse> { response ->
            this.dataResponse(1, response)
        })

        tvBack.setOnClickListener {
            finish()
        }

        tvEdit.setOnClickListener {
            navigate(UpdateProfileActivity::class.java)
        }

        ivSettings.setOnClickListener {
            navigate(SettingsActivity::class.java)
        }
    }


    private fun dataResponse(type: Int, response: ApiResponse) {
        when (response.status) {
            Status.LOADING -> {
                showProgress("")
            }
            Status.SUCCESS -> {

                hideProgress()

                renderResponse(type, response)
            }
            Status.ERROR -> {

                hideProgress()
                Log.e("home_error", Gson().toJson(response.error))
                //showCustomToastMessage(this, "Something went wrong!")
            }
        }
    }

    private fun renderResponse(type: Int, response: ApiResponse) {
        if (response != null) {
            Log.e("home_response: ", Gson().toJson(response))

            if (type == 1) {
                val data: String = Utils.toJson(response.data)
                val gson1 = Gson();

                try {
//                    val profilePageResponse = gson1.fromJson(data, ProfilePageResponse::class.java)
                    val profilePageResponse = ProfileParser.parseResponse(response.data)
                    if (profilePageResponse != null && profilePageResponse.success) {
                        var profileImageUrl: String =
                            profilePageResponse.data.profile.get(0).profilePic
                        Glide.with(this).load(profileImageUrl)
                            .placeholder(resources.getDrawable(R.drawable.user_placeholder))
                            .error(resources.getDrawable(R.drawable.user_placeholder))
                            .into(profileSetupImg!!)
                        tvFirstNameValue.setText("" + profilePageResponse.data.profile.get(0).firstName)
                        tvLastNameValue.setText("" + profilePageResponse.data.profile.get(0).lastName)

                        if (!profilePageResponse.data.profile[0].userName.equals("") || profilePageResponse.data.profile[0].userName != null
                        ){
                            tvUserName.setText("@" + profilePageResponse.data.profile.get(0).userName)
                        }


                        tvBioValue.setText("" + profilePageResponse.data.profile.get(0).bio)
                        if(profilePageResponse.data.profile[0].phone.isNotEmpty()){
                            tvPhoneValue.setText(
                                profilePageResponse.data.profile[0].countryCode + "-" + profilePageResponse.data.profile[0].phone
                            )
                        }

                        tvEmailValue.setText("" + profilePageResponse.data.profile.get(0).email)
                        tvAddressValue.setText("" + profilePageResponse.data.profile.get(0).address)
                        tvGenderValue.setText("" + profilePageResponse.data.profile.get(0).gender)
                        tvFacebookValue.setText("" + profilePageResponse.data.profile.get(0).facebook)
                        tvInstagramValue.setText("" + profilePageResponse.data.profile.get(0).instagram)
                        tvYoutubeValue.setText("" + profilePageResponse.data.profile.get(0).youtube)
                        //tvInstagramValue.setText(""+ VrockkApplication.user_obj!!.instagram)


                        val json = Gson().toJson(
                            Data(
                                profilePageResponse.data.profile.get(0).__v,
                                profilePageResponse.data.profile.get(0)._id,
                                VrockkApplication.user_obj!!.authToken,
                                profilePageResponse.data.profile.get(0).bio,
                                profilePageResponse.data.profile.get(0).countryCode,
                                profilePageResponse.data.profile.get(0).createdAt,
                                profilePageResponse.data.profile.get(0).dob,
                                profilePageResponse.data.profile.get(0).email,
                                profilePageResponse.data.profile.get(0).firstName,
                                VrockkApplication.user_obj!!.id,
                                profilePageResponse.data.profile.get(0).instagram,
                                profilePageResponse.data.profile.get(0).isEmailVerified,
                                profilePageResponse.data.profile.get(0).isPhoneVerified,
                                profilePageResponse.data.profile.get(0).lastName,
                                profilePageResponse.data.profile.get(0).phone,
                                profilePageResponse.data.profile.get(0).profilePic,
                                profilePageResponse.data.profile.get(0).profileStatus,
                                profilePageResponse.data.profile.get(0).provider,
                                profilePageResponse.data.profile.get(0).providerId,
                                profilePageResponse.data.profile.get(0).referralCode,
                                profilePageResponse.data.profile.get(0).updatedAt,
                                profilePageResponse.data.profile.get(0).userName,
                                VrockkApplication.user_obj!!.loginType,
                                profilePageResponse.data.profile.get(0).address,
                                profilePageResponse.data.profile.get(0).gender,
                                0,
                                profilePageResponse.data.profile.get(0).facebook,
                                profilePageResponse.data.profile.get(0).youtube
                            )
                        )

                        //**  save data in shared prefrences class

                        val editor = VrockkApplication.prefs.edit()

                        VrockkApplication.user_obj =
                            VrockkApplication.gson!!.fromJson(
                                json.toString(),
                                Data::class.java
                            ) as Data
                        editor.putString(PreferenceHelper.Key.REGISTEREDUSER, json)
                        editor.commit()


                    }
                } catch (e: Exception) {
                    Log.e("call", "exception: " + e.toString())
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        profilePageViewModel.profilePagePost(
            "SEC " + VrockkApplication.user_obj!!.authToken,
            1,
            1,
            ""
        )
        scrollView.fullScroll(View.FOCUS_UP);
    }


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

}