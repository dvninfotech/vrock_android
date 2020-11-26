package com.vrockk.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.shape.CornerFamily
import com.vrockk.R
import com.vrockk.interfaces.ItemClickListernerWithType
import com.vrockk.models.home.home_page.Data
import kotlinx.android.synthetic.main.profile_videos_layout.view.*

class PostsRelatedSongsAdapter(
    val ctx: Context,
    val hashTagsList: ArrayList<Data>,
    val ItemClickListernerWithType: ItemClickListernerWithType
) :
    RecyclerView.Adapter<PostsRelatedSongsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.profile_videos_layout, parent, false)
        return MyViewHolder(v)
    }

    override fun getItemCount(): Int {

        return hashTagsList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val radius = ctx.resources.getDimensionPixelSize(R.dimen._7sdp)
        holder.ivVideoImage.shapeAppearanceModel = holder.ivVideoImage.shapeAppearanceModel
            .toBuilder()
            .setTopLeftCorner(CornerFamily.ROUNDED, radius.toFloat())
            .setTopRightCorner(CornerFamily.ROUNDED, radius.toFloat())
            .setBottomLeftCorner(CornerFamily.ROUNDED, radius.toFloat())
            .setBottomRightCorner(CornerFamily.ROUNDED, radius.toFloat())
            .build()

        Glide.with(ctx)
            .load(hashTagsList[position].post) // or URI/path
            .thumbnail(0.1f)
            .into(holder.ivVideoImage)

        holder.tvViews.text = ""+hashTagsList[position].views
        holder.itemView.setOnClickListener {
            ItemClickListernerWithType.onItemClicked(position,"")

        }

    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivVideoImage = itemView.ivVideoImage
        val tvViews = itemView.tvViews
    }
}