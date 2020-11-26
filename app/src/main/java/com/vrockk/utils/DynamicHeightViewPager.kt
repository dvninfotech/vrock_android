package com.vrockk.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.MeasureSpec
import androidx.viewpager.widget.ViewPager


class DynamicHeightViewPager : ViewPager {
    private var mCurrentView: View? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (mCurrentView != null) {
            mCurrentView!!.measure(
                widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            )

            val height = Math.max(0, mCurrentView!!.getMeasuredHeight())
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)

            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    fun measureCurrentView(currentView: View) {
        mCurrentView = currentView
        requestLayout()
    }
}