package com.vrockk.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.stitchlrapp.interfaces.ItemClickListenerWithCoin
import com.vrockk.R
import com.vrockk.api.BASE_URL
import com.vrockk.interfaces.ItemClickListernerWithType
import com.vrockk.models.coins.CoinsModel
import com.vrockk.models.coins.get_all_coins.Data
import kotlinx.android.synthetic.main.row_coins.view.*

class GiftsAdapter(val ctx: Context, var coinsList: ArrayList<Data>, private val itemClickListener: ItemClickListenerWithCoin) :
    RecyclerView.Adapter<GiftsAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_coins, parent, false)
        return MyViewHolder(v)
    }

    override fun getItemCount(): Int {
        return coinsList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        Log.e("call","gift images: "+coinsList[position].image)

         Glide.with(ctx).load(coinsList[position].image).into(holder.imgGift!!)
        holder.tvGiftName.text = coinsList[position].name
        holder.tvCoins.text = "${coinsList[position].coins} Coins"
        holder.itemView.setOnClickListener {
            itemClickListener.onItemClicked(position,"gift",coinsList[position].coins,coinsList[position].name)
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
           val imgGift = itemView.imgGift
           val tvGiftName = itemView.tvGiftName
           val tvCoins = itemView.tvCoins
    }
}