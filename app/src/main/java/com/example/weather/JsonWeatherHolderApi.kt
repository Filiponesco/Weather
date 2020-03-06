package com.example.weather

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface JsonWeatherHolderApi {
    @GET("weather")
    fun getCity(@Query("q") city: String, @Query("APPID") apiKey: String): Call<Json>
}