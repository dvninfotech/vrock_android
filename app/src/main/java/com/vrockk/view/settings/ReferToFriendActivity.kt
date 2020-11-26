

package com.vrockk.view.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.vrockk.R
import com.vrockk.api.BASE_URL
import com.vrockk.base.BaseActivity
import kotlinx.android.synthetic.main.activity_refertofriend.*
import java.net.URLEncoder

class ReferToFriendActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_refertofriend)

        onclickListeners()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        finish()
    }

    private fun onclickListeners() {

        tvBack.setOnClickListener {

            finish()

        }

        tvCode.setOnClickListener {

            val cManager: ClipboardManager =

                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            val cData = ClipData.newPlainText("text", tvCode.text.toString())

            cManager.setPrimaryClip(cData)

            showSnackbar("Copied to clipboard")

        }

        ivShare.setOnClickListener {
            val ss =URLEncoder.encode(tvCode.text.toString(), "UTF-8")
            val link:String = "${BASE_URL}/api/user/deeplink?url=ss"

            val shareIntent = Intent()

            shareIntent.action = Intent.ACTION_SEND

            shareIntent.type="text/plain"

            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "vRockk")

            shareIntent.putExtra(Intent.EXTRA_TEXT,
                "Refer vRockk to your friends and let them join in on the adventure. As a reward, you will get extra coins.\n$link"
            )

            startActivity(Intent.createChooser(shareIntent,"Referral Code"))

        }

        tvWhatsapp.setOnClickListener {
            val ss =URLEncoder.encode(tvCode.text.toString(), "UTF-8")
            val link:String = "${BASE_URL}/api/user/deeplink?url=ss"


            val shareIntent = Intent()

            shareIntent.action = Intent.ACTION_SEND

            shareIntent.type="text/plain"

            shareIntent.setPackage("com.whatsapp")

            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "vRockk")

            shareIntent.putExtra(Intent.EXTRA_TEXT,
                "Refer vRockk to your friends and let them join in on the adventure. As a reward, you will get extra coins.\n$link"
            )

            startActivity(Intent.createChooser(shareIntent,"Refferal Code"))

        }

    }

}

