package com.vrockk.adapter


import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vrockk.R
import com.vrockk.interfaces.ItemClickListernerWithType
import com.vrockk.models.search_page.HashtagsItem
import com.vrockk.view.posts_play.PostsPlayAcivity
import com.vrockk.view.search.SearchHashtagActivity
import kotlinx.android.synthetic.main.adapter_search_parent.view.*


class SearchParentAdapter(
    val context: Context,
    val postsItemList: ArrayList<HashtagsItem>
)  : RecyclerView.Adapter<SearchParentAdapter.ViewHolder>(), ItemClickListernerWithType{

    private var  hashTag = ""
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view : View = LayoutInflater.from(context).inflate(R.layout.adapter_search_parent,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return postsItemList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.tvHashtag.text = postsItemList[position].id
        val searchAdapter = SearchChildAdapter(context,postsItemList[position].posts!!,position ,this)
        holder.rvSearchItems.adapter = searchAdapter
        holder.rvSearchItems.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)

        holder.tvHashtag.setOnClickListener {
            hashTag = postsItemList[position].id!!
            context.startActivity(
                Intent(context, SearchHashtagActivity::class.java)
                    .putExtra("hashTag", postsItemList[position].id)
                    .putExtra("position", position)
            )
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var rvSearchItems = itemView.rvSearchItems
        var tvHashtag = itemView.tvHashtag
    }

    override fun onItemClicked(position1: Int, type: String) {
        context.startActivity(
            Intent(context, PostsPlayAcivity::class.java)
                .putExtra("hashTag", hashTag)
                .putExtra("position", position1)
        )
    }
}