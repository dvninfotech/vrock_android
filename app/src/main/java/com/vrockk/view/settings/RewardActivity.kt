package com.vrockk.view.settings

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.*
import com.google.gson.Gson
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.adapter.CoinsPackageAdapter
import com.vrockk.api.ApiResponse
import com.vrockk.api.Status
import com.vrockk.api.parsers.ProfileParser
import com.vrockk.base.BaseActivity
import com.vrockk.models.coins.all_settings.GetAllSettingsResponse
import com.vrockk.models.coins.order_complete.CompleteOrderRequest
import com.vrockk.models.coins.order_complete.FullOrderResponse
import com.vrockk.models.coins.order_complete.PaymentObject
import com.vrockk.models.coins.redeem_coins.ReedemCoinsResponse
import com.vrockk.utils.Utils
import com.vrockk.viewmodels.GetAllSettingsViewModel
import com.vrockk.viewmodels.ProfilePageViewModel
import com.vrockk.viewmodels.viewmodels.CompleteOrderViewModel
import com.vrockk.viewmodels.viewmodels.RedeemCoinsViewModel
import kotlinx.android.synthetic.main.activity_reward.*
import kotlinx.android.synthetic.main.my_custom_alert.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.ArrayList

class RewardActivity : BaseActivity() {

    var googlePlayFee: Int = 0
    var platFormFee: Int = 0
    var redeemPercentage: Int = 0
    var totalRedeemCoins: Int = 0
    var totalPurchaseCoins: Int = 0
    var remaingGiftCoins: Int = 0
    private val completeOrderViewModel by viewModel<CompleteOrderViewModel>()
    private val getAllSettingsViewModel by viewModel<GetAllSettingsViewModel>()
    val profilePageViewModel by viewModel<ProfilePageViewModel>()
    private val redeemCoinsViewModel by viewModel<RedeemCoinsViewModel>()
    lateinit var coinsPackageAdapter: CoinsPackageAdapter
    var transactionId: String? = ""
    var purchase_at: String? = ""
    var expire_at: String? = ""
    private lateinit var billingClient: BillingClient
    var membershipList: List<SkuDetails>? = null
//    var purchaseCoins: Int = 0
//    var purchaseAmount: String = ""
//    var purchase_product_id: String = ""
//    var purchase_coin_type: String = ""
//    var purchase_currency: String = ""
    var totalCoins: Int = 0

    companion object {
        var totalGiftCoins: Int = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reward)

        totalGiftCoins = 0

        Log.e("call", "token: " + VrockkApplication.user_obj!!.authToken)

        observerApi()

        setupBillingClient()

        tvBack.setOnClickListener {
            finish()
        }

        tvRedeemCoins.setOnClickListener {

            showConfirmPopup()

        }

        // call api.
        profilePageViewModel.profilePagePost(
            "SEC " + VrockkApplication.user_obj!!.authToken,
            1,
            1,
            ""
        )
        getAllSettingsViewModel.getAllSettings()

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        billingClient.endConnection()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 35) {

            totalCoins = totalPurchaseCoins + totalGiftCoins
            tvTotalCoins.setText("" + totalCoins)
            tvGiftCoins.setText("Gift coins: " + totalGiftCoins)

            profilePageViewModel.profilePagePost(
                "SEC " + VrockkApplication.user_obj!!.authToken,
                1,
                1,
                ""
            )
        }
    }



    private fun observerApi() {

        profilePageViewModel.profilePageResponse().observe(this, Observer<ApiResponse> { response ->
            this.dataResponse(3, response)
        })

        completeOrderViewModel.completeOrderResponse()
            .observe(this, Observer<ApiResponse> { response ->
                this.dataResponse(1, response)
            })

        getAllSettingsViewModel.getAllSettingsResponse()
            .observe(this, Observer<ApiResponse> { response ->
                this.dataResponse(2, response)
            })

        redeemCoinsViewModel.response().observe(this, Observer<ApiResponse> { response ->
            this.dataResponse(4, response)
        })

    }

    private fun dataResponse(type: Int, response: ApiResponse) {
        when (response.status) {
            Status.LOADING -> {
                if (type == 1 || type == 3 || type == 4)
                    showProgress("")
            }
            Status.SUCCESS -> {
                if (type == 1 || type == 3 || type == 4)
                    hideProgress()

                renderResponse(type, response)
            }
            Status.ERROR -> {
                if (type == 1 || type == 3 || type == 4)
                    hideProgress()
                Log.e("order_error", Gson().toJson(response.error))
            }
        }
    }


    private fun renderResponse(type: Int, response: ApiResponse) {
        Log.e("Response", Gson().toJson(response))

        try {
            val data: String = Utils.toJson(response.data)
            val gson1 = Gson()

            if (type == 1) {
                val fullOrderResponse = gson1.fromJson(data, FullOrderResponse::class.java)
                Log.e("call", "Response purchase response fullorder : " + fullOrderResponse.message)
                if(fullOrderResponse.success){
                    val consumeResponseListene = object : ConsumeResponseListener {
                        override fun onConsumeResponse(p0: BillingResult, p1: String) {
                            Log.e(
                                    "Consumed",
                                    "response code:" + p0.toString() + "   purchaseToken : " + p1
                            )

//                        totalPurchaseCoins += purchaseCoins
                            totalPurchaseCoins += fullOrderResponse.data.coins
                            tvPurchaseCoins.text = "Purchase coins: " + totalPurchaseCoins

                            totalCoins = totalPurchaseCoins + totalGiftCoins
                            tvTotalCoins.setText("" + totalCoins)
                        }
                    }

                    val consumeParams = ConsumeParams.newBuilder()
                            .setPurchaseToken(fullOrderResponse.data.paymentObject.purchaseToken)
//                .setDeveloperPayload(purchase.developerPayload)
                            .build()

                    billingClient.consumeAsync(consumeParams, consumeResponseListene)
                }else{
                    Toast.makeText(this, resources.getString(R.string.transaction_will_be_cancelled_due_to_error), Toast.LENGTH_SHORT).show()
                }
            }

            if (type == 2) {
                val getAllSettingsResponse =
                    gson1.fromJson(data, GetAllSettingsResponse::class.java)
                Log.e("call", "Response getallsettings : " + getAllSettingsResponse.message)

                googlePlayFee = getAllSettingsResponse.data.googlePlayFee
                platFormFee = getAllSettingsResponse.data.platformFee
                redeemPercentage = getAllSettingsResponse.data.redeemPercentage
            }

            if (type == 3) {
//                val profilePageResponse = gson1.fromJson(data, ProfilePageResponse::class.java)
                val profilePageResponse = ProfileParser.parseResponse(response.data)
                if (profilePageResponse != null && profilePageResponse.success) {
                    Log.e("call", "Response profilepage : " + profilePageResponse.message)

                    tvWalletBalance.setText("Wallet Balance")
                    totalPurchaseCoins = profilePageResponse.data.profile.get(0).purchasedCoins
                    totalGiftCoins = profilePageResponse.data.profile.get(0).giftCoins

                    totalCoins = totalPurchaseCoins + totalGiftCoins
                    tvTotalCoins.setText("" + totalCoins)
                    tvGiftCoins.setText("Gift coins: " + totalGiftCoins)
                    tvPurchaseCoins.setText("Purchase coins: " + totalPurchaseCoins)

                    tvRedeemCoins.visibility = View.VISIBLE
                }
            }

            if (type == 4) {
                val reedemCoinsResponse = gson1.fromJson(data, ReedemCoinsResponse::class.java)
                Log.e("call", "Response redeemcoins : " + reedemCoinsResponse.message)

                if (reedemCoinsResponse.success) {
                    redeemResponse("Successfully redeem.")
                    profilePageViewModel.profilePagePost(
                        "SEC " + VrockkApplication.user_obj!!.authToken,
                        1,
                        1,
                        ""
                    )
                } else {
                    redeemResponse("" + reedemCoinsResponse.message)
                }

            }

        } catch (e: java.lang.Exception) {
            Log.e("call", "exception: " + e.toString())
        }

    }


    fun onitemClicked(
        position: Int
//        coin: Int,
//        amount: String,
//        product_id: String,
//        coin_type: String,
//        currency: String
    ) {

//        purchaseCoins = coin
//        purchaseAmount = amount
//        purchase_product_id = product_id
//        purchase_coin_type = coin_type
//        purchase_currency = currency

        try {
            val billingFlowParams = BillingFlowParams
                .newBuilder()
                .setSkuDetails(membershipList!![position])
                .build()
            billingClient.launchBillingFlow(this, billingFlowParams)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("call", "111111 exception " + e.message)
        }
    }


    private fun setupBillingClient() {
        billingClient =
            BillingClient.newBuilder(this).setListener(onPurchasesUpdated)
                .enablePendingPurchases()
                .build();
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                Log.d("service", "Disconnected")
            }

            override fun onBillingSetupFinished(p0: BillingResult) {
                if (p0.responseCode == BillingClient.BillingResponseCode.OK) {

                    val skuList = ArrayList<String>()
                    skuList.add("com_vrockk_brass")
                    skuList.add("com_vrockk_copper")
                    skuList.add("com_vrockk_bronze")
                    skuList.add("com_vrockk_silver")
                    skuList.add("com.vrockk_gold")
//                    skuList.add("com_vrockk_platinum")
//                    skuList.add("com_vrockk_emerald")
                    skuList.add("com_vrockk_crown")


                    val sku = SkuDetailsParams.newBuilder()
                    sku.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
                    billingClient.querySkuDetailsAsync(sku.build(),
                        object : SkuDetailsResponseListener {
                            override fun onSkuDetailsResponse(
                                p0: BillingResult,
                                p1: MutableList<SkuDetails>?
                            ) {
                                try {
                                    membershipList = p1!!
                                    Log.e("men=mbershipList", Gson().toJson(membershipList))

                                    // Log.e("call","RESPONSE COINS DESCRIPTION: "+inAppPurchaseResponse.get(0).zzb.nameValuePairs.description)

                                    Collections.sort(
                                        membershipList,
                                        object : Comparator<SkuDetails> {
                                            override fun compare(
                                                o1: SkuDetails,
                                                o2: SkuDetails
                                            ): Int {
                                                return (o1.priceAmountMicros.toLong() / 1000000.0f - o2.priceAmountMicros.toLong() / 1000000.0f).toInt()
                                            }
                                        })

                                    coinsPackageAdapter =
                                        CoinsPackageAdapter(this@RewardActivity, membershipList!!)
                                    rvCoinPackage.adapter = coinsPackageAdapter
                                    rvCoinPackage.layoutManager =
                                        LinearLayoutManager(
                                            this@RewardActivity,
                                            LinearLayoutManager.VERTICAL,
                                            false
                                        )

                                    val queryPurchases = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
                                    val billingResult: BillingResult  = queryPurchases.billingResult
                                    val purchases: List<Purchase> = queryPurchases.purchasesList as List<Purchase>

                                    for (purchase in purchases){
                                        if(purchase.purchaseState == Purchase.PurchaseState.PURCHASED){
                                            handlePurchase(purchase)
                                        }else {
                                            val consumeResponseListener = object : ConsumeResponseListener {
                                                override fun onConsumeResponse(p0: BillingResult, p1: String) {
                                                    Log.e(
                                                        "Cancelled",
                                                        "response code:" + p0.toString() + "   purchaseToken : " + p1
                                                    )
                                                }
                                            }
                                            val consumeParams = ConsumeParams.newBuilder()

                                                .setPurchaseToken(purchase.purchaseToken)
                                                .build()

                                            billingClient.consumeAsync(consumeParams, consumeResponseListener)

                                        }
                                    }
                                    Log.e("Response", queryPurchases.toString())

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }

                        })

                }
            }

        })
    }


    private val onPurchasesUpdated = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null
        ) {
            showSnackbar(resources.getString(R.string.purchase_in_progress))
            for (purchase in purchases) {
                if(!purchase.isAcknowledged){
                    handlePurchase(purchase)
                }

            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
             Toast.makeText(this, "Purchase Cancelled", Toast.LENGTH_SHORT).show()

        }else if(billingResult.responseCode == BillingClient.BillingResponseCode.ERROR){
            Toast.makeText(this, resources.getString(R.string.error_in_transaction), Toast.LENGTH_SHORT).show()
        } else if(billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED){
            Toast.makeText(this, resources.getString(R.string.transaction_for_this_item_in_progress), Toast.LENGTH_SHORT).show()
        } else {
            // Handle any other error codes.
            Toast.makeText(this, billingResult.debugMessage, Toast.LENGTH_SHORT).show();
        }
    }


    fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Grant entitlement to the user.
            val elem = membershipList!!.find { it.sku == purchase.sku }
            val purchaseAmount = String.format("%.2f", elem!!.priceAmountMicros / 1000000.0f)
            val price = elem.price
            val description = elem.description
            val priceAmountMicros = elem.priceAmountMicros
            val purchaseCurrency = elem.priceCurrencyCode
            val purchaseProductId = elem.sku
            val purchaseCoinType = elem.title.split(" ")[0]
            val type = elem.type
//            val purchaseCoins : Int = elem.description.split(" ")[0].toInt()

            completeOrderViewModel.completeOrder(
                "SEC " + VrockkApplication.user_obj!!.authToken, CompleteOrderRequest(
                    purchaseAmount ,
                    null, //purchaseCoins
                    purchaseProductId,
                    PaymentObject(
                        purchase.purchaseToken,
                        "" + purchase.developerPayload,
                        purchase.isAcknowledged,
                        purchase.isAutoRenewing,
                        purchase.orderId,
                        purchase.originalJson,
                        purchase.packageName,
                        purchase.purchaseState,
                        purchase.purchaseTime,
                        purchase.signature,
                        purchase.sku
                    ),
                    "" + purchase.orderId,
                    purchaseCoinType,
                    purchaseCurrency
                )
            )

            Log.e("purchase", "purchase success" + Gson().toJson(purchase))

//            Log.e("call", "purchase coins: $purchaseCoins")



        }
    }

    fun redeemResponse(msg: String) {

        val myCustomDlg = Dialog(this)
        myCustomDlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        myCustomDlg.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        myCustomDlg.setContentView(R.layout.my_custom_alert)
        myCustomDlg.tvAlertMessage.text = msg
        myCustomDlg.positiveBtn.text = resources.getString(R.string.ok)
        myCustomDlg.tvTitleCustom.text = resources.getString(R.string.vrockk)
        myCustomDlg.noBtn.visibility = View.GONE
        myCustomDlg.show()
        myCustomDlg.positiveBtn.setOnClickListener {
            myCustomDlg.dismiss()
        }

//        AlertDialog.Builder(this).setTitle("vRockk")
//            .setMessage(msg)
//            .setPositiveButton(
//                getString(R.string.ok)
//            ) { p0, p1 ->
//                p0.dismiss()
//            }
//            .show()
    }

    fun showConfirmPopup() {

        val myCustomDlg = Dialog(this)
        myCustomDlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        myCustomDlg.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        myCustomDlg.setContentView(R.layout.my_custom_alert)
        myCustomDlg.tvAlertMessage.text = resources.getString(R.string.msg_you_can_redeem_money_from)
        myCustomDlg.tvTitleCustom.text = resources.getString(R.string.alert)
        myCustomDlg.positiveBtn.text = resources.getString(R.string.ok)
        myCustomDlg.noBtn.visibility = View.GONE
        myCustomDlg.show()
        myCustomDlg.positiveBtn.setOnClickListener {
            myCustomDlg.dismiss()
        }

//        AlertDialog.Builder(this).setTitle("vRockk")
//            .setMessage("You can redeem your money directly to your bank account starting 15 November, 2020 and once you have more than 8000 coins in your wallet.")
//            .setPositiveButton(
//                getString(R.string.ok)
//            ) { p0, p1 ->
//                p0.dismiss()
//
//            }
//            .show()
    }
}