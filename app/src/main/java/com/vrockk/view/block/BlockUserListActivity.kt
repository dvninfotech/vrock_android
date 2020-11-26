package com.vrockk.view.following


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.adapter.BlockListAdapter
import com.vrockk.api.ApiResponse
import com.vrockk.api.Status
import com.vrockk.base.BaseActivity
import com.vrockk.models.block.blocklistuser.BlockListUserResponse

import com.vrockk.utils.Utils
import com.vrockk.viewmodels.UserBlockListViewModel
import com.vrockk.viewmodels.UserBlockViewModel
import kotlinx.android.synthetic.main.activity_like.*
import org.koin.android.viewmodel.ext.android.viewModel

class BlockUserListActivity : BaseActivity() {

    lateinit var blockListAdapter: BlockListAdapter

    private val userBlockListViewModel by viewModel<UserBlockListViewModel>()

    val userBlockViewModel by viewModel<UserBlockViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_like)
        etSearch.visibility = View.GONE

        userBlockListViewModel.userBlockListResponse()
            .observe(this, Observer<ApiResponse> { response ->
                this.responseData(1,response)
            })

        userBlockViewModel.userBlockResponse()
            .observe(this, Observer<ApiResponse> { response ->
                this.responseData(2,response)
            })


        init()

        userBlockListViewModel.userBlockList("SEC " +VrockkApplication.user_obj!!.authToken)

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun responseData(type:Int,response: ApiResponse) {
        when (response.status) {
            Status.LOADING -> {
                showProgress("")
            }
            Status.SUCCESS -> {
                hideProgress()
                renderResponse(type,response)
            }
            Status.ERROR -> {
                hideProgress()
                Log.e("login_error", Gson().toJson(response.error))
            }
        }
    }

    private fun renderResponse(type:Int,response: ApiResponse) {

        if (response != null) {

            if (type == 1) {
                Log.e("login_data_response", Gson().toJson(response))
                val data: String = Utils.toJson(response.data)
                val gson1 = Gson();
                val blockListUserResponse = gson1.fromJson(data, BlockListUserResponse::class.java)

                if(blockListUserResponse.success) {
                    blockListAdapter = BlockListAdapter(this, blockListUserResponse.data)
                    rvLike.adapter = blockListAdapter
                    rvLike.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)

                }
                else
                {
                    showSnackbar(""+blockListUserResponse.message)
                }

            }else if(type == 2) {
                userBlockListViewModel.userBlockList("SEC "+VrockkApplication.user_obj!!.authToken)

            }

        }
    }


    fun init() {
        tvTitle.setText("Block List")

        ibBack.setOnClickListener { finish() }
    }

    fun blockUnblock(_id: String)
    {
        userBlockViewModel.userBlock("SEC "+VrockkApplication.user_obj!!.authToken,_id)
    }


}
