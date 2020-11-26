package com.vrockk.fcm

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.facebook.login.LoginManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.utils.PreferenceHelper
import com.vrockk.view.comment.CommentActivity
import com.vrockk.view.dashboard.DashboardActivity
import com.vrockk.view.home.HomeActivity
import com.vrockk.view.login.LoginActivity
import com.vrockk.view.posts_play.PostsPlayAcivity
import org.json.JSONObject

class MyFirebaseMessagingService : FirebaseMessagingService()  {
    private val TAG = MyFirebaseMessagingService::class.java.simpleName
    private var mChannel: NotificationChannel? = null
    private var notifManager: NotificationManager? = null

    var title = ""
    var type = ""
    var receiverId = ""
    var message = ""
    var intent : Intent?=null

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.e("newToken","hhh $p0")
    }

    @SuppressLint("WrongConstant")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.e("man", "$remoteMessage csvvcxcccc ")
        Log.e("man", " " + remoteMessage.data)

        val gson = Gson()

        if(remoteMessage.data.containsKey("driverData")){
            val obj=  JSONObject(remoteMessage.data["driverData"].toString())
        }

        displayNotifications(
            remoteMessage.data["title"].toString(),
            remoteMessage.data["body"].toString(),
            remoteMessage.data["type"]!!.toInt(),
            JSONObject(remoteMessage.data["notiData"].toString())
        )
        // }

        if (remoteMessage.notification != null) {
            message = remoteMessage.notification!!.body!!
            Log.e("man", "Message Notification Body: " + remoteMessage.notification!!.body!!)
            Log.e("man", "Message Notification Body: " + remoteMessage.notification!!.title!!)
        } else {
            Log.e("man", "Message Notification Body   else")
        }
    }

    private fun displayNotifications(title: String, message: String, /*jsonObject: JSONObject?,*/ clickAction: Int, jsonObject: JSONObject) {
        Log.e("titila", " $title message ")

        try {
            if (notifManager == null) {
                notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }

            intent = Intent(applicationContext, DashboardActivity::class.java)
            intent!!.putExtra("notification","")

            val icon = R.mipmap.ic_launcher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_HIGH
                if (mChannel == null) {
                    mChannel = NotificationChannel(packageName, title, importance)
                    mChannel!!.description = message
                    mChannel!!.enableVibration(true)
                    notifManager!!.createNotificationChannel(mChannel!!)
                }
                val builder: NotificationCompat.Builder =
                    NotificationCompat.Builder(this, packageName)
                val pendingIntent: PendingIntent =
                    PendingIntent.getActivity(this, 1251, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                builder.setContentTitle(title)
                    .setSmallIcon(R.mipmap.ic_launcher) // required
                    .setContentText(message)  // required
                    .setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setLargeIcon(BitmapFactory.decodeResource(resources, icon))
                    .setBadgeIconType(icon!!)
                    .setContentIntent(pendingIntent)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                val notification = builder.build()
                notifManager!!.notify(1251, notification)
            } else {
                Log.e("2334", "12345")
                var pendingIntent: PendingIntent? = null
                pendingIntent = PendingIntent.getActivity(this, 1251, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val notificationBuilder = NotificationCompat.Builder(this, packageName)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setColor(ContextCompat.getColor(baseContext, R.color.colorPrimary))
                    .setSound(defaultSoundUri)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .setBadgeIconType(icon!!)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setStyle(NotificationCompat.BigTextStyle().setBigContentTitle(title).bigText(message))
                notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notifManager!!.notify(1251, notificationBuilder.build())
            }
        } catch (e: Exception) {
            Log.e("noti", " Exception $e")
        }
    }



    fun isAppIsInBackground(context: Context): Boolean {
        var isInBackground = true
        try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                val runningProcesses = am.runningAppProcesses
                for (processInfo in runningProcesses) {
                    if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        for (activeProcess in processInfo.pkgList) {
                            Log.e("Active process : ", activeProcess)
                            if (activeProcess == context.packageName) {
                                isInBackground = false
                            }
                        }
                    }
                }
            } else {
                val taskInfo = am.getRunningTasks(1)
                val componentInfo = taskInfo[0].topActivity
                Log.e("componentInfo : ", componentInfo.toString())
                if (componentInfo!!.packageName == context.packageName) {
                    isInBackground = false
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return isInBackground
    }
}