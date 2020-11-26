package com.vrockk.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.shape.CornerFamily
import com.squareup.picasso.Picasso
import com.vrockk.R
import com.vrockk.api.IMAGE_BASE_URL
import com.vrockk.base.BaseActivity
import com.vrockk.interfaces.ItemClickListener
import com.vrockk.models.profile.profile_page.Post
import com.vrockk.view.dashboard.DashboardActivity
import com.vrockk.view.home.HomeActivity
import com.vrockk.view.posts_play.Posts2PlayAcivity
import com.vrockk.view.posts_play.PostsPlayAcivity
import kotlinx.android.synthetic.main.profile_videos_layout.view.*
import java.lang.Exception

class GalleryPostAdapter(
    val ctx: Context,
    val postsList: ArrayList<Post>,
    val itemClickListener: ItemClickListener

) :
    RecyclerView.Adapter<GalleryPostAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.profile_videos_layout, parent, false)
        return MyViewHolder(v)
    }

    override fun getItemCount(): Int {

        return postsList.size
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

        Log.e("call","thumbnail: "+postsList[position].thumbnail)

        if(postsList[position].post.contains("media.vrockk")){
            (ctx as BaseActivity).getImageRequest(postsList[position].thumbnail).into(holder.ivVideoImage)
//            Picasso.get().load("http://access.spaceimagingme.com:9090/vdoz/test_thumbail.jpg").into(holder.ivVideoImage)
        }else{
            (ctx as BaseActivity).getImageRequest(IMAGE_BASE_URL + postsList[position].thumbnail).into(holder.ivVideoImage)
        }

        holder.tvViews.text = ""+postsList[position].views
        holder.itemView.setOnClickListener {

            try {
                ctx.startActivity(
                    Intent(ctx, Posts2PlayAcivity::class.java)
                        .putExtra("userId", postsList[position].userId.id)
                        .putExtra("position", position)
                        .putExtra("postId", postsList[position]._id)
                )
            }
            catch (e:Exception)
            {
                Log.e("call","e"+e.toString())
            }


        }
        if(DashboardActivity.classType == "profile"){
            holder.imgDelete.visibility = View.VISIBLE
        }else{
            holder.imgDelete.visibility = View.GONE
        }
        holder.imgDelete.setOnClickListener {
            itemClickListener.onItemClicked(position)

        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var ivVideoImage = itemView.ivVideoImage
        var imgDelete = itemView.imgDelete
        var tvViews = itemView.tvViews

    }
}