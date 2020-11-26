package com.vrockk.view.profile

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.adapter.GalleryPostAdapter
import com.vrockk.api.ApiResponse
import com.vrockk.api.Status
import com.vrockk.base.BaseActivity
import com.vrockk.interfaces.ItemClickListener
import com.vrockk.view.dashboard.ProfileFragment.Companion.dataList
import com.vrockk.viewmodels.DeletePostViewModel
import kotlinx.android.synthetic.main.fragment_gallery.*
import kotlinx.android.synthetic.main.my_custom_alert.*
import org.json.JSONObject
import org.koin.android.viewmodel.ext.android.viewModel

class GalleryFragment : Fragment() , ItemClickListener
{

    var selectedPosition = 0
    val deletePostViewModel by viewModel<DeletePostViewModel>()

    companion object {
        lateinit var galleryPostAdapter: GalleryPostAdapter
        lateinit var instanceGalleryObj:Fragment
        lateinit var rvGallery:RecyclerView
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        instanceGalleryObj = this


        observerApi()

        setUpRecyclerviews()

    }

    override fun onResume() {
        super.onResume()
    }

    public fun setUpRecyclerviews() {

             galleryPostAdapter = GalleryPostAdapter(context!!,dataList,this@GalleryFragment)
             rvGallery.adapter = galleryPostAdapter
             rvGallery.layoutManager = GridLayoutManager(context?.applicationContext,3, LinearLayoutManager.VERTICAL, false)
//             dataListTemp = dataList
    }

    override fun onItemClicked(position: Int) {
        htiDeleteApi(position)
    }

    private fun observerApi() {
        deletePostViewModel.deletePost().observe(activity!!,Observer<ApiResponse>{
             response -> this.dataResponse(response)
        })
    }

    fun htiDeleteApi(position: Int) {

        val myCustomDlg = Dialog(activity!!)
        myCustomDlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        myCustomDlg.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        myCustomDlg.setContentView(R.layout.my_custom_alert)
        myCustomDlg.tvTitleCustom.text = resources.getString(R.string.confirm)
        myCustomDlg.tvAlertMessage.text = resources.getString(R.string.delete_post)
        myCustomDlg.positiveBtn.text = resources.getString(R.string.yes)
        myCustomDlg.noBtn.text = resources.getString(R.string.cancel)
        myCustomDlg.show()
        myCustomDlg.positiveBtn.setOnClickListener {
            myCustomDlg.dismiss()
            selectedPosition = position

            Log.e("call","_id: "+dataList[position]._id)
            val  _token ="SEC "+ VrockkApplication.user_obj!!.authToken
            deletePostViewModel.deleteCommon(_token,dataList[position]._id, "Post")
        }

        myCustomDlg.noBtn.setOnClickListener {
            myCustomDlg.dismiss()
        }


//        AlertDialog.Builder(activity).setTitle(resources.getString(R.string.confirm))
//            .setMessage(getString(R.string.delete_post))
//            .setPositiveButton(getString(R.string.ok)
//             ) { p0, p1 ->
//                selectedPosition = position
//
//                Log.e("call","_id: "+dataList[position]._id)
//                val  _token ="SEC "+ VrockkApplication.user_obj!!.authToken
//                deletePostViewModel.deleteCommon(_token,dataList[position]._id, "Post")
//
//            }
//            .setNeutralButton(getString(R.string.cancel), null)
//            .show()
    }
    private fun dataResponse(response: ApiResponse) {
        when (response.status) {
            Status.LOADING -> {

                    (activity as BaseActivity).showProgress("")

            }
            Status.SUCCESS -> {
                 (activity as BaseActivity).hideProgress()

                renderResponse(response)
            }
            Status.ERROR -> {

                    (activity as BaseActivity).hideProgress()
                Log.e("home_error", Gson().toJson(response.error))
                //showCustomToastMessage(this, "Something went wrong!")
            }
        }
    }

    private fun renderResponse(response: ApiResponse) {
          val json = JSONObject(response.data.toString())
          Log.e("JSON" , " $json")
        dataList.removeAt(selectedPosition)
          galleryPostAdapter.notifyDataSetChanged()
    }
}