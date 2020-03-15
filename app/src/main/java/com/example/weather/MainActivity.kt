package com.example.weather

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.AssetManager
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
import com.arlib.floatingsearchview.FloatingSearchView
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import java.io.*
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T

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
        val cities = getAllCities()

        floating_search_view.setOnQueryChangeListener(FloatingSearchView.OnQueryChangeListener { oldQuery, newQuery ->
            //get suggestions based on newQuery
            if(newQuery.isNotEmpty()) {
                val matchCities = cities.filter { it.name.contains(newQuery) }.take(numberOfPrompt)
                //pass them on to the search view
                floating_search_view.swapSuggestions(matchCities)
            }
            else{
                floating_search_view.clearSuggestions()
            }
        })

        floating_search_view.setOnSearchListener(object : FloatingSearchView.OnSearchListener {
            override fun onSuggestionClicked(searchSuggestion: SearchSuggestion) {
                val mLastCity: City = searchSuggestion as City
                Log.e("OnSuggestionClicked", mLastCity.name)
                getCityInfoByID(mLastCity.id)
                floating_search_view.setSearchBarTitle(mLastCity.name)
                floating_search_view.clearSearchFocus()
            }

            override fun onSearchAction(query: String) {
                val mLastQuery = query
                Log.e("OnSearchAction", mLastQuery)
                floating_search_view.clearSearchFocus()
            }
        })
    }
    private fun getCityInfo(city: String){
        val call: Call<Json> = jsonWeatherHolderApi.getCity(city, API_KEY)
        call.enqueue(object: Callback<Json>{
            override fun onFailure(call: Call<Json>, t: Throwable) {
                Toast.makeText(this@MainActivity, messageFailureApi, Toast.LENGTH_SHORT).show()
                Log.e("responseApiFailure", t.message)
            }

            override fun onResponse(call: Call<Json>, response: Response<Json>) {
                if(!response.isSuccessful) {
                    Toast.makeText(this@MainActivity, messageResponseNoSuccessful + response.code(), Toast.LENGTH_LONG).show()
                    return
                }
                setUI(response.body())
            }
        })
    }
    private fun getCityInfoByID(id: Int){
        val call: Call<Json> = jsonWeatherHolderApi.getCityByID(id, API_KEY)
        call.enqueue(object: Callback<Json>{
            override fun onFailure(call: Call<Json>, t: Throwable) {
                Toast.makeText(this@MainActivity, messageFailureApi, Toast.LENGTH_SHORT).show()
                Log.e("responseApiFailure", t.message)
            }

            override fun onResponse(call: Call<Json>, response: Response<Json>) {
                if(!response.isSuccessful) {
                    Toast.makeText(this@MainActivity, messageResponseNoSuccessful + response.code(), Toast.LENGTH_LONG).show()
                    return
                }
                setUI(response.body())
            }
        })
    }
    @SuppressLint("SetTextI18n")
    private fun setUI(response: Json?){
        if(response == null) {
            txtViewDate.text = "0"
            txtViewDescription.text = "brak opisu"
            txtViewTemp.text = "0"
            return
        }
        txtViewDate.text = convertTimeUnixToString(response.dt, "dd-MM HH:mm")
        txtViewDescription.text = response.weather[0].description
        txtViewTemp.text = "${response.main.temp.roundToInt()}"
        txtViewSunrise.text = convertTimeUnixToString(response.sys.sunrise, "HH:mm")
        txtViewSunset.text = convertTimeUnixToString(response.sys.sunset, "HH:mm")
        txtViewPressure.text = "${response.main.pressure}"
        txtViewFeelsLike.text = "${response.main.feels_like.roundToInt()}"
        Glide.with(this@MainActivity)
            .load(URLIcon + response.weather[0].icon + "@2x.png")
            .fitCenter()
            //.placeholder(R.mipmap.)
            .into(imgViewIcon)
    }
    private fun convertTimeUnixToString(timeUnix: Long, pattern: String): String{
        val sdf = java.text.SimpleDateFormat(pattern)
        val date = java.util.Date(timeUnix * 1000)
        sdf.format(date)
        return sdf.format(date)
    }
    private fun getAllCities(): List<City>{
        val reader = applicationContext.assets.open("city.list.json").bufferedReader()
        val cities = Gson().fromJson(reader, Array<City>::class.java).toList()
        Log.e("JSON file", cities[0].toString())
        return cities
    }

    companion object{
        const val URLOpenWeather: String = "http://api.openweathermap.org/"
        const val API_KEY: String = "2c3a4150c327f165dd4e954b18ca56bf"
        const val URLIcon = "http://openweathermap.org/img/wn/"
        const val numberOfPrompt = 3
        const val messageFailureApi = "Coś poszło nie tak"
        const val messageResponseNoSuccessful = "Brak odpowiedzi. Kod: "
    }
}
