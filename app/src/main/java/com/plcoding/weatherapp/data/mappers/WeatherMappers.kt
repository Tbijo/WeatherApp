package com.plcoding.weatherapp.data.mappers

import com.plcoding.weatherapp.data.remote.WeatherDataDto
import com.plcoding.weatherapp.data.remote.WeatherDto
import com.plcoding.weatherapp.domain.weather.WeatherData
import com.plcoding.weatherapp.domain.weather.WeatherInfo
import com.plcoding.weatherapp.domain.weather.WeatherType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// temp class
// for easier indexing of List<WeatherData>
// otherwise index would have to be time which is a string date
private data class IndexedWeatherData(
    val index: Int,
    val data: WeatherData
)

private fun WeatherDataDto.toWeatherDataMap(): Map<Int, List<WeatherData>> {
    return time.mapIndexed { index, time ->
        val temperature = temperatures[index]
        val weatherCode = weatherCodes[index]
        val windSpeed = windSpeeds[index]
        val pressure = pressures[index]
        val humidity = humidities[index]
        IndexedWeatherData(
            index = index,
            data = WeatherData(
                time = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME),
                temperatureCelsius = temperature,
                pressure = pressure,
                windSpeed = windSpeed,
                humidity = humidity,
                weatherType = WeatherType.fromWMO(weatherCode)
            )
        )
    }.groupBy {
        // grouping by days
        // day is from 0 to 23 hours any number from that interval divided by 24 is 0, which will be index of first day
        // so each index (day) will have 24 items (24 hours), each item is WeatherData data
        it.index / 24
    }.mapValues {
        // map IndexedWeatherData to WeatherData
        it.value.map { it.data }
    }
}

fun WeatherDto.toWeatherInfo(): WeatherInfo {
    val weatherDataMap = weatherData.toWeatherDataMap()
    val now = LocalDateTime.now()
    val currentWeatherData = weatherDataMap[0]?.find {
        // find nearest data to current hour because weather is hourly
        //val hour = if(now.minute < 30) now.hour else now.hour + 1

        // take care of the case when the time would be 23:50 for example
        // then you want to take 12am of the next day, not of the current one
        // TODO ak bude 23:50 treba vybrat pocasie z nasledujuceho dna o 0:00
        // get(1) or [1]
        val hour = when { // toto je zle
            now.minute < 30 -> now.hour
            now.hour == 23 -> 12.00
            else -> now.hour + 1
        }
        it.time.hour == hour
    }
    return WeatherInfo(
        weatherDataPerDay = weatherDataMap,
        currentWeatherData = currentWeatherData
    )
}