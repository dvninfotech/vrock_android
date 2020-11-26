package com.vrockk.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vrockk.R
import com.vrockk.models.local_sogs.LocalSongsModel
import com.vrockk.models.songs_list.DataItem
import com.vrockk.models.songs_list.SongsListModel
import com.vrockk.view.cameraactivity.StartCountDownActivity
import com.vrockk.view.settings.UpdateProfileActivity
import kotlinx.android.synthetic.main.songs_rv_layout.view.*
import java.util.ArrayList

class SongListAdapter (val ctx: Context,  var localSongsList: ArrayList<DataItem>) :
    RecyclerView.Adapter<SongListAdapter.MyViewHolder>() {

    lateinit var clickAdapter: ClickAdapter
    interface ClickAdapter{
        fun clickItem(position: Int)
    }

    fun adapterListener(clickAdapter: ClickAdapter){
        this.clickAdapter = clickAdapter
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.songs_rv_layout, parent, false)
        return MyViewHolder(v)
    }

    override fun getItemCount(): Int {
        return localSongsList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.tvNotiTitle.text = localSongsList[position].originalName.replace(".mp3","")
        holder.itemView.setOnClickListener {
            clickAdapter.clickItem(position)
        }
    }

    fun filterList(list: ArrayList<DataItem>) {
        this.localSongsList = list
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgSong  = itemView.imgSong
        val tvNotiTitle  = itemView.tvNotiTitle
        val tvNotiData  = itemView.tvNotiData
    }
}