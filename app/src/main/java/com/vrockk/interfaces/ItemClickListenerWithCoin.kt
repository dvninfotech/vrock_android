package com.stitchlrapp.interfaces

interface ItemClickListenerWithCoin {
    fun onItemClicked(position: Int, type: String, coins:Int, coinType:String)
}