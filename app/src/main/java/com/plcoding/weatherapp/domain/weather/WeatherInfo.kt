package com.plcoding.weatherapp.domain.weather

// Weather data per day

data class WeatherInfo(
    // day index, weather data
    // 0 is current day
    val weatherDataPerDay: Map<Int, List<WeatherData>>,

    // current day and hour data
    val currentWeatherData: WeatherData?
)
