package com.example.weather

import retrofit2.Retrofit

object Data {
    var cities = listOf<City>()
    var activeCity: Json? = null
}
object AppSetings{
    var createFirstTime = true
    var theme = R.style.AppThemeNormal
}