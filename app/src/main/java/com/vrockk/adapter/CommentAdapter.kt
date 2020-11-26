package com.vrockk.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vrockk.R
import com.vrockk.VrockkApplication

import com.vrockk.base.BaseActivity
import com.vrockk.models.comments.get_comments.Data
import com.vrockk.view.comment.CommentActivity
import com.vrockk.view.home.HomeActivity
import com.vrockk.view.home.HomeFragment
import com.vrockk.view.posts_play.PostsPlayAcivity
import kotlinx.android.synthetic.main.adapter_comments.view.*


class CommentAdapter(
    val context: Context,
    val dataList: ArrayList<Data>
)  : RecyclerView.Adapter<CommentAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view : View = LayoutInflater.from(context).inflate(R.layout.adapter_comments,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (VrockkApplication.user_obj!!._id.equals(dataList.get(position).user._id))
        {
            holder.ibReport.visibility = View.GONE
        }

        holder.tvName.setText("@"+dataList.get(position).user.userName)
        holder.tvComment.setText(""+dataList.get(position).comment)

        var myFinalValue = VrockkApplication.covertTimeToText(dataList.get(position).createdAt)
        holder.tvTime.setText(""+myFinalValue)

        var profileImageUrl:String = dataList.get(position).user.profilePic
        Glide.with(context).load(profileImageUrl).placeholder(context.resources.getDrawable(R.drawable.user_placeholder)).error(context.resources.getDrawable(R.drawable.user_placeholder)).into(holder.ivProfile!!)

        holder.ibReport.setOnClickListener {
            (context as CommentActivity).showReportDialog(dataList.get(position)._id)
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvName = itemView.tvName
        var tvComment = itemView.tvComment
        var tvTime = itemView.tvTime
        var ivProfile = itemView.ivProfile
        var ibReport = itemView.ibReport

    }


}