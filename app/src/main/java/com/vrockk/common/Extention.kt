package com.vrockk.common

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.provider.Settings
import com.vrockk.R

fun onfailure(mcontext: Context, message: String,button:String,mActivity: Activity)
{
    Handler().post{
        if (message.isNotEmpty())
        {
            var dialogBuilder = AlertDialog.Builder(mcontext)
                .setMessage(message)
                .setPositiveButton(button) { arg0, _ -> when(button){
                    mcontext.getString(R.string.gotosettings) ->{
                        arg0.dismiss()
                        try {
                            startInstalledAppDetailsActivity(mActivity)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    else -> arg0.dismiss()
                } }
            val alert = dialogBuilder.create()
            alert.setTitle(mcontext.getString(R.string.alert))
            alert.show()
            val posButton = alert.getButton(DialogInterface.BUTTON_POSITIVE)
            with(posButton) { setTextColor(Color.RED) }
        }
    }
}

private fun startInstalledAppDetailsActivity(context: Activity) {
    val i = Intent()
    i.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    i.addCategory(Intent.CATEGORY_DEFAULT)
    i.data = Uri.parse("package:" + context.packageName)
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
    i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    context.startActivity(i)
}