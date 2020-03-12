package com.example.weather

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    lateinit var jsonWeatherHolderApi: JsonWeatherHolderApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val city: String = "Katowice,pl"

        val retrofit = Retrofit.Builder()
            .baseUrl(URLOpenWeather)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        jsonWeatherHolderApi = retrofit.create(
            JsonWeatherHolderApi::class.java
        )

        // Icon's functionality.
//        textInputCity.setEndIconOnClickListener {
//            getCity(city)
//        }
        // Error
        //textInputCity.error = "Brak takiego miasta"
    }
    private fun getCity(city: String){
        val call: Call<Json> = jsonWeatherHolderApi.getCity(city, API_KEY)
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
                setUI(response.body())
            }
        })
    }
    private fun setUI(response: Json?){
        if(response == null) {
            txtViewDate.text = "0"
            txtViewDescription.text = "brak opisu"
            txtViewTemp.text = "0"
            return
        }
        txtViewDate.text = convertTimeUnixToString(response.dt, "dd-MM HH:mm")
        txtViewDescription.text = response.weather[0].description
        txtViewTemp.text = response.main.temp.roundToInt().toString() + "°C"
        Glide.with(this@MainActivity)
            .load(URLIcon + response.weather[0].icon + "@2x.png")
            .fitCenter()
            //.placeholder(R.mipmap.)
            .into(imgViewIcon)
        txtViewSunrise.text = convertTimeUnixToString(response.sys.sunrise, "HH:mm")
        txtViewSunset.text = convertTimeUnixToString(response.sys.sunset, "HH:mm")
        txtViewPressure.text = response.main.pressure + "hPa"
    }
    private fun convertTimeUnixToString(timeUnix: Long, pattern: String): String{
        val sdf = java.text.SimpleDateFormat(pattern)
        val date = java.util.Date(timeUnix * 1000)
        sdf.format(date)
        return sdf.format(date)
    }

    companion object{
        const val URLOpenWeather: String = "http://api.openweathermap.org/"
        const val API_KEY: String = "2c3a4150c327f165dd4e954b18ca56bf"
        const val URLIcon = "http://openweathermap.org/img/wn/"
    }
}
