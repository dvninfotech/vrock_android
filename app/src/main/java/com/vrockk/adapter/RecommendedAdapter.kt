package com.vrockk.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vrockk.R
import com.vrockk.models.upload_post.gethashtags.Data
import com.vrockk.view.create_video.VideoPostActivity
import kotlinx.android.synthetic.main.adapter_recommended.view.*

class RecommendedAdapter(
    val context: Context,
    val list: List<Data>
)  : RecyclerView.Adapter<RecommendedAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view : View = LayoutInflater.from(context).inflate(R.layout.adapter_recommended,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvTags.setText(""+list.get(position)._id)

        holder.itemView.setOnClickListener {
            (context as VideoPostActivity).getTags(holder.tvTags.text.toString())
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvTags = itemView.tvTags

    }
}