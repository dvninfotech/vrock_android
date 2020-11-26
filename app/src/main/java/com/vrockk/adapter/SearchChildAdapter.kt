package com.vrockk.adapter


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.shape.CornerFamily
import com.vrockk.R
import com.vrockk.api.IMAGE_BASE_URL
import com.vrockk.base.BaseActivity
import com.vrockk.interfaces.ItemClickListernerWithType
import com.vrockk.models.search_page.PostsItem
import com.vrockk.view.posts_play.PostsPlayAcivity
import kotlinx.android.synthetic.main.adapter_search_child.view.*


class SearchChildAdapter(
    val context: Context,
    val posts: List<PostsItem?>,
    val parentPostion: Int,
    val ItemClickListernerWithType: ItemClickListernerWithType
) : RecyclerView.Adapter<SearchChildAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view: View =
            LayoutInflater.from(context).inflate(R.layout.adapter_search_child, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val radius = context.resources.getDimensionPixelSize(R.dimen._7sdp)
        holder.ivVideoImage.shapeAppearanceModel = holder.ivVideoImage.shapeAppearanceModel
            .toBuilder()
            .setTopLeftCorner(CornerFamily.ROUNDED, radius.toFloat())
            .setTopRightCorner(CornerFamily.ROUNDED, radius.toFloat())
            .setBottomLeftCorner(CornerFamily.ROUNDED, radius.toFloat())
            .setBottomRightCorner(CornerFamily.ROUNDED, radius.toFloat())
            .build()

        if (posts[position]?.post?.contains("media.vrockk")!!) {
//            (context as BaseActivity)
//                .getImageRequest(posts[position]!!.thumbnail!!).into(holder.ivVideoImage)
            val imageUrl = posts[position]!!.thumbnail!!
            if (imageUrl.isNotEmpty()) {
                (context as BaseActivity)
                    .loadThumbnailWitLoader(imageUrl, holder.ivVideoImage)
            }
        } else {
//            (context as BaseActivity)
//                .getImageRequest(IMAGE_BASE_URL + posts[position]!!.thumbnail)
//                .into(holder.ivVideoImage)
            val imageUrl = IMAGE_BASE_URL + posts[position]!!.thumbnail
            if (imageUrl.isNotEmpty()) {
                (context as BaseActivity)
                    .loadThumbnailWitLoader(imageUrl, holder.ivVideoImage)
            }
        }

        holder.itemView.setOnClickListener {
            if (posts[position]?.hashtags?.isNotEmpty()!!) {
                context.startActivity(
                    Intent(context, PostsPlayAcivity::class.java)
                        .putExtra("hashTag", posts[position]?.hashtags!![0])
                        .putExtra("position", position)
                        .putExtra("postId", posts[position]!!.id)
                )
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivVideoImage = itemView.ivVideoImage
    }
}