package com.vrockk.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import androidx.annotation.NonNull
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.stitchlrapp.interfaces.ItemClickListenerWithCoin
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.adapter.GiftsAdapter
import com.vrockk.api.ApiResponse
import com.vrockk.api.Status
import com.vrockk.base.BaseActivity
import com.vrockk.models.coins.SendCoinResponse
import com.vrockk.models.coins.get_all_coins.Data
import com.vrockk.models.coins.get_all_coins.GetAllCoinsResponse
import com.vrockk.utils.Utils
import com.vrockk.view.settings.RewardActivity
import com.vrockk.viewmodels.GiftsViewModel
import com.vrockk.viewmodels.viewmodels.SendGiftViewModel
import kotlinx.android.synthetic.main.gifts_bs_layout.view.*
import kotlinx.android.synthetic.main.my_custom_alert.*
import org.koin.android.viewmodel.ext.android.viewModel

class GiftsBSFragment(val activity: Context) : BottomSheetDialogFragment(),
    ItemClickListenerWithCoin {

    companion object {
        var otherUserProfileId: String = ""
    }

    private val coinImgs = intArrayOf(
        R.drawable.ic_copper,
        R.drawable.ic_bronze,
        R.drawable.ic_silver,
        R.drawable.ic_gold,
        R.drawable.ic_platinum,
        R.drawable.ic_crown
    )
    private val coinNames =
        arrayOf<String>("Copper", "Bronze", "Silver", "Gold", "Platinum", "Crown")
    private val coinCount = arrayOf<String>("10", "50", "100", "500", "500", "1000")
    private val coinList = ArrayList<Data>()
    private val giftsViewModel by viewModel<GiftsViewModel>()
    private val sendGiftViewModel by viewModel<SendGiftViewModel>()
    private lateinit var giftsAdapter: GiftsAdapter
    var total_coins: Int = 0
    lateinit var contentView: View

    var selected_coins: Int = 0
    var selected_conType: String = ""

    private val mBottomSheetBehaviorCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override
        fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss()
            }
        }

        override
        fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {
        }
    }

    @SuppressLint("RestrictedApi")
    override
    fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        contentView = View.inflate(context, R.layout.gifts_bs_layout, null)
        dialog.setContentView(contentView)
        val params = (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior
        if (behavior != null && behavior is BottomSheetBehavior) {
            (behavior as BottomSheetBehavior).setBottomSheetCallback(mBottomSheetBehaviorCallback)
        }
        (contentView.parent as View).setBackgroundColor(resources.getColor(android.R.color.transparent))

        getCoinsApi()
        observerApi()
        val gridLayoutManager = GridLayoutManager(activity, 3)
        contentView.rvGiftsList.layoutManager = gridLayoutManager
        giftsAdapter = GiftsAdapter(activity, coinList, this)
        contentView.rvGiftsList.adapter = giftsAdapter
        contentView.ibCross.setOnClickListener {
            dismiss()
            //(behavior as BottomSheetBehavior).state = BottomSheetBehavior.STATE_COLLAPSED
        }

        contentView.tvBuyCoins.setOnClickListener {
            (context as BaseActivity).navigate(RewardActivity::class.java)
            dismiss()
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e("created", " ")
    }

    private fun observerApi() {
        giftsViewModel.response().observe(this, Observer<ApiResponse> { response ->
            this.dataResponse(1, response)
        })

        sendGiftViewModel.response().observe(this, Observer<ApiResponse> { response ->
            this.dataResponse(2, response)
        })
    }

    private fun getCoinsApi() {
        if (VrockkApplication.user_obj != null)
            giftsViewModel.getGifts("SEC " + VrockkApplication.user_obj?.authToken ?: "", 1, 10)
    }

    private fun dataResponse(type: Int, response: ApiResponse) {
        when (response.status) {
            Status.LOADING -> {
                if (type == 2)
                    (activity as BaseActivity).showProgress("")
            }
            Status.SUCCESS -> {

                if (type == 2)
                    (activity as BaseActivity).hideProgress()

                renderResponse(type, response)
            }
            Status.ERROR -> {
                if (type == 2)
                    (activity as BaseActivity).hideProgress()
                Log.e("gifts_error", Gson().toJson(response.error))
                //showCustomToastMessage(this, "Something went wrong!")
            }
        }
    }

    private fun renderResponse(type: Int, response: ApiResponse) {
        Log.e("Response", Gson().toJson(response))
        val data: String = Utils.toJson(response.data)
        val gson1 = Gson()

        if (type == 1) {
            try {
                val coinsResponse = gson1.fromJson(data, GetAllCoinsResponse::class.java)
                Log.e("call", "response data 1: " + coinsResponse.purchasedCoins!!)


                total_coins = coinsResponse.purchasedCoins!!
                contentView.tvTotalCoins.setText(total_coins.toString())

                if (coinsResponse?.success!!) {
                    coinList.clear()
                    coinList.addAll(coinsResponse.data!!)
                    giftsAdapter.notifyDataSetChanged()
                }

            } catch (e: Exception) {
                Log.e("call", "exception1111111 " + e.toString())
            }

        }




        if (type == 2) {
            val sendCoinResponse = gson1.fromJson(data, SendCoinResponse::class.java)
            Log.e("call", "response data 2: " + sendCoinResponse.message)
            // Toast.makeText(activity!!, sendCoinResponse.message+" please buy now.", Toast.LENGTH_SHORT).show()

            if (sendCoinResponse.success) {
                total_coins = total_coins - selected_coins
                contentView.tvTotalCoins.setText(total_coins.toString())
                showResponsePopup("Gift successfully sent")
            } else {
                showResponsePopup(sendCoinResponse.message + " please buy now.")
            }
        }
    }

    override fun onItemClicked(position: Int, type: String, coins: Int, coinType: String) {

        showConfirmPopup(coins, coinType)

    }

    fun showConfirmPopup(coins: Int, coinType: String) {

        val myCustomDlg = Dialog(activity!!)
        myCustomDlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        myCustomDlg.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        myCustomDlg.setContentView(R.layout.my_custom_alert)
        myCustomDlg.tvAlertMessage.text =
            resources.getString(R.string.confirm_you_want_to_send_gift)
        myCustomDlg.tvTitleCustom.text = resources.getString(R.string.confirm)
        myCustomDlg.positiveBtn.text = resources.getString(R.string.yes)
        myCustomDlg.noBtn.text = resources.getString(R.string.no)
        myCustomDlg.show()

        myCustomDlg.noBtn.setOnClickListener {
            myCustomDlg.dismiss()
        }

        myCustomDlg.positiveBtn.setOnClickListener {
            myCustomDlg.dismiss()
            selected_coins = coins
            selected_conType = coinType

            Log.e("call", "selected coins: " + selected_coins)
            Log.e("call", "selected coinstype: " + selected_conType)

            sendGiftViewModel.sendGifts(
                "SEC " + VrockkApplication.user_obj!!.authToken,
                otherUserProfileId,
                coins,
                selected_conType
            )
        }

//        AlertDialog.Builder(activity!!).setTitle("vRockk")
//            .setMessage("Are you sure you want to send the gift?")
//            .setPositiveButton(getString(R.string.ok)
//            ) { p0, p1 ->
//                p0.dismiss()
//
//                selected_coins = coins
//                selected_conType = coinType
//
//                Log.e("call","selected coins: "+selected_coins)
//                Log.e("call","selected coinstype: "+selected_conType)
//
//                sendGiftViewModel.sendGifts("SEC "+VrockkApplication.user_obj!!.authToken, HomeFragment.otherUserProfileId,coins,selected_conType)
//
//            }
//            .setNeutralButton(getString(R.string.no), null)
//            .show()
    }

    fun showResponsePopup(responseMessage: String) {

        val myCustomDlg = Dialog(activity!!)
        myCustomDlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        myCustomDlg.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        myCustomDlg.setContentView(R.layout.my_custom_alert)
        myCustomDlg.tvAlertMessage.text = responseMessage
        myCustomDlg.tvTitleCustom.text = resources.getString(R.string.confirm)
        myCustomDlg.positiveBtn.text = resources.getString(R.string.ok)
        myCustomDlg.noBtn.visibility = View.GONE
        myCustomDlg.show()
        myCustomDlg.positiveBtn.setOnClickListener {
            myCustomDlg.dismiss()
        }
//        AlertDialog.Builder(activity!!).setTitle("vRockk")
//            .setMessage(responseMessage)
//            .setPositiveButton(getString(R.string.ok)
//            ) { p0, p1 ->
//                p0.dismiss()
//            }
//            .show()
    }


}