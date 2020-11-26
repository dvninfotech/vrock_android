package com.vrockk.view.following


import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.adapter.FollowersAdapter
import com.vrockk.api.ApiResponse
import com.vrockk.api.Status
import com.vrockk.base.BaseActivity
import com.vrockk.models.profile.followers_list.Data
import com.vrockk.models.profile.followers_list.FollowersResponse
import com.vrockk.utils.Utils
import com.vrockk.viewmodels.FollowUnfollowViewModel
import com.vrockk.viewmodels.GetFollowersViewModel
import kotlinx.android.synthetic.main.activity_like.*
import kotlinx.android.synthetic.main.activity_like.ibBack
import org.koin.android.viewmodel.ext.android.viewModel


class FollowersActivity : BaseActivity() {

    var pageCount:Int = 1
    var totalPages:Int = 1
    var pagesFromApi: Double = 0.00

    var `dataList`: ArrayList<Data>? = null
    var `dataListTemp`: ArrayList<Data>? = null

    lateinit var followingAdapter: FollowersAdapter
    private val getFollowersViewModel by viewModel<GetFollowersViewModel>()
    val followUnfollowViewModel by viewModel<FollowUnfollowViewModel>()
    private var profileId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_like)

        if(intent.hasExtra("profileId"))
            profileId = intent.getStringExtra("profileId")!!

        getFollowersViewModel.getFollowersResponse()
            .observe(this, Observer<ApiResponse> { response ->
                this.followingData(1,response)
            })


        followUnfollowViewModel.followUnfollowResponse().observe(this, Observer<ApiResponse> { response ->
            this.followingData(2,response)
        })

        init()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val search = s.toString()
                if (search.startsWith(" ")) {
                    etSearch.setText(search.trim { it <= ' ' })
                }else{

                    pageCount = 1
                    totalPages = 1
                    pagesFromApi = 0.00

                    `dataList` = null
                    `dataListTemp` = null

                    if(profileId.isEmpty())
                        getFollowersViewModel.getFollowers("SEC "+ VrockkApplication.user_obj!!.authToken, VrockkApplication.user_obj!!._id, VrockkApplication.user_obj!!._id,etSearch.text.toString(),pageCount,10)
                    else
                        getFollowersViewModel.getFollowers("SEC "+ VrockkApplication.user_obj!!.authToken,VrockkApplication.user_obj!!._id, profileId, etSearch.text.toString(),pageCount,10)
                }

            }
        })


        pagination()

    }

    override fun onResume() {
        super.onResume()

        pageCount = 1
        totalPages = 1
        pagesFromApi = 0.00

        `dataList` = null
        `dataListTemp` = null


        hitApi()
    }

    private fun hitApi() {
        if(profileId.isEmpty())
            getFollowersViewModel.getFollowers("SEC "+ VrockkApplication.user_obj!!.authToken, VrockkApplication.user_obj!!._id, VrockkApplication.user_obj!!._id,"",pageCount,10)
        else
            getFollowersViewModel.getFollowers("SEC "+ VrockkApplication.user_obj!!.authToken,VrockkApplication.user_obj!!._id, profileId, "",pageCount,10)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun followingData(type: Int,response: ApiResponse) {
        when (response.status) {
            Status.LOADING -> {
                if(etSearch.text.toString().isEmpty())
                    showProgress("")
            }
            Status.SUCCESS -> {
                hideProgress()
                renderResponse(type,response)
            }
            Status.ERROR -> {
                hideProgress()
                Log.e("login_error", Gson().toJson(response.error))
                //showCustomToastMessage(this, "Something went wrong!")
            }
        }
    }

    private fun renderResponse(type: Int, response: ApiResponse) {
        if (type == 1) {
            Log.e("login_data_response", Gson().toJson(response))
            val data: String = Utils.toJson(response.data)
            val gson1 = Gson();
            val getfollowingResonse = gson1.fromJson(data, FollowersResponse::class.java)

            if(getfollowingResonse.success)
            {

                pagesFromApi = getfollowingResonse.total / 10.0
                totalPages = Math.ceil(pagesFromApi).toInt()

                dataList = getfollowingResonse.data

                if (dataList!!.size > 0)
                {
                    etSearch.visibility = View.VISIBLE
                }

                Log.e("call","response total pages: "+totalPages)
                Log.e("call","response total: "+getfollowingResonse.total)
                Log.e("call","response  list size: "+dataList!!.size)

                if (dataListTemp!=null) {
                    dataListTemp!!.addAll(dataList!!)
                    rvLike.adapter!!.notifyDataSetChanged()
                }
                else {
                    dataListTemp = dataList
                    followingAdapter = FollowersAdapter(this,dataList!!)
                    rvLike.adapter = followingAdapter
                    rvLike.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
                }
            } else {
                showSnackbar(""+getfollowingResonse.message)
            }
        }else{



        }
    }


    fun init()
    {
        tvTitle.text = resources.getString(R.string.followers)

        ibBack.setOnClickListener { finish() }

    }

    fun followUnfollow(value : Boolean ,_id: String ,position : Int) {
        followUnfollowViewModel.followUnfollowPost("SEC "+VrockkApplication.user_obj!!.authToken,_id)
        dataListTemp!![position].isFollowing = value
        followingAdapter.notifyItemChanged(position)
    }


    fun pagination()
    {
        rvLike.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = LinearLayoutManager::class.java.cast(recyclerView.layoutManager)
                val totalItemCount = layoutManager!!.itemCount
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                val endHasBeenReached = lastVisible + 2 >= totalItemCount
                if (totalItemCount > 0 && endHasBeenReached) {
                    Log.e("last_item", "$lastVisible")
                    if (pageCount < totalPages) {
                        pageCount++
                        if(profileId.isEmpty())
                            getFollowersViewModel.getFollowers("SEC "+ VrockkApplication.user_obj!!.authToken, VrockkApplication.user_obj!!._id, VrockkApplication.user_obj!!._id,etSearch.text.toString(),pageCount,10)
                        else
                            getFollowersViewModel.getFollowers("SEC "+ VrockkApplication.user_obj!!.authToken,VrockkApplication.user_obj!!._id, profileId, etSearch.text.toString(),pageCount,10)
                    }
                }
            }
        })
    }



}
