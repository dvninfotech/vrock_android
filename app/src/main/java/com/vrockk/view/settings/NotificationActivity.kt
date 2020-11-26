package com.vrockk.view.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.adapter.NotificationAdapter
import com.vrockk.api.ApiResponse
import com.vrockk.api.Status
import com.vrockk.base.BaseActivity
import com.vrockk.models.notification.NotificationResponse
import com.vrockk.utils.Utils
import com.vrockk.viewmodels.NotificatioViewModel
import kotlinx.android.synthetic.main.activity_notification.*
import org.koin.android.viewmodel.ext.android.viewModel
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.vrockk.interfaces.ItemClickListernerWithType
import com.vrockk.models.notification.Data
import com.vrockk.view.comment.CommentActivity
import com.vrockk.view.home.HomeFragment
import com.vrockk.view.posts_play.PostsPlayAcivity
import com.vrockk.view.profile.OtherProfileActivity
import kotlinx.android.synthetic.main.activity_notification.rvNotification
import kotlin.math.ceil


class NotificationActivity : BaseActivity() ,ItemClickListernerWithType {

    var pageCount:Int = 1
    var totalPages:Int = 1
    var pagesFromApi: Double = 0.00
    var `dataList`: ArrayList<Data>? = null
    var `dataListTemp`: ArrayList<Data>? = null

    lateinit var notificationAdapter: NotificationAdapter

    private val notificatioViewModel by viewModel<NotificatioViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        notificatioViewModel.notificationResponse().observe(this, Observer<ApiResponse> {
                response -> this.notificationData(response)
        })

        notificatioViewModel.notification("SEC "+VrockkApplication.user_obj!!.authToken,pageCount,15)

        pagination()

        tvBack.setOnClickListener {
            finish()
        }

    }

    private fun notificationData(response: ApiResponse) {
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
        val data: String = Utils.toJson(response.data)
        Log.e("notification response: ", data)
        val gson1 = Gson()

        try{
            val notificationResponse = gson1.fromJson(data, NotificationResponse::class.java)


            pagesFromApi = notificationResponse.total / 10.0
            totalPages = ceil(pagesFromApi).toInt()
            dataList = notificationResponse.data

            Log.e("call", "response total pages: $totalPages")
            Log.e("call","response total: "+notificationResponse.total)
            Log.e("call","response  list size: "+dataList!!.size)

            if (dataListTemp!=null)
            {
                dataListTemp!!.addAll(dataList!!)
                rvNotification.adapter!!.notifyDataSetChanged()
            }
            else
            {
                dataListTemp = dataList
                Log.e("call","notification data:   "+notificationResponse.data!!.toString())
                notificationAdapter = NotificationAdapter(this,dataListTemp!!,this)
                rvNotification.adapter = notificationAdapter
                rvNotification.setHasFixedSize(true)
                rvNotification.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            }

        }catch (e : Exception){
            Log.e("Exception: ", e.toString())
        }
    }

    fun pagination()
    {
        rvNotification.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                        notificatioViewModel.notification("SEC "+VrockkApplication.user_obj!!.authToken,pageCount,10)
                    }
                }
            }
        })
    }

    override fun onItemClicked(position: Int, type: String) {
         var notiType = type.toInt()

//         if(notiType == 1){
//             if(VrockkApplication.user_obj != null){
//                 HomeFragment.otherUserProfileId = ""+ dataListTemp!![position].user?._id
//                 val i = Intent(this, OtherProfileActivity::class.java)
//                 i.putExtra("_id",dataListTemp!![position].user?._id)
//                 i.putExtra("position",position)
//                 startActivityForResult(i,30)
//             }else{
//                 showLoginPopup()
//             }
//         }else if(notiType == 2){
//             if(VrockkApplication.user_obj != null){
//                 HomeFragment.otherUserProfileId = ""+ dataListTemp!![position].user?._id
//                 val i = Intent(this, OtherProfileActivity::class.java)
//                 i.putExtra("_id",dataListTemp!![position].user?._id)
//                 i.putExtra("position",position)
//                 startActivityForResult(i,30)
//             }else{
//                 showLoginPopup()
//             }
//         }else if(notiType == 3){
//             if(VrockkApplication.user_obj != null){
//                 val i = Intent(this, CommentActivity::class.java)
//                 i.putExtra("_id",dataListTemp!![position].notiData.post)
//                 Log.e("position"," $position ")
//                 i.putExtra("position",position)
//                 startActivityForResult(i,20)
//             }else{
//                 showLoginPopup()
//             }
//         }else if(notiType == 4){
//             val ii = Intent(this, PostsPlayAcivity::class.java)
//             //   intent!!.putExtra("_id",postObj.getString("_id"))
//             ii.putExtra("postId",dataListTemp!![position].notiData._id)
//             ii.putExtra("notification","")
//             startActivity(ii)
//         }
//         else if(notiType == 5){
//             if(VrockkApplication.user_obj != null){
//                 HomeFragment.otherUserProfileId = ""+ dataListTemp!![position].user?._id
//                 val i = Intent(this, OtherProfileActivity::class.java)
//                 i.putExtra("_id",dataListTemp!![position].user?._id)
//                 i.putExtra("position",position)
//                 startActivityForResult(i,30)
//             }else{
//                showLoginPopup()
//             }
//         }

        if(notiType == 1){ //started following
            if(VrockkApplication.user_obj != null){
                HomeFragment.otherUserProfileId = ""+ dataListTemp!![position].user?._id
                val i = Intent(this, OtherProfileActivity::class.java)
                i.putExtra("_id",dataListTemp!![position].user?._id)
                i.putExtra("position",position)
                startActivityForResult(i,30)
            }else{
                (this as BaseActivity).showLoginPopup()
            }
        }else if(notiType == 2){ // liked
//            if(VrockkApplication.user_obj != null){
//                HomeFragment.otherUserProfileId = ""+ dataListTemp!![position].user?._id
//                val i = Intent(activity, OtherProfileActivity::class.java)
//                i.putExtra("_id",dataListTemp!![position].user?._id)
//                i.putExtra("position",position)
//                startActivityForResult(i,30)
//            }else{
//                (activity as BaseActivity).showLoginPopup()
//            }
            val ii = Intent(this, PostsPlayAcivity::class.java)
            //   intent!!.putExtra("_id",postObj.getString("_id"))
            ii.putExtra("postId",dataListTemp!![position].notiData.post)
            ii.putExtra("notification","")
            startActivityForResult(ii, 30)
        }else if(notiType == 3){ // commented
            if(VrockkApplication.user_obj != null){
                val i = Intent(this, CommentActivity::class.java)
                i.putExtra("_id",dataListTemp!![position].notiData.post)
                Log.e("position"," $position ")
                i.putExtra("position",position)
                startActivityForResult(i,20)
            }else{
                (this as BaseActivity).showLoginPopup()
            }
        }else if(notiType == 4){ // posted
            val ii = Intent(this, PostsPlayAcivity::class.java)
            //   intent!!.putExtra("_id",postObj.getString("_id"))
            ii.putExtra("postId",dataListTemp!![position].notiData._id)
            ii.putExtra("notification","")
            startActivity(ii)
        }
        else if(notiType == 5){ //
            if(VrockkApplication.user_obj != null){
                HomeFragment.otherUserProfileId = ""+ dataListTemp!![position].user?._id
                val i = Intent(this, OtherProfileActivity::class.java)
                i.putExtra("_id",dataListTemp!![position].user?._id)
                i.putExtra("position",position)
                startActivityForResult(i,30)
            }else{
                (this as BaseActivity).showLoginPopup()
            }
        }
        else if(notiType == 7){ //
            if(VrockkApplication.user_obj != null){
                HomeFragment.otherUserProfileId = ""+ dataListTemp!![position].user?._id
                val i = Intent(this, OtherProfileActivity::class.java)
                i.putExtra("_id",dataListTemp!![position].user?._id)
                i.putExtra("position",position)
                startActivityForResult(i,30)
            }else{
                (this as BaseActivity).showLoginPopup()
            }
        }
    }
}