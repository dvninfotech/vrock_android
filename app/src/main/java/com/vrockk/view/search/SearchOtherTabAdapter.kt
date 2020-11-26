package com.vrockk.view.search

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.vrockk.R
import com.vrockk.base.BaseActivity
import com.vrockk.interfaces.ItemClickListernerWithType
import com.vrockk.models.search.SearchHashTagDataModel
import com.vrockk.models.search.SearchPostDataModel
import com.vrockk.models.search.SearchSongDataModel
import com.vrockk.models.search.SearchUserDataModel
import com.vrockk.utils.Constant.Companion.POST
import com.vrockk.utils.Constant.Companion.SONG
import com.vrockk.utils.Constant.Companion.USER
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.row_search.view.*

class SearchOtherTabAdapter(
    val context: Context,
    private val searchType: String,
    private val itemClickListenerWithType: ItemClickListernerWithType
) : RecyclerView.Adapter<SearchOtherTabAdapter.ViewHolder>() {

    private val hashTagDataModels = arrayListOf<SearchHashTagDataModel>()
    private val postDataModels = arrayListOf<SearchPostDataModel>()
    private val songDataModels = arrayListOf<SearchSongDataModel>()
    private val userDataModels = arrayListOf<SearchUserDataModel>()

    fun setHashTagDataModels(dataModels: ArrayList<SearchHashTagDataModel>) {
        hashTagDataModels.clear()
        hashTagDataModels.addAll(dataModels)
        notifyDataSetChanged()
    }

    fun setPostDataModels(dataModels: ArrayList<SearchPostDataModel>) {
        postDataModels.clear()
        postDataModels.addAll(dataModels)
        notifyDataSetChanged()
    }

    fun setSongDataModels(dataModels: ArrayList<SearchSongDataModel>) {
        songDataModels.clear()
        songDataModels.addAll(dataModels)
        notifyDataSetChanged()
    }

    fun setUserDataModels(dataModels: ArrayList<SearchUserDataModel>) {
        userDataModels.clear()
        userDataModels.addAll(dataModels)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.row_search, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return when (searchType) {
            USER -> userDataModels.size
            SONG -> songDataModels.size
            POST -> postDataModels.size
            else -> hashTagDataModels.size
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (searchType) {
            USER -> {
                val userDataModel = userDataModels[position]
                holder.ivProfile.visibility = View.VISIBLE

                holder.tvName.text = userDataModel.userName
                if (userDataModel.profilePic.isNotEmpty()) {
                    (context as BaseActivity)
                        .loadThumbnail(userDataModel.profilePic, holder.ivProfile)
                } else
                    holder.ivProfile.setImageResource(R.drawable.user_placeholder)
            }
            SONG -> {
                holder.ivProfile.visibility = View.GONE
                holder.tvName.text = songDataModels[position].originalName.replace(".mp3", "")
            }
            POST -> {
                val postDataModel = postDataModels[position]
                holder.ivProfile.visibility = View.VISIBLE

                holder.tvName.text = postDataModel.originalName.replace(".mp4", "")
                if (postDataModel.thumbnail.isNotEmpty()) {
                    (context as BaseActivity)
                        .loadThumbnail(postDataModel.thumbnail, holder.ivProfile)
                }
            }
            else -> {
                holder.ivProfile.visibility = View.GONE
                if (hashTagDataModels[position]._id.isEmpty()) {
                    holder.clSearchMain.visibility = View.GONE
                } else {
                    holder.tvName.text = hashTagDataModels[position].name
                }
            }
        }
        holder.itemView.setOnClickListener {
            itemClickListenerWithType.onItemClicked(position, searchType)
        }
    }

    fun getHashTagDataModel(position: Int): SearchHashTagDataModel {
        return hashTagDataModels[position]
    }

    fun getPostDataModel(position: Int): SearchPostDataModel {
        return postDataModels[position]
    }

    fun getSongDataModel(position: Int): SearchSongDataModel {
        return songDataModels[position]
    }

    fun getUserDataModel(position: Int): SearchUserDataModel {
        return userDataModels[position]
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProfile: CircleImageView = itemView.ivProfile
        val tvName: TextView = itemView.tvName
        val clSearchMain: ConstraintLayout = itemView.cl_Serach_main
    }
}