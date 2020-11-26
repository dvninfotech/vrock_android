package com.vrockk.view.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.api.ApiResponse
import com.vrockk.api.Status
import com.vrockk.models.profile.get_favourite_profile.GetFavouriteProfileResponse
import com.vrockk.utils.Utils
import com.vrockk.viewmodels.GetFavouriteProfileViewModel
import kotlinx.android.synthetic.main.fragment_favourite.*
import org.koin.android.viewmodel.ext.android.viewModel

class FavouriteFragment : Fragment()
{
    val getFavouriteProfileViewModel by viewModel<GetFavouriteProfileViewModel>()

    lateinit var favouriteAdapter : FavouriteAdapter

    companion object {
        lateinit var instanceFavouriteFragmentObj:Fragment
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favourite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        instanceFavouriteFragmentObj = this

        getFavouriteProfileViewModel.getFavProfileResponse().observe(viewLifecycleOwner, Observer<ApiResponse> { response ->
            this.dataResponse(response)
        })

        getFavouriteProfileViewModel.getFavProfilePost("SEC "+ VrockkApplication.user_obj!!.authToken)

    }

    private fun dataResponse( response: ApiResponse) {
        when (response!!.status) {
            Status.LOADING -> {
            }
            Status.SUCCESS -> {

                renderResponse(response)
            }
            Status.ERROR -> {

                Log.e("home_error", Gson().toJson(response.error))
            }
        }
    }

    private fun renderResponse(response: ApiResponse) {
        if(response != null){
            Log.e("likes post: ", Gson().toJson(response))

                val data: String = Utils.toJson(response.data)
                val gson1 = Gson();
                val getFavouriteProfileResponse = gson1.fromJson(data, GetFavouriteProfileResponse::class.java)

                if (getFavouriteProfileResponse.success)
                {
                    favouriteAdapter = FavouriteAdapter(context!!,getFavouriteProfileResponse.data)
                    rvFavourite.adapter = favouriteAdapter
                    rvFavourite.layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
                    rvFavourite.setHasFixedSize(true)
                    rvFavourite.layoutManager = GridLayoutManager(context?.applicationContext,3, LinearLayoutManager.VERTICAL, false)
                }
        }
    }


    override fun onResume() {
        super.onResume()
    }


}