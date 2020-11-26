package com.vrockk

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import kotlinx.android.synthetic.main.circuler_progress_bar.*

class CirculerProgressBar(context: Context, private val textString : String) : Dialog(context, R.style.TransparentDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.circuler_progress_bar)
        setCancelable(false)
    }

    fun setProgressText( textString : String){
        //txtProgress.text = textString
    }

}