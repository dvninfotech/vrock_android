package com.vrockk.services

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import java.util.concurrent.TimeUnit

public class LocationService: Service() {
    companion object {
        val TAG = "LocationService"
        val LOCATION_INTERVAL = TimeUnit.SECONDS.toMillis(120)

        var instance: LocationService? = null
    }

    var locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)
            val myLocation = locationResult!!.lastLocation

            latitude = myLocation.latitude
            longitude = myLocation.longitude
        }
    }

    var locationRequest: LocationRequest? = null
    var fusedLocationProviderClient: FusedLocationProviderClient? = null

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate() {
        super.onCreate()

        instance = this

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(instance!!)

        locationRequest = LocationRequest.create()
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest!!.interval = LOCATION_INTERVAL
        locationRequest!!.fastestInterval = LOCATION_INTERVAL / 6

        startLocationUpdates()
    }

    public fun getLatitude(): Double {
        return latitude
    }

    public fun getLongitude(): Double {
        return longitude
    }

    public fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest, locationCallback, null
        )
    }

    public fun stopLocationUpdates() {
        fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}