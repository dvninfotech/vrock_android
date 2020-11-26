package com.vrockk.utils

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.vrockk.R


class CustomProgressDialog(context: Context) : Dialog(context, R.style.TransparentDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.progressdialog)
        setCancelable(false)
    }
}