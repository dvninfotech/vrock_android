
package com.vrockk.view.settings

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.api.ApiResponse
import com.vrockk.api.Status
import com.vrockk.base.BaseActivity
import com.vrockk.models.coins.redeem_coins.ReedemCoinsResponse
import com.vrockk.utils.Utils
import com.vrockk.viewmodels.viewmodels.RedeemCoinsViewModel
import kotlinx.android.synthetic.main.activity_refertofriend.tvBack
import kotlinx.android.synthetic.main.activity_review_redeem.*
import kotlinx.android.synthetic.main.my_custom_alert.*
import org.koin.android.viewmodel.ext.android.viewModel

class ReviewRedeemActivity : BaseActivity() {

    var googlePlayFee:Int = 0
    var platFormFee:Int = 0
    var redeemPercentage:Int = 0
    var totalRedeemCoins:Int = 0

    var totalPurchaseCoins:Int = 0
    var totalGiftCoins:Int = 0
    var remaingGiftCoins:Int = 0

    var redeemCoins: Double = 0.0
    var remainingCoins: Double = 0.0

    private val redeemCoinsViewModel by viewModel<RedeemCoinsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_redeem)

        redeemCoinsViewModel.response().observe(this, Observer<ApiResponse> { response -> this.dataResponse(1,response)
        })

        totalGiftCoins = intent.getIntExtra("total_gift_coin",0)
        googlePlayFee = intent.getIntExtra("google_pay_fee",0)
        platFormFee = intent.getIntExtra("plateform_fee",0)
        redeemPercentage = intent.getIntExtra("redeem_percentage",0)

        var giftCoins:Double = 0.0
        giftCoins = totalGiftCoins.toDouble()

        giftCoins = giftCoins - (giftCoins * googlePlayFee.toDouble() / 100) - (giftCoins * platFormFee.toDouble() / 100)

        Log.e("call","total redeem value: "+Math.round(giftCoins))


        if (giftCoins > 5000.0) {

            redeemCoins = giftCoins * redeemPercentage.toDouble() / 100
            totalRedeemCoins = Math.round(redeemCoins).toInt()

            Log.e("call", "totalRedeemCoins: " + totalRedeemCoins)

            remainingCoins = giftCoins - redeemCoins
            remaingGiftCoins = Math.round(remainingCoins).toInt()

            Log.e("call", "totalRemaingift: " + remaingGiftCoins)
        }


        tvTotalGiftCoins.setText(""+Math.round(totalGiftCoins.toDouble()).toInt())

        var detail_gift  =  totalGiftCoins.toDouble() * googlePlayFee.toDouble() / 100
        tvGooglePayFee.setText(""+Math.round(detail_gift).toInt())

        var detail_plateform  =  totalGiftCoins.toDouble() * platFormFee.toDouble() / 100
        tvPlateformPayFee.setText(""+Math.round(detail_plateform).toInt())

        var deduction:Double = totalGiftCoins.toDouble() - (detail_gift + detail_plateform)

        tvAfterDeductionFee.setText(""+Math.round(deduction).toInt())

        tvRedeemCoinupto.setText(""+Math.round(redeemCoins).toInt())

        tvWalletBallence.setText(""+Math.round(remainingCoins).toInt())

        tvRedeemCoin.setText(""+Math.round(redeemCoins).toInt())


        onClick()
    }

    override fun onBackPressed() {
        //super.onBackPressed()

        val i = Intent()
        setResult(Activity.RESULT_OK, i)
        finish()
    }

    private fun onClick() {

        tvBack.setOnClickListener {
            val i = Intent()
            setResult(Activity.RESULT_OK, i)
            finish()
        }

        tvRedeemButton.setOnClickListener {
            redeemCoinsViewModel.redeemCoin("SEC "+ VrockkApplication.user_obj!!.authToken, totalRedeemCoins , remaingGiftCoins)
        }

    }

    private fun dataResponse(type:Int , response: ApiResponse) {
        when (response.status) {
            Status.LOADING -> {
                if (type == 1 || type == 3 || type == 4)
                    showProgress("")
            }
            Status.SUCCESS -> {
                if (type == 1 || type == 3 || type == 4)
                    hideProgress()

                renderResponse(type,response)
            }
            Status.ERROR -> {
                if (type == 1 || type == 3 || type == 4)
                    hideProgress()
                Log.e("order_error", Gson().toJson(response.error))
            }
        }
    }


    private fun renderResponse(type:Int ,response: ApiResponse) {
        Log.e("Response", Gson().toJson(response))

        try {
            val data: String = Utils.toJson(response.data)
            val gson1 = Gson()


            if (type == 1)
            {
                val reedemCoinsResponse = gson1.fromJson(data, ReedemCoinsResponse::class.java)
                Log.e("call","Response redeemcoins : "+reedemCoinsResponse.message)

                if (reedemCoinsResponse.success)
                {
                    redeemResponse("Successfully Redeem.","success")
                }
                else
                {
                    redeemResponse(reedemCoinsResponse.message,"")
                }

            }

        }
        catch (e:java.lang.Exception)
        {
            Log.e("call","exception: "+e.toString())
        }
    }

    fun redeemResponse(msg:String, status:String)
    {
        val myCustomDlg = Dialog(this)
        myCustomDlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        myCustomDlg.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        myCustomDlg.setContentView(R.layout.my_custom_alert)
        myCustomDlg.tvAlertMessage.text = msg
        myCustomDlg.tvTitleCustom.text = resources.getString(R.string.vrockk)
        myCustomDlg.positiveBtn.text = resources.getString(R.string.ok)
        myCustomDlg.noBtn.visibility = View.GONE
        myCustomDlg.show()
        myCustomDlg.positiveBtn.setOnClickListener {
            myCustomDlg.dismiss()

            if (status.equals("success"))
                RewardActivity.totalGiftCoins = Math.round(remainingCoins).toInt()

            val i = Intent()
            setResult(Activity.RESULT_OK, i)
            finish()
        }

//        AlertDialog.Builder(this).setTitle("vRockk")
//            .setMessage(msg)
//            .setPositiveButton(getString(R.string.ok)
//            ) { p0, p1 ->
//                p0.dismiss()
//
//                if (status.equals("success"))
//                RewardActivity.totalGiftCoins = Math.round(remainingCoins).toInt()
//
//                val i = Intent()
//                setResult(Activity.RESULT_OK, i)
//                finish()
//
//            }
//            .show()
    }
}

