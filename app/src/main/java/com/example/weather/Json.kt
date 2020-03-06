package com.example.weather

data class Json(val weather: List<Weather>, val main: Main, val dt: String, val sys: Sun) {
    data class Weather(val main: String, val description: String, val icon: String)
    data class Main(val temp: String, val feels_like: String, val pressure: String, val humidity: String)
    data class Sun(val sunrise: String, val sunset: String)
}