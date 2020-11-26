package com.vrockk

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.google.firebase.FirebaseApp
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.vrockk.koin_di.retrofitModule
import com.vrockk.koin_di.viewModelModule
import com.vrockk.models.login.Data
import com.vrockk.player.cache.CacheUtils
import com.vrockk.socket.AppSocketListener
import com.vrockk.utils.PreferenceHelper
import com.vrockk.utils.Variables
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class VrockkApplication : Application() {
    companion object {
        fun covertTimeToText(dataDate: String): String {
            var convTime: String = ""
            val suffix = "Ago"

            try {
                val dateFormat =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

                dateFormat.setTimeZone(TimeZone.getTimeZone(dataDate));

                val pasTime = dateFormat.parse(dataDate)
                val nowTime = Date()
                val dateDiff = nowTime.getTime() - pasTime.getTime()

                val second = TimeUnit.MILLISECONDS.toSeconds(dateDiff)
                val minute = TimeUnit.MILLISECONDS.toMinutes(dateDiff)
                val hour = TimeUnit.MILLISECONDS.toHours(dateDiff)
                val day = TimeUnit.MILLISECONDS.toDays(dateDiff)

                if (second < 60) {
                    convTime = "a moment ago"
                } else if (minute < 60) {
                    if (minute.toInt() == 1) {
                        convTime = "" + minute + " Minute " + suffix
                    } else {
                        convTime = "" + minute + " Minutes " + suffix
                    }
                } else if (hour < 24) {
                    if (hour.toInt() == 1) {
                        convTime = "" + hour + " Hour " + suffix
                    } else {
                        convTime = "" + hour + " Hours " + suffix
                    }
                } else if (day >= 7) {
                    if (day > 360) {
                        if ((day / 360).toString().equals("")) {
                            convTime = (day / 360).toString() + " Year " + suffix
                        } else {
                            convTime = (day / 360).toString() + " Years " + suffix
                        }
                    } else if (day > 30) {
                        if ((day / 30).toString().equals("1")) {
                            convTime = (day / 30).toString() + " Month " + suffix
                        } else {
                            convTime = (day / 30).toString() + " Months " + suffix
                        }
                    } else {
                        if ((day / 7).toString().equals("1")) {
                            convTime = (day / 7).toString() + " Week " + suffix
                        } else {
                            convTime = (day / 7).toString() + " Weeks " + suffix
                        }
                    }
                } else if (day < 7) {
                    if (day.toInt() == 1) {
                        convTime = day.toString() + " Day " + suffix
                    } else {
                        convTime = day.toString() + " Days " + suffix
                    }
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return convTime
        }

        const val TAG = "VrockkApplication"
        const val appName = "Vrockk"
        lateinit var context: Context

        var gson: Gson? = null
        lateinit var prefs: SharedPreferences
        var user_obj: Data? = null
        var uri_data_deep_linkind: Uri? = null
        var fcmToken = ""



        fun isLoggedIn(): Boolean {
            return user_obj != null
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        CacheUtils(this)
        initializeSocket()

        Variables.HOME_PAGE_RADIX_POST_KEY = ""

        startKoin {
            androidContext(this@VrockkApplication)
            modules(listOf(retrofitModule, viewModelModule))
        }

        FirebaseApp.initializeApp(applicationContext)
        Picasso.setSingletonInstance(Picasso.Builder(applicationContext).build())

        prefs = PreferenceHelper.defaultPrefs(applicationContext)
        gson = Gson()

        user_obj = if (prefs.contains(PreferenceHelper.Key.REGISTEREDUSER)) {
            val gson_String = prefs.getString(PreferenceHelper.Key.REGISTEREDUSER, "")
            gson!!.fromJson(gson_String.toString(), Data::class.java) as Data
        } else {
            null
        }
    }

    fun initializeSocket() {
        AppSocketListener.getInstance().initialize(context)
    }

    fun destroySocketListener() {
        AppSocketListener.getInstance().destroy()
    }

    fun getContext(): Context {
        return context
    }

    override fun onTerminate() {
        super.onTerminate()
        destroySocketListener()
    }
}