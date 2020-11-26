package com.vrockk.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vrockk.R
import com.vrockk.interfaces.ItemClickListener
import com.vrockk.models.settings.SettingsModel
import com.vrockk.utils.Constant
import kotlinx.android.synthetic.main.settings_rv_layout.view.*

class SettingsAdapter(val ctx: Context, var settingsModels: ArrayList<SettingsModel>, val itemClickListener: ItemClickListener) :
    RecyclerView.Adapter<SettingsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.settings_rv_layout, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return settingsModels.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val settingsModel = settingsModels[position]
        if (settingsModel.settingsKey == Constant.SETTINGS_DISABLE_ACCOUNT
            || settingsModel.settingsKey == Constant.SETTINGS_DELETE_ACCOUNT)
            holder.itemView.tvSetting.setTextColor(ctx.getColor(R.color._fab806))
        else
            holder.itemView.tvSetting.setTextColor(Color.WHITE)

        holder.itemView.tvSetting.text = settingsModel.settingsName
        holder.itemView.setOnClickListener {
            itemClickListener.onItemClicked(position)
        }
    }

    fun getSettingsType(position: Int): String {
        return settingsModels[position].settingsKey
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
}