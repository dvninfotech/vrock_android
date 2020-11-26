package com.vrockk.view.settings

import android.os.Bundle
import android.util.Log
import androidx.core.text.HtmlCompat
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.vrockk.R
import com.vrockk.adapter.SettingsAdapter
import com.vrockk.api.ApiResponse
import com.vrockk.api.Status
import com.vrockk.base.BaseActivity
import com.vrockk.models.static_pages.StaticAllPagesResponse
import com.vrockk.utils.Utils
import com.vrockk.viewmodels.GetStaticPagesViewModel
import kotlinx.android.synthetic.main.activity_terms_privacy_about.*
import kotlinx.android.synthetic.main.layout_loader.*
import org.koin.android.viewmodel.ext.android.viewModel

class Terms_Privacy_About_Activity : BaseActivity()
{

    var type:String = ""

    private val getStaticPagesViewModel by viewModel<GetStaticPagesViewModel>()

    lateinit var settingsAdapter : SettingsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_privacy_about)

        initProgress(contentLoadingProgressBar)
        type = intent.getStringExtra("type")?:""

        when (type) {
            "terms-and-conditions" -> {
                tvTitle.text = resources.getString(R.string.termsandcondition)
            }
            "privacy-policy" -> {
                tvTitle.text = resources.getString(R.string.privacypolicy)
            }
            else -> {
                tvTitle.text = resources.getString(R.string.aboutapp)
            }
        }

        getStaticPagesViewModel.getStaticPagesResponse().observe(this, Observer<ApiResponse> {
                response -> this.staticAllData(response)
        })

        getStaticPagesViewModel.getStaticPages(getMyAuthToken(), 1,10)
        handleClickEvents()
    }


    private fun staticAllData(response: ApiResponse) {
        when (response.status) {
            Status.LOADING -> {
                showProgress()
            }
            Status.SUCCESS -> {
                hideProgress()
                renderResponse(response)
            }
            Status.ERROR -> {
                hideProgress()
                Log.e("login_error", Gson().toJson(response.error))
            }
        }
    }

    /*POST http://52.14.41.120:3090/api/user/commentPost
    Authorization: SEC eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI1ZjkwNmU2OWNmZmM5MWE1MGVlOD
    FhYjciLCJpYXQiOjE2MDQzNTY5MDcsImV4cCI6MTYxOTkwODkwNywiYXVkIjoiYWJjNTg5NCIsImlzcyI6ImFwcHR1bml4
    In0.c8vB48kLUiStaaJbkQXrUBRxJD_UovdAj8kXFyKJF1YadrSLFQ8des_IiSWIsphce9OGofCsnzYM7L2xJl1X_Q
    postId=5f72326d92de7f21fe0ed03a&comment=nicr%20one

    {"success":true,"message":"Comment posted","data":{"status":1,"_id":"5fa08da3f28e9a95407cfa6f",
        "user":{"location":{"type":"Point","coordinates":[85.9204401,20.5458136]},"firstName":"Dayal",
            "lastName":"Das","purchasedCoins":1500,"giftCoins":0,"profilePic":"","provider":"",
            "providerId":"","instagram":"","profileStatus":1,"level":0,"isEmailVerified":false,
            "isPhoneVerified":true,"isSocialRegister":false,"isDeleted":false,"roles":"user","facebook":"",
            "youtube":"","_id":"5f906e69cffc91a50ee81ab7","address":"","gender":"","latitude":20.5458136,
            "bio":"","userName":"junund","phone":"7377164612","countryCode":"+91","dob":0,"referralCode":"",
            "email":"","longitude":85.9204401,"referingCode":"368973458MK","createdAt":"2020-10-21T17:22:49.701Z",
            "updatedAt":"2020-11-02T22:41:47.339Z","__v":0,"id":"5f906e69cffc91a50ee81ab7"},
        "post":{"location":{"type":"Point","coordinates":[0,0]},"song":"5f7f3053b2b40887006cd934",
            "description":"","hashtags":["#desiboy #odiaRocker #odisha #odiacomedy #rajudas #GangOfShree
            #trending #foryou #WeWillRock  #vRockk #vRockkIndia #comedy "],"views":0,"isDeleted":false,
            "isUploaded":false,"status":1,"songExtractStatus":1,"_id":"5f72326d92de7f21fe0ed03a",
            "userId":"5f6eb096b17c9c24405e4182","originalName":"rajudastiktoker6823877899276406018.mp4",
            "size":13326770,"type":"video/mp4","post":"https://media.vrockk.mobi/india/videos/1601319532120-video.mp4",
            "s3LinkPost":"https://vrockkindia.s3.ap-south-1.amazonaws.com/india/videos/1601319532120-video.mp4",
            "thumbnail":"https://media.vrockk.mobi/india/thumbnails/1601319533095-rajudastiktoker6823877899276406018.png",
            "createdAt":"2020-09-28T18:58:53.168Z","updatedAt":"2020-11-02T12:41:29.756Z","__v":0,
            "totalComments":0,"totalLikes":0,"likes":[],"comments":[]},"comment":"nicr one","replies":[],
        "createdAt":"2020-11-02T22:52:19.555Z","updatedAt":"2020-11-02T22:52:19.555Z","__v":0}}

    POST http://52.14.41.120:3090/api/user/getComments
    Authorization: SEC eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI1ZjkwNmU2OWNmZmM5MWE1MGVlODFh
    YjciLCJpYXQiOjE2MDQzNTY5MDcsImV4cCI6MTYxOTkwODkwNywiYXVkIjoiYWJjNTg5NCIsImlzcyI6ImFwcHR1bml4In0
    .c8vB48kLUiStaaJbkQXrUBRxJD_UovdAj8kXFyKJF1YadrSLFQ8des_IiSWIsphce9OGofCsnzYM7L2xJl1X_Q
    page=1&count=100&postId=5f72326d92de7f21fe0ed03a
    {"success":true,"message":"Successful","data":[],"total":0}*/

    private fun renderResponse(response: ApiResponse) {
        val data: String = Utils.toJson(response.data)
        Log.e("staticAllData response: ", data)
        val gson1 = Gson()

        try{
            val staticAllPagesResponse = gson1.fromJson(data, StaticAllPagesResponse::class.java)
            Log.e("call","description: "+ staticAllPagesResponse.data[0].description)

            for(i in staticAllPagesResponse.data.indices)
            {
               if (type == staticAllPagesResponse.data[i].slugName)
               {
                   tvDescription.text = HtmlCompat.fromHtml(staticAllPagesResponse.data[i].description, 0)
               }
            }

        }catch (e : Exception){
            Log.e("Exception: ", e.toString())
        }

    }

    private fun handleClickEvents() {
        tvBack.setOnClickListener {
            finish()
        }
    }

}