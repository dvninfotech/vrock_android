package com.vrockk.interfaces

interface OnMediaEventChangeListener {
    fun onMediaStateChanged(adapterPosition: Int)
    fun onPlayChanged(adapterPosition: Int, playRequested: Boolean)
}