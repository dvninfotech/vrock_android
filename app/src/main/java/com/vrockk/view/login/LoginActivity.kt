package com.vrockk.view.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Observer
import com.facebook.*
import com.facebook.login.LoginBehavior
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.api.ApiResponse
import com.vrockk.api.Status
import com.vrockk.base.BaseActivity
import com.vrockk.models.login.Data
import com.vrockk.models.login.LoginResponse
import com.vrockk.models.login.SocailLoginRequest
import com.vrockk.utils.PreferenceHelper
import com.vrockk.utils.Utils
import com.vrockk.view.dashboard.DashboardActivity
import com.vrockk.view.forgotPassword.ForgotPasswordActivity
import com.vrockk.view.signup.SignUpV2Activity
import com.vrockk.view.signup.SignupWithPhoneEmailActivity
import com.vrockk.view.signup.SocailProfileUpdateActivity
import com.vrockk.viewmodels.LoginViewModel
import com.vrockk.viewmodels.SocailLoginViewModel
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.layout_loader.*
import org.json.JSONObject
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*

class LoginActivity : BaseActivity() {

    //Google Login Request Code
    private val RC_SIGN_IN = 101
    var mAuth: FirebaseAuth? = null
    var callbackManager: CallbackManager? = null
    var facebookToken: String? = ""
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    var verificationWith: String = "email"
    val loginViewModel by viewModel<LoginViewModel>()
    val socailLoginViewModel by viewModel<SocailLoginViewModel>()
    var isEmail: Boolean = true


    companion object {
        var user_email_phone = ""
        var user_country_code = ""
        var socail_first_name = ""
        var socail_last_name = ""
        var socail_email = ""
        var social_user_profile: Uri? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initProgress(contentLoadingProgressBar)

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("DeviceToken", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }
                VrockkApplication.fcmToken = task.result?.token!!
                Log.e("DeviceToken", "getInstanceId :- ${VrockkApplication.fcmToken}")
            })

        callbackManager = CallbackManager.Factory.create()
        mAuth = FirebaseAuth.getInstance()

        loginViewModel.loginResponse().observe(this, Observer<ApiResponse> { response ->
            this.otpData(1, response)
        })

        socailLoginViewModel.socailLoginResponse().observe(this, Observer<ApiResponse> { response ->
            this.otpData(2, response)
        })

        allBtnClicks()
        btnValidation()

        checkRememberMe()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }


    private fun otpData(type: Int, response: ApiResponse) {
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
                if (Utils.isConnectedToNetwork(this))
                    showSnackbar("Not connected to internet")
                else
                    showSnackbar("Something went wrong")
            }
        }
    }

    private fun renderResponse(type: Int, response: ApiResponse) {
        Log.e("login_data_response", Gson().toJson(response))

        val data: String = Utils.toJson(response.data)
        val gson1 = Gson();

        val dataJSONObject = JSONObject(data)

        var loginData = Data()
        if (dataJSONObject.has("data"))
            loginData = parseLoginData(dataJSONObject.optJSONObject("data"))

        val loginResponse = LoginResponse(
            loginData,
            dataJSONObject.optString("message"),
            dataJSONObject.optBoolean("success")
        )
//            val loginResponse = gson1.fromJson(data, LoginResponse::class.java)

        if (loginResponse.success) {
            if (type == 1) {
                val json = Gson().toJson(
                    /*Data(
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
                    )*/
                    loginData
                )

                //**  save data in shared prefrences class
                val editor = VrockkApplication.prefs.edit()

                VrockkApplication.user_obj =
                    VrockkApplication.gson!!.fromJson(json.toString(), Data::class.java) as Data
                editor.putString(PreferenceHelper.Key.REGISTEREDUSER, json)
                editor.apply()

//                    Log.e("call","111111 token: "+VrockkApplication.fcmToken)

                navigateFinishAffinity(DashboardActivity::class.java)

                val sharedPreference = getSharedPreferences("RembemberMe", Context.MODE_PRIVATE)
                val editor_ = sharedPreference.edit()

                Log.e("call", "verification: $verificationWith")

                if (verificationWith == "email") {
                    editor_.putString("email", etEmail.text.toString())
                    editor_.putString("PASSWORD", etPassword.text.toString())
                } else {
                    editor_.putString("countrycode", edtSignupCountryCode.selectedCountryCode)
                    editor_.putString("phone", etPhone.text.toString())
                    editor_.putString("PASSWORD", etPassword.text.toString())
                }

                editor_.apply()

            }


            ////**** google signout
            if (type == 2) {
                Log.e("call", "profile status: " + loginResponse.data.profileStatus)

                if (loginResponse.data.profileStatus == 0) {
                    LoginActivity.user_email_phone = socail_email

//                    val i = Intent(this, SocailProfileUpdateActivity::class.java)
                    val i = Intent(this, SignUpV2Activity::class.java)
//                    i.putExtra("token", loginResponse.data.authToken)
                    i.putExtra("isSocialLogin", true)
                    i.putExtra("socialEmail", socail_email)
                    startActivity(i)
                } else {

                    val json = Gson().toJson(
                        /*Data(
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
                        )*/
                        loginData
                    )

                    //**  save data in shared prefrences class

                    val editor = VrockkApplication.prefs.edit()

                    VrockkApplication.user_obj = VrockkApplication.gson!!.fromJson(
                        json.toString(),
                        Data::class.java
                    ) as Data
                    editor.putString(PreferenceHelper.Key.REGISTEREDUSER, json)
                    editor.apply()

                    navigateFinishAffinity(DashboardActivity::class.java)
                }

                if (loginResponse.data.provider == "Google")
                    mGoogleSignInClient.signOut()
            }
        } else {
            showSnackbar("" + loginResponse.message)
        }
    }

    private fun parseLoginData(jsonObject: JSONObject?): Data {
        return Data(
            jsonObject!!.optInt("__v"),
            jsonObject.optString("_id"),
            jsonObject.optString("authToken"),
            jsonObject.optString("bio"),
            jsonObject.optString("countryCode"),
            jsonObject.optString("createdAt"),
            jsonObject.optString("dob"),
            jsonObject.optString("email"),
            jsonObject.optString("firstName"),
            jsonObject.optString("id"),
            jsonObject.optString("instagram"),
            jsonObject.optBoolean("isEmailVerified"),
            jsonObject.optBoolean("isPhoneVerified"),
            jsonObject.optString("lastName"),
            jsonObject.optString("phone"),
            jsonObject.optString("profilePic"),
            jsonObject.optInt("profileStatus"),
            jsonObject.optString("provider"),
            jsonObject.optString("providerId"),
            jsonObject.optString("referralCode"),
            jsonObject.optString("updatedAt"),
            jsonObject.optString("userName"),
            jsonObject.optString("loginType"),
            jsonObject.optString("address"),
            jsonObject.optString("gender"),
            jsonObject.optInt("level"),
            jsonObject.optString("facebook"),
            jsonObject.optString("youtube")
        )
    }

    @SuppressLint("SetTextI18n")
    private fun checkRememberMe() {
        val sharedPreference = getSharedPreferences("RembemberMe", Context.MODE_PRIVATE)

        if (verificationWith.equals("email")) {
            val email: String? = sharedPreference.getString("email", "")
            val password: String? = sharedPreference.getString("PASSWORD", "")
            etEmail.setText("" + email)
            etPassword.setText("" + password)
        } else {
            val phone: String? = sharedPreference.getString("phone", "")
            val countrycode: String? = sharedPreference.getString("countrycode", "91")
            val password: String? = sharedPreference.getString("PASSWORD", "")
            etPhone.setText("" + phone)
            etPassword.setText("" + password)
            try {
                val countryCode = LoginActivity.user_country_code
                if (countryCode.contains("+"))
                    countryCode.replace("+", "")
                edtSignupCountryCode.setCountryForPhoneCode(countryCode.toInt())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }


    private fun callLogin() {
        LoginActivity.user_email_phone = etEmail.text.toString()
        LoginActivity.user_country_code = "" + edtSignupCountryCode.selectedCountryCode

        if (verificationWith == "phone") {
            if (etPhone.text.toString() == "") {
                showSnackbar(resources.getString(R.string.please_enter_phone_number))
            } else if (etPhone.text.toString().toString().length < 7) {
                showSnackbar("Please enter a valid phone number")
            } else if (etPassword.text.toString() == "") {
                showSnackbar(resources.getString(R.string.please_enter_password))
            } else {


                loginViewModel.login(
                    null, etPassword.text.toString(), etPhone.text.toString(),
                    "+" + edtSignupCountryCode.selectedCountryCode,
                    "android",
                    VrockkApplication.fcmToken
                )
            }
        } else {
            if (etEmail.text.toString() == "") {
                showSnackbar(resources.getString(R.string.please_enter_email))
            } else if (!isValidEmail(etEmail.text.toString())) {
                showSnackbar(resources.getString(R.string.please_enter_valid_email_address))
            } else if (etPassword.text.toString() == "") {
                showSnackbar(resources.getString(R.string.please_enter_password))
            } else {
                loginViewModel.login(
                    etEmail.text.toString(), etPassword.text.toString(), null, null,
                    "android",
                    VrockkApplication.fcmToken
                )
            }
        }
    }

    private fun allBtnClicks() {

        ibGoogle.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build()
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
            signIn()
        }

        ibFB.setOnClickListener {
            facebookSignIn()
        }

        loginWithPhoneBtn.setOnClickListener {
            btnValidation()
        }
        clRegisterNow.setOnClickListener {

//            val i = Intent(this, SignupWithPhoneEmailActivity::class.java)
//            i.putExtra("verificationWith", verificationWith)
//            startActivity(i)

            val i = Intent(this, SignUpV2Activity::class.java)
            startActivity(i)
        }

        loginBtn.setOnClickListener {

            callLogin()
        }

        tvForgot.setOnClickListener {

            val i = Intent(this, ForgotPasswordActivity::class.java)
            i.putExtra("verificationWith", verificationWith)
            startActivity(i)
        }
    }

    fun btnValidation() {
        if (isEmail) {

            tvEmail.visibility = View.INVISIBLE
            clEmail.visibility = View.INVISIBLE

            tvPhone.visibility = View.VISIBLE
            clPhone.visibility = View.VISIBLE

            loginBtn.text = "LOGIN"
            loginWithPhoneBtn.text = "LOGIN WITH EMAIL"
            verificationWith = "phone"
            //      etEmail.setInputType(InputType.TYPE_CLASS_NUMBER);

            isEmail = false

            checkRememberMe()

        } else {
            tvEmail.visibility = View.VISIBLE
            clEmail.visibility = View.VISIBLE

            tvPhone.visibility = View.INVISIBLE
            clPhone.visibility = View.INVISIBLE

            loginBtn.text = "LOGIN"
            loginWithPhoneBtn.text = "LOGIN WITH PHONE NUMBER"
            verificationWith = "email"

            isEmail = true

            checkRememberMe()

        }
    }

    //SignInMethod for Google
    private fun signIn() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null)
            firebaseAuthWithGoogle(account)
        else {
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //SignInMethod for Google
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            task.addOnCompleteListener {
                if (it.isSuccessful)
                    handleGoogleSignin(it)
//                else
//                    Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        } else {
            callbackManager!!.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleGoogleSignin(task: Task<GoogleSignInAccount>?) {
        try {
            // Google Sign In was successful, authenticate with Firebase
            val account = task?.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account!!)
            Log.e("call", "GoogleLogin Successfully Working")
        } catch (e: ApiException) {
            Log.w("GoogleLogin", "Google sign in failed", e)
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {

        ////****   google data ....

        socail_first_name = account.givenName.toString()
        socail_last_name = account.familyName.toString()
        socail_email = account.email.toString()
        social_user_profile = account.photoUrl

        Log.e("call", "token: " + VrockkApplication.fcmToken)

        ////  call api .

        socailLoginViewModel.socail_login(
            SocailLoginRequest(
                VrockkApplication.fcmToken,
                "Android",
                account.email.toString(),
                account.givenName.toString(),
                account.familyName.toString(),
                "Google",
                account.id.toString()
            )
        )
    }


    ////**** for facebook ....

    private fun facebookSignIn() {
        LoginManager.getInstance().loginBehavior = LoginBehavior.WEB_ONLY
        LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList("email", "public_profile"))
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    Log.e("Facebook............", " id : " + result?.accessToken?.token)
                    facebookToken = result?.accessToken?.token
                    //added new
                    val graph: GraphRequest = GraphRequest.newMeRequest(
                        AccessToken.getCurrentAccessToken()
                    ) { `object`, response ->
                        Log.e("data of fb ", " all data facebook $`object` response  $response")
                        getFacebookData(`object`!!)
                    }
                    val bundle: Bundle = Bundle()
                    bundle.putString("fields", "id,first_name,last_name,email,gender")
                    graph.parameters = bundle
                    graph.executeAsync()

                }

                override fun onCancel() {
                    Log.d(TAG, "Login attempt cancelled.")
                }

                override fun onError(error: FacebookException?) {
                    Log.d(TAG, "Login attempt failed.")
                    Log.w("FacebookLogin", "failed error$error")
                }
            })
    }

    private fun getFacebookData(jsonObject: JSONObject) {

        val id: String = jsonObject.getString("id")
        val pic: String = "https://graph.facebook.com/$id/picture?type=large"
        val firstName: String = jsonObject.getString("first_name")
        val lastName: String = jsonObject.getString("last_name")


        var email: String = ""
        var gender: String = ""
        if (jsonObject.has("email"))
            email = jsonObject.getString("email")


        ////  call api .

        socail_first_name = firstName
        socail_last_name = lastName
        socail_email = email


        Log.e("call", "token: " + VrockkApplication.fcmToken)

        socailLoginViewModel.socail_login(
            SocailLoginRequest(
                VrockkApplication.fcmToken,
                "Android",
                email,
                firstName,
                lastName,
                "Facebook",
                id
            )
        )
    }

}