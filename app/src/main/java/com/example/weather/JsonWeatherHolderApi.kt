package com.example.weather

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface JsonWeatherHolderApi {
    @GET("data/2.5/weather")
    fun getCity(@Query("q") city: String, @Query("APPID") apiKey: String, @Query("units") units: String = "metric"): Call<Json>
}