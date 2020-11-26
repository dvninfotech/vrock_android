package com.vrockk.view.search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.adapter.ProfileVideosAdapter
import com.vrockk.api.ApiResponse
import com.vrockk.api.Status
import com.vrockk.base.BaseActivity
import com.vrockk.interfaces.ItemClickListernerWithType
import com.vrockk.models.hashtags.DataItem
import com.vrockk.models.hashtags.HashTagsPostModel
import com.vrockk.utils.Utils
import com.vrockk.view.posts_play.PostsPlayAcivity
import com.vrockk.viewmodels.viewmodels.CommonViewModel
import kotlinx.android.synthetic.main.activity_search_hashtag.*
import kotlinx.android.synthetic.main.layout_loader.*
import org.koin.android.viewmodel.ext.android.viewModel
import kotlin.math.ceil

class SearchHashtagActivity : BaseActivity(), ItemClickListernerWithType {

    var pageCount: Int = 1
    var totalPages: Int = 1
    var pagesFromApi: Double = 0.00

    private var hashTagsList = ArrayList<DataItem>()
    // private var hashTagsListTemp  = ArrayList<DataItem>()

    lateinit var profileVideosAdapter: ProfileVideosAdapter
    private var hashTag = ""
    private var parentPosition = 0
    private val commonViewModel by viewModel<CommonViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_hashtag)

        initProgress(contentLoadingProgressBar)

        textViewHeader.visibility = View.VISIBLE
        profile_image.visibility = View.INVISIBLE

        if (intent.hasExtra("hashTag"))
            hashTag = intent.getStringExtra("hashTag")!!

        tvHashTagName.text = hashTag

        if (intent.hasExtra("position"))
            parentPosition = intent.getIntExtra("position", 0)

        init()
        setUpRecyclerviews()
        observerApi()
        hitApi()

        pagination()
    }

    private fun hitApi() {
        commonViewModel.getHashTags(1, 10, hashTag, "",
            VrockkApplication.user_obj?._id ?: "")
    }

    fun init() {
        //hideStatusBar()
        ibBack.setOnClickListener { finish() }
    }

    private fun setUpRecyclerviews() {
        rvHashtag.setHasFixedSize(true)
        rvHashtag.layoutManager = GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false)
        profileVideosAdapter = ProfileVideosAdapter(this, hashTagsList, this)
        rvHashtag.adapter = profileVideosAdapter
    }

    private fun observerApi() {
        commonViewModel.hashTagsResponseLiveData()
            .observe(this, Observer<ApiResponse> { response ->
                this.setData(response)
            })
    }

    private fun setData(response: ApiResponse?) {
        when (response!!.status) {
            Status.LOADING -> {
                if (pageCount == 1)
                    showProgress("")
            }
            Status.SUCCESS -> {
                hideProgress()
                renderResponse(response)
            }
            Status.ERROR -> {
                hideProgress()
                Log.e("response", Gson().toJson(response.error))
                //showCustomToastMessage(this, "Something went wrong!")
            }
        }
    }

    private fun renderResponse(response: ApiResponse) {
        val data: String = Utils.toJson(response.data)
        val gson1 = Gson()
        val model = gson1.fromJson(data, HashTagsPostModel::class.java)

        if (model.success!!) {
            pagesFromApi = model?.total!! / 10.0
            totalPages = ceil(pagesFromApi).toInt()
            Log.e("call", "response total pages: " + totalPages)
            Log.e("call", "response total: " + model.total)
            Log.e("call", "response list size: " + hashTagsList.size)

            if (hashTagsList.size == 0) {
                hashTagsList.clear()
                hashTagsList.addAll(model.data!!)
                //   hashTagsListTemp.addAll(hashTagsList)
                profileVideosAdapter.notifyDataSetChanged()
            } else {
                hashTagsList.addAll(model.data!!)
                //hashTagsListTemp.addAll(hashTagsList)
                profileVideosAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onItemClicked(position: Int, type: String) {
        startActivity(
            Intent(this, PostsPlayAcivity::class.java)
                .putExtra("hashTag", hashTag)
                .putExtra("position", position)
                .putExtra("postId", hashTagsList[position].id)
        )
    }


    fun pagination() {
        nestedScrollview.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            val nestedScrollView = checkNotNull(v) {
                return@setOnScrollChangeListener
            }
            val lastChild = nestedScrollView.getChildAt(nestedScrollView.childCount - 1)
            if (lastChild != null) {
                if ((scrollY >= (lastChild.measuredHeight - nestedScrollView.measuredHeight)) && scrollY > oldScrollY) {
                    pageCount++
                    commonViewModel.getHashTags(
                        pageCount,
                        10,
                        hashTag,
                        "",
                        VrockkApplication.user_obj?._id ?: ""
                    )
                }
            }
        }
    }


}
