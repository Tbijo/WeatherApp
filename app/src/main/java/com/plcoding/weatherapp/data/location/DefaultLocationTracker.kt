package com.plcoding.weatherapp.data.location

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.plcoding.weatherapp.domain.location.LocationTracker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

@ExperimentalCoroutinesApi
class DefaultLocationTracker @Inject constructor( // specific impl of LocationTracker for android
    private val locationClient: FusedLocationProviderClient, // needs provides function manually
    private val application: Application // Hilt provides automatically
): LocationTracker {

    // TODO Could be wrapped in a Resource Object and pass a specific error
    override suspend fun getCurrentLocation(): Location? {
        // Check permission ACCESS_FINE_LOCATION
        val hasAccessFineLocationPermission = ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        // Check permission ACCESS_COARSE_LOCATION
        val hasAccessCoarseLocationPermission = ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // access the Location Manager
        val locationManager = application.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // check if GPS is enabled
        // We can retrieve loacation if one these providers is enabled
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        // if we do not have permissions or nothing is enabled return null
        if(!hasAccessCoarseLocationPermission || !hasAccessFineLocationPermission || !isGpsEnabled) {
            return null
        }

        // if we do have perms and gps is enabled...
        // return location suspendedly
        // suspendCancellableCoroutine - convert a callback to a suspending coroutine
        return suspendCancellableCoroutine { cont ->
            locationClient.lastLocation.apply {
                if(isComplete) {
                    if(isSuccessful) {
                        // if the task is complete, maybe there is a last location we do not have to wait
                        // And if it is successful return the result of the Task (last location)
                        cont.resume(result)
                    } else {
                        // not successful, failed
                        cont.resume(null)
                    }
                    // if resumed twice it will crash
                    return@suspendCancellableCoroutine
                }
                // If it was not completed
                // we want to wait while it completes
                // YAY async
                // this how to transfer callback to coroutine
                addOnSuccessListener {
                    cont.resume(it)
                }
                addOnFailureListener {
                    cont.resume(null)
                }
                addOnCanceledListener {
                    // makes coroutine cancelable
                    cont.cancel()
                }
            }
        }
    }
}