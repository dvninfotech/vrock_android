package com.vrockk.adapter


import android.content.Context

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.vrockk.R
import com.vrockk.api.IMAGE_BASE_URL

import com.vrockk.models.search_page.SliderItem
import kotlinx.android.synthetic.main.imagview_layout.view.*

class MyViewPagerAdapter(
    var context: Context,
    val sliderItemList: ArrayList<SliderItem>
) : PagerAdapter(){

    private var layoutInflater: LayoutInflater? = null
    init {
        layoutInflater =  context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }
    override fun isViewFromObject(view: View, `objects`: Any): Boolean {
        return view === objects as View
    }
    override fun getCount(): Int {
        return sliderItemList.size
    }
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view :View = layoutInflater!!.inflate(R.layout.imagview_layout, container, false)

        if (sliderItemList[position].thumbnail!!.contains("media.vrockk"))
        {
            Glide.with(context)
                .load(sliderItemList[position].thumbnail) // or URI/path
                .thumbnail(0.1f)
                .into(view.image)
            container.addView(view);
        }
        else
        {
            Glide.with(context)
                .load(IMAGE_BASE_URL + sliderItemList[position].thumbnail) // or URI/path
                .thumbnail(0.1f)
                .into(view.image)
            container.addView(view);
        }

        return view
    }
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }


}
