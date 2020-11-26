package com.vrockk.view.search

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vrockk.R
import com.vrockk.interfaces.ItemClickListernerWithType
import com.vrockk.models.search.SearchAllModel
import com.vrockk.utils.Constant
import kotlinx.android.synthetic.main.row_search_by_type.view.*

class SearchAllTabAdapter(
    val context: Context,
    private var tabItems: ArrayList<String>,
    private var allModel: SearchAllModel,
    private val itemClickListenerWithType: ItemClickListernerWithType
) : RecyclerView.Adapter<SearchAllTabAdapter.ViewHolder>(), ItemClickListernerWithType {

//    lateinit var otherTabAdapter: SearchOtherTabAdapter

//    fun setTabItems(tabItems: ArrayList<String>) {
//        this.tabItems = tabItems
//        notifyDataSetChanged()
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.row_search_by_type, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return tabItems.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txtType.visibility = View.VISIBLE
        holder.rvSearchItem.layoutManager = LinearLayoutManager(context)

        if (tabItems[position] == Constant.HASHTAGS) {
            holder.txtType.text = "Tags"
            val otherTabAdapter = SearchOtherTabAdapter(context, Constant.HASHTAGS, this@SearchAllTabAdapter)
            holder.rvSearchItem.adapter = otherTabAdapter
            otherTabAdapter.setHashTagDataModels(allModel.hashTagsDataModels)
        }
        if (tabItems[position] == Constant.USER) {
            holder.txtType.text = "User"
            val otherTabAdapter = SearchOtherTabAdapter(context, Constant.USER, this@SearchAllTabAdapter)
            holder.rvSearchItem.adapter = otherTabAdapter
            otherTabAdapter.setUserDataModels(allModel.userDataModels)
        }
        if (tabItems[position] == Constant.SONG) {
            holder.txtType.text = "Song"
            val otherTabAdapter = SearchOtherTabAdapter(context, Constant.SONG, this@SearchAllTabAdapter)
            holder.rvSearchItem.adapter = otherTabAdapter
            otherTabAdapter.setSongDataModels(allModel.songsDataModels)
        }
        if (tabItems[position] == Constant.POST) {
            holder.txtType.text = "Video"
            val otherTabAdapter = SearchOtherTabAdapter(context, Constant.POST, this@SearchAllTabAdapter)
            holder.rvSearchItem.adapter = otherTabAdapter
            otherTabAdapter.setPostDataModels(allModel.postsDataModels)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtType: TextView = itemView.txtType
        val rvSearchItem: RecyclerView = itemView.rvSearchItem
    }

    override fun onItemClicked(position: Int, type: String) {
        itemClickListenerWithType.onItemClicked(position, type)
    }
}