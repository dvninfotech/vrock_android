package com.vrockk.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.shape.CornerFamily
import com.vrockk.R
import com.vrockk.base.BaseActivity
import com.vrockk.interfaces.ItemClickListernerWithType
import com.vrockk.models.hashtags.DataItem

import kotlinx.android.synthetic.main.profile_videos_layout.view.*

class ProfileVideosAdapter(
    val ctx: Context,
    val hashTagsList: ArrayList<DataItem>,
    val ItemClickListernerWithType: ItemClickListernerWithType
) :
    RecyclerView.Adapter<ProfileVideosAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.profile_videos_layout, parent, false)
        return MyViewHolder(v)
    }

    override fun getItemCount(): Int {

        return hashTagsList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val radius = ctx.resources.getDimensionPixelSize(R.dimen._7sdp)
        holder.ivVideoImage.shapeAppearanceModel = holder.ivVideoImage.shapeAppearanceModel
            .toBuilder()
            .setTopLeftCorner(CornerFamily.ROUNDED, radius.toFloat())
            .setTopRightCorner(CornerFamily.ROUNDED, radius.toFloat())
            .setBottomLeftCorner(CornerFamily.ROUNDED, radius.toFloat())
            .setBottomRightCorner(CornerFamily.ROUNDED, radius.toFloat())
            .build()

        holder.tvViews.text = ""+hashTagsList[position].views
        if(hashTagsList[position].thumbnail?.contains("/uploads/")!!){
            (ctx as BaseActivity).getImageRequest(""+hashTagsList[position].thumbnail).into(holder.ivVideoImage)
        }else{
            (ctx as BaseActivity).getImageRequest(hashTagsList[position].thumbnail!!).into(holder.ivVideoImage)
        }

        holder.itemView.setOnClickListener {
            ItemClickListernerWithType.onItemClicked(position,"")

        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivVideoImage = itemView.ivVideoImage
        val tvViews = itemView.tvViews
    }
}