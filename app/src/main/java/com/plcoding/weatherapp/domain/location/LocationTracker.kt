package com.plcoding.weatherapp.domain.location

import android.location.Location

interface LocationTracker {
    // for strict clean code use custom Location class
    // here we use androids Location class
    // But for multiplatform purposes you should create a custom class (Location iOS != Location Android)
    suspend fun getCurrentLocation(): Location?
}