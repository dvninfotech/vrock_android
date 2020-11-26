package com.vrockk.view.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.adapter.NotificationAdapter
import com.vrockk.api.ApiResponse
import com.vrockk.api.Status
import com.vrockk.base.BaseActivity
import com.vrockk.base.BaseFragment
import com.vrockk.interfaces.ItemClickListernerWithType
import com.vrockk.models.notification.Data
import com.vrockk.models.notification.NotificationResponse
import com.vrockk.utils.Utils
import com.vrockk.view.comment.CommentActivity
import com.vrockk.view.home.HomeFragment
import com.vrockk.view.posts_play.PostsPlayAcivity
import com.vrockk.view.profile.OtherProfileActivity
import com.vrockk.viewmodels.NotificatioViewModel
import kotlinx.android.synthetic.main.fragment_notification.*
import kotlinx.android.synthetic.main.layout_loader.*
import org.koin.android.viewmodel.ext.android.viewModel


class NotificationFragment : BaseFragment(), ItemClickListernerWithType {
    companion object {
        const val TAG = "NotificationFragment"
    }

    var page: Int = 1
    var totalPages: Int = 1
    var pagesFromApi: Double = 0.00

    var dataList: ArrayList<Data>? = null
    var dataListTemp: ArrayList<Data>? = null

    var isprogress: Boolean = true

    private val notificatioViewModel by viewModel<NotificatioViewModel>()

    var layoutManager: LinearLayoutManager? = null
    lateinit var notificationAdapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutManager = LinearLayoutManager(activity!!, LinearLayoutManager.VERTICAL, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initProgress(contentLoadingProgressBar)

        srlNotifications.setOnRefreshListener {
            notificatioViewModel.notification(
                "SEC " + VrockkApplication.user_obj!!.authToken,
                page,
                10
            )
        }

        notificatioViewModel.notificationResponse()
            .observe(activity!!, Observer<ApiResponse> { response ->
                this.notificationData(response)
            })
        addScrollListener()
    }

    private fun addScrollListener() {
        rvNotification.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val totalItemCount = layoutManager?.itemCount ?: 0
                val lastVisible = layoutManager?.findLastVisibleItemPosition() ?: 0
                val endHasBeenReached = lastVisible + 2 >= totalItemCount

                if (totalItemCount > 0 && endHasBeenReached) {
                    if (page < totalPages) {
                        page++
                        notificatioViewModel.notification(
                            /*"SEC " + VrockkApplication.user_obj!!.authToken*/ getMyAuthToken(),
                            page,
                            10
                        )
                    }
                }
            }
        })
    }

    override fun onTabSwitched() {
        if (dataList != null && dataList!!.size == 0) {
            notificatioViewModel.notification(
                "SEC " + VrockkApplication.user_obj!!.authToken,
                page,
                10
            )
        }
    }

    override fun onResume() {
        super.onResume()

        if (dataList != null && dataList!!.isEmpty()) {
            showProgress()
        }
        notificatioViewModel.notification(
            "SEC " + VrockkApplication.user_obj!!.authToken,
            page,
            10
        )
    }

    private fun notificationData(response: ApiResponse) {
        when (response.status) {
            Status.LOADING -> {
                if (isprogress)
                    showProgress("")
            }
            Status.SUCCESS -> {
                srlNotifications.isRefreshing = false
                hideProgress()
                renderResponse(response)
            }
            Status.ERROR -> {
                srlNotifications.isRefreshing = false
                hideProgress()
            }
        }
    }

    private fun renderResponse(response: ApiResponse) {
        isprogress = false

        val data: String = Utils.toJson(response.data)
        val gson1 = Gson()

        try {
            val notificationResponse = gson1.fromJson(data, NotificationResponse::class.java)

            pagesFromApi = notificationResponse.total / 10.0
            totalPages = Math.ceil(pagesFromApi).toInt()
            dataList = notificationResponse.data

            if (dataListTemp != null) {
                dataList!!.addAll(dataList!!)
                rvNotification.adapter!!.notifyDataSetChanged()
            } else {
                notificationAdapter =
                    NotificationAdapter(activity!!, notificationResponse.data, this)
                rvNotification.adapter = notificationAdapter
                rvNotification.setHasFixedSize(true)
                rvNotification.layoutManager = layoutManager
                dataListTemp = dataList
            }
        } catch (e: Exception) {

        }
    }

    override fun onItemClicked(position: Int, type: String) {
        var notiType = type.toInt()

        if (notiType == 1) { //started following
            if (VrockkApplication.user_obj != null) {
                HomeFragment.otherUserProfileId = "" + dataListTemp!![position].user?._id
                val i = Intent(activity, OtherProfileActivity::class.java)
                i.putExtra("_id", dataListTemp!![position].user?._id)
                i.putExtra("position", position)
                startActivityForResult(i, 30)
            } else {
                (activity as BaseActivity).showLoginPopup()
            }
        } else if (notiType == 2) { // liked
//            if(VrockkApplication.user_obj != null){
//                HomeFragment.otherUserProfileId = ""+ dataListTemp!![position].user?._id
//                val i = Intent(activity, OtherProfileActivity::class.java)
//                i.putExtra("_id",dataListTemp!![position].user?._id)
//                i.putExtra("position",position)
//                startActivityForResult(i,30)
//            }else{
//                (activity as BaseActivity).showLoginPopup()
//            }
            val ii = Intent(activity, PostsPlayAcivity::class.java)
            //   intent!!.putExtra("_id",postObj.getString("_id"))
            ii.putExtra("postId", dataListTemp!![position].notiData.post)
            ii.putExtra("notification", "")
            startActivityForResult(ii, 30)
        } else if (notiType == 3) { // commented
            if (VrockkApplication.user_obj != null) {
                val i = Intent(activity, CommentActivity::class.java)
                i.putExtra("_id", dataListTemp!![position].notiData.post)
                Log.e("position", " $position ")
                i.putExtra("position", position)
                startActivityForResult(i, 20)
            } else {
                (activity as BaseActivity).showLoginPopup()
            }
        } else if (notiType == 4) { // posted
            val ii = Intent(activity, PostsPlayAcivity::class.java)
            //   intent!!.putExtra("_id",postObj.getString("_id"))
            ii.putExtra("postId", dataListTemp!![position].notiData._id)
            ii.putExtra("notification", "")
            startActivity(ii)
        } else if (notiType == 5) { //
            if (VrockkApplication.user_obj != null) {
                HomeFragment.otherUserProfileId = "" + dataListTemp!![position].user?._id
                val i = Intent(activity, OtherProfileActivity::class.java)
                i.putExtra("_id", dataListTemp!![position].user?._id)
                i.putExtra("position", position)
                startActivityForResult(i, 30)
            } else {
                (activity as BaseActivity).showLoginPopup()
            }
        } else if (notiType == 7) { //
            if (VrockkApplication.user_obj != null) {
                HomeFragment.otherUserProfileId = "" + dataListTemp!![position].user?._id
                val i = Intent(activity, OtherProfileActivity::class.java)
                i.putExtra("_id", dataListTemp!![position].user?._id)
                i.putExtra("position", position)
                startActivityForResult(i, 30)
            } else {
                (activity as BaseActivity).showLoginPopup()
            }
        }
    }
}