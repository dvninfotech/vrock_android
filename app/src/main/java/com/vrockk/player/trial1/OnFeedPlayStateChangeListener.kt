package com.vrockk.player.trial1

import android.view.View

interface OnFeedPlayStateChangeListener {
    fun onStateActivated(newActiveView: View?, newActiveViewPosition: Int)
    fun onStateDeactivated(currentView: View?, currentViewPosition: Int)
}