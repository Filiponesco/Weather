package com.example.weather

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URL

class MainActivity : AppCompatActivity() {
    lateinit var jsonWeatherHolderApi: JsonWeatherHolderApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val city: String = "Katowice,pl"
        val apiKey: String = "2c3a4150c327f165dd4e954b18ca56bf"

        val retrofit = Retrofit.Builder()
            .baseUrl("http://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        jsonWeatherHolderApi = retrofit.create(
            JsonWeatherHolderApi::class.java
        )
        val call: Call<Json> = jsonWeatherHolderApi.getCity(city, apiKey)
        call.enqueue(object: Callback<Json>{
            override fun onFailure(call: Call<Json>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Coś poszło nie tak", Toast.LENGTH_SHORT).show()
                Log.e("responseApiFailure", t.message)
            }

            override fun onResponse(call: Call<Json>, response: Response<Json>) {
                if(!response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Brak odpowiedzi, code: " + response.code(), Toast.LENGTH_LONG).show()
                    return
            }
                Toast.makeText(this@MainActivity, "odpowiedz: " + response.body()!!, Toast.LENGTH_LONG).show()
                Log.e("responseApiCorrect", response.body()!!.toString())
            }
        })
    }
}
