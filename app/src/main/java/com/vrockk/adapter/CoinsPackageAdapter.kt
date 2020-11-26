package com.vrockk.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.SkuDetails
import com.vrockk.R
import com.vrockk.view.settings.RewardActivity
import kotlinx.android.synthetic.main.adapter_coins_package.view.*

class CoinsPackageAdapter(
    val context: Context,
    val membershipList: List<SkuDetails>
)  : RecyclerView.Adapter<CoinsPackageAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view : View = LayoutInflater.from(context).inflate(R.layout.adapter_coins_package,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return membershipList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvCoins.setText(""+membershipList.get(position).description)

        val orignal_price: Float = membershipList.get(position).priceAmountMicros / 1000000.0f

        holder.tvDoller.setText(""+membershipList.get(position).price)
        holder.itemView.setOnClickListener {

//            if (membershipList.get(position).description.contains("50 Coins"))
//            {
//                (context as RewardActivity).onitemClicked(position,50,String.format("%.2f", orignal_price),"com_vrockk_brass","brass",membershipList.get(position).priceCurrencyCode)
//            }
//            else if (membershipList.get(position).description.contains("100 Coins"))
//            {
//                (context as RewardActivity).onitemClicked(position,100,String.format("%.2f", orignal_price),"com_vrockk_copper","copper",membershipList.get(position).priceCurrencyCode)
//            }
//            else if (membershipList.get(position).description.contains("200 Coins"))
//            {
//                (context as RewardActivity).onitemClicked(position,200,String.format("%.2f", orignal_price),"com_vrockk_bronze","bronze",membershipList.get(position).priceCurrencyCode)
//            }
//            else if (membershipList.get(position).description.contains("500 Coins"))
//            {
//                (context as RewardActivity).onitemClicked(position,500,String.format("%.2f", orignal_price),"com_vrockk_silver","silver",membershipList.get(position).priceCurrencyCode)
//            }
//            else if (membershipList.get(position).description.contains("1000 Coins"))
//            {
//                (context as RewardActivity).onitemClicked(position,1000,String.format("%.2f", orignal_price),"com_vrockk_gold","gold",membershipList.get(position).priceCurrencyCode)
//            }
//            else if (membershipList.get(position).description.contains("2000 Coins"))
//            {
//                (context as RewardActivity).onitemClicked(position,2000,String.format("%.2f", orignal_price),"com_vrockk_platinum","platinum",membershipList.get(position).priceCurrencyCode)
//            }
//            else if (membershipList.get(position).description.contains("5000 Coins"))
//            {
//                (context as RewardActivity).onitemClicked(position,5000,String.format("%.2f", orignal_price),"com_vrockk_emeraId","emeraid",membershipList.get(position).priceCurrencyCode)
//            }
//            else if (membershipList.get(position).description.contains("10000 Coins"))
//            {
//                (context as RewardActivity).onitemClicked(position,10000,String.format("%.2f", orignal_price),"com_vrockk_crown","crown",membershipList.get(position).priceCurrencyCode)
//            }
            (context as RewardActivity).onitemClicked(position)

        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvCoins = itemView.tvCoins
        var tvDoller = itemView.tvDoller

    }
}