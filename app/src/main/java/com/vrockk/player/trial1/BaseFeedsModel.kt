package com.vrockk.player.trial1

import android.graphics.Rect
import android.view.View
import com.vrockk.player.trial1.visibility_utils.items.ListItem

abstract class BaseFeedsModel protected constructor(
    private val mStateChangeListener: OnFeedPlayStateChangeListener?
) : ListItem {

    companion object {
        const val TAG = "BaseFeedsModel"
    }

    private val mCurrentViewRect = Rect()

    override fun getVisibilityPercents(view: View): Int {
        var percents = 100
        view.getLocalVisibleRect(mCurrentViewRect)
        val height = view.height
        if (viewIsPartiallyHiddenTop()) percents =
            (height - mCurrentViewRect.top) * 100 / height else if (viewIsPartiallyHiddenBottom(
                height
            )
        ) percents = mCurrentViewRect.bottom * 100 / height
        return percents
    }

    override fun setActive(newActiveView: View, newActiveViewPosition: Int) {
        mStateChangeListener!!.onStateActivated(newActiveView, newActiveViewPosition)
    }

    override fun deactivate(currentView: View, position: Int) {
        mStateChangeListener!!.onStateDeactivated(currentView, position)
    }

    private fun viewIsPartiallyHiddenBottom(height: Int): Boolean {
        return mCurrentViewRect.bottom in 1 until height
    }

    private fun viewIsPartiallyHiddenTop(): Boolean {
        return mCurrentViewRect.top > 0
    }
}