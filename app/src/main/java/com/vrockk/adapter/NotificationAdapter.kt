package com.vrockk.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vrockk.R
import com.vrockk.VrockkApplication

import com.vrockk.interfaces.ItemClickListernerWithType
import com.vrockk.models.notification.Data
import kotlinx.android.synthetic.main.notification_item_layout.view.*

class NotificationAdapter(
    val context: Context,
    val list: List<Data>,
    val ItemClickListernerWithType: ItemClickListernerWithType
) :
    RecyclerView.Adapter<NotificationAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.notification_item_layout, parent, false)
        return MyViewHolder(v)
    }

    override fun getItemCount(): Int {

        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        Log.e("call","list size: "+list.size)
        if(list[position].user != null){

            if (list[position].user?.userName == null)
            {
                holder.tvName.visibility = View.GONE
            }
            else
            {
                holder.tvName.text = "@"+ list[position].user?.userName
            }

            val profileImageUrl:String = ""+list[position].user?.profilePic
            Glide.with(context).load(profileImageUrl).placeholder(context.resources.getDrawable(R.drawable.user_placeholder)).error(context.resources.getDrawable(R.drawable.user_placeholder)).into(holder.ivProfile!!)

        }

        holder.tvData.text = ""+ list[position].message

        val myFinalValue = VrockkApplication.covertTimeToText(list.get(position).notiData.createdAt)
        holder.tvDate.text = ""+myFinalValue

        holder.itemView.setOnClickListener {
            ItemClickListernerWithType.onItemClicked(position,list.get(position).notiType.toString())
        }

    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val ivProfile = itemView.ivProfile
        val tvName = itemView.tvName
        val tvData = itemView.tvData
        val tvDate = itemView.tvDate
    }
}