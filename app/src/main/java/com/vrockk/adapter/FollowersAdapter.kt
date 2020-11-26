package com.vrockk.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.models.profile.followers_list.Data
import com.vrockk.view.following.FollowersActivity
import com.vrockk.view.following.FollowingActivity
import com.vrockk.view.home.HomeFragment
import com.vrockk.view.profile.OtherProfileActivity
import kotlinx.android.synthetic.main.adapter_following.view.*
import kotlinx.android.synthetic.main.adapter_like.view.ivProfile
import kotlinx.android.synthetic.main.adapter_like.view.tvFollow
import kotlinx.android.synthetic.main.adapter_like.view.tvName

class FollowersAdapter(
    val context: Context,
    val dataList: ArrayList<Data>
) : RecyclerView.Adapter<FollowersAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view : View = LayoutInflater.from(context).inflate(R.layout.adapter_following,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.itemView.setOnClickListener {
            HomeFragment.otherUserProfileId = ""+ dataList[position]._id
            val i = Intent(context, OtherProfileActivity::class.java)
            i.putExtra("_id",dataList[position]._id)
            context.startActivity(i)
        }


        holder.tvFollow.visibility = View.GONE


        if(VrockkApplication.user_obj !=null){
            if(VrockkApplication.user_obj?._id == dataList[position]._id){
                holder.tvFollow.visibility = View.GONE
            }else{
                holder.tvFollow.visibility = View.VISIBLE
            }
        }

        if(dataList[position].isFollowing){
            holder.tvFollow.text = "Unfollow"
            holder.tvFollow.background = context.resources.getDrawable(R.drawable.button_outline)
        }else{
            holder.tvFollow.text = "Follow"
            holder.tvFollow.background = context.resources.getDrawable(R.drawable.button_round_yellow)
        }

        holder.tvFollow.setOnClickListener {
            if(dataList[position].isFollowing){
                holder.tvFollow.text = "Follow"
                holder.tvFollow.background = context.resources.getDrawable(R.drawable.button_round_yellow)

                (context as FollowersActivity).followUnfollow(false ,dataList[position]._id ,position)
            }else{
                holder.tvFollow.text = "Unfollow"
                holder.tvFollow.background = context.resources.getDrawable(R.drawable.button_outline)
                (context as FollowersActivity).followUnfollow(true ,dataList[position]._id,position)
            }
        }


        holder.tvName.text = ""+ dataList[position].firstName+" "+ dataList[position].lastName
        holder.tvUsername.text = ""+ dataList[position].userName
        val profileImageUrl:String = dataList.get(position).profilePic
        Glide.with(context).load(profileImageUrl).placeholder(context.resources.getDrawable(R.drawable.user_placeholder)).error(context.resources.getDrawable(R.drawable.user_placeholder)).into(holder.ivProfile!!)

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvFollow = itemView.tvFollow
        var ivProfile = itemView.ivProfile
        var tvName = itemView.tvName
        var tvUsername = itemView.tvUserName

    }
}
