package com.vrockk.view.profile

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.shape.CornerFamily
import com.vrockk.R
import com.vrockk.view.home.HomeActivity
import com.vrockk.view.home.HomeFragment
import kotlinx.android.synthetic.main.adapter_favourite.view.*

class FavouriteAdapter(
    val context: Context,
    val dataList: List<com.vrockk.models.profile.get_favourite_profile.Data>
)  : RecyclerView.Adapter<FavouriteAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view : View = LayoutInflater.from(context).inflate(R.layout.adapter_favourite,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

      holder.itemView.setOnClickListener {

          HomeFragment.otherUserProfileId = ""+ dataList.get(position).profileId._id
          val i = Intent(context, OtherProfileActivity::class.java)
          i.putExtra("_id",dataList.get(position).profileId._id)
          i.putExtra("position",position)
          context.startActivity(i)
      }

        val radius = context.resources.getDimensionPixelSize(R.dimen._6sdp)
        holder.ivProfile.shapeAppearanceModel = holder.ivProfile.shapeAppearanceModel
            .toBuilder()
            .setTopLeftCorner(CornerFamily.ROUNDED, radius.toFloat())
            .setTopRightCorner(CornerFamily.ROUNDED, radius.toFloat())
            .setBottomLeftCorner(CornerFamily.ROUNDED, radius.toFloat())
            .setBottomRightCorner(CornerFamily.ROUNDED, radius.toFloat())
            .build()


        var profileImageUrl:String = dataList.get(position).profileId.profilePic
        Log.e("call","url: "+profileImageUrl)
        Glide.with(context).load(profileImageUrl).placeholder(context.resources.getDrawable(R.drawable.user_placeholder)).error(context.resources.getDrawable(R.drawable.user_placeholder)).into(holder.ivProfile!!)

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var ivProfile = itemView.ivProfile

    }
}