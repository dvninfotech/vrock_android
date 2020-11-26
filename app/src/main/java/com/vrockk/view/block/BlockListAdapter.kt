package com.vrockk.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vrockk.R
import com.vrockk.models.block.blocklistuser.Data
import com.vrockk.view.following.BlockUserListActivity
import com.vrockk.view.following.FollowingActivity
import kotlinx.android.synthetic.main.adapter_blocklist.view.*
import kotlinx.android.synthetic.main.adapter_following.view.tvUserName
import kotlinx.android.synthetic.main.adapter_like.view.ivProfile
import kotlinx.android.synthetic.main.adapter_like.view.tvName

class BlockListAdapter(
    val context: Context,
    val dataList: ArrayList<Data>

)  : RecyclerView.Adapter<BlockListAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view : View = LayoutInflater.from(context).inflate(R.layout.adapter_blocklist,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.tvBlock.setOnClickListener {
            (context as BlockUserListActivity).blockUnblock(dataList[position].blocked._id)
        }

        holder.tvName.text = ""+ dataList[position].blocked.firstName+" "+ dataList[position].blocked.lastName
        holder.tvUsername.text = ""+ dataList[position].blocked.userName
        val profileImageUrl:String = dataList.get(position).blocked.profilePic
        Glide.with(context).load(profileImageUrl).placeholder(context.resources.getDrawable(R.drawable.user_placeholder)).error(context.resources.getDrawable(R.drawable.user_placeholder)).into(holder.ivProfile!!)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvBlock = itemView.tvBlock
        var ivProfile = itemView.ivProfile
        var tvName = itemView.tvName
        var tvUsername = itemView.tvUserName

    }
}