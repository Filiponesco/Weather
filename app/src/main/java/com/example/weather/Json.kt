package com.example.weather

data class Json(val weather: List<Weather>, val main: Main, val dt: Long, val sys: Sun, val name: String, val timezone: Long, val id: Int) {
    data class Weather(val main: String, val description: String, val icon: String)
    data class Main(val temp: Float, val feels_like: Float, val pressure: String, val humidity: String)
    data class Sun(val sunrise: Long, val sunset: Long, val country: String)
}