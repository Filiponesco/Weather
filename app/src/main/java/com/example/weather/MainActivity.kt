package com.example.weather

import android.annotation.SuppressLint
import android.content.Context
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
import android.os.Parcelable
import android.provider.Settings
import android.view.View
import kotlinx.coroutines.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    lateinit var jsonWeatherHolderApi: JsonWeatherHolderApi
    override fun onCreate(savedInstanceState: Bundle?) {
        chooseTheme(Data.activeCity?.main?.temp?.roundToInt())
        setTheme(AppSetings.theme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buildRetrofit()
        if(AppSetings.createFirstTime){
            changeVisibleUI(false)
            progressBarLoadingContent.visibility = View.VISIBLE
            getCityInfoByID(IDCityOnStart)
            AppSetings.createFirstTime = false
        }
        else {
            changeVisibleUI(true)
            setUI(Data.activeCity)
            progressBarLoadingContent.visibility = View.GONE
            //next time do it with service
            if (Data.cities.isEmpty()) { //parsing JSON only first time
                GlobalScope.launch(Dispatchers.Main) {
                    floating_search_view.showProgress() //inform user: loading json file with city ID
                    Data.cities = getAllCities() //async
                    floating_search_view.hideProgress()
                    setOnQueryChangeListenerSearchBox(Data.cities)
                    setOnClickSuggestionSearchBox()
                }
            } else {
                setOnQueryChangeListenerSearchBox(Data.cities)
                setOnClickSuggestionSearchBox()
            }
        }
        swipeRefresh.setOnRefreshListener {
            getCityInfoByID(Data.activeCity?.id ?: IDCityOnStart)
            swipeRefresh.isRefreshing = false
        }
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
                Data.activeCity = response.body()
                val i = Intent(this@MainActivity, MainActivity::class.java)
                startActivity(i)
                finish()
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
        txtViewDate.text = convertTimeUnixToString(response.dt + response.timezone - 3600, "dd-MM HH:mm")
        txtViewDescription.text = response.weather[0].description
        txtViewTemp.text = "${response.main.temp.roundToInt()}"
        txtViewSunrise.text = convertTimeUnixToString(response.sys.sunrise, "HH:mm")
        txtViewSunset.text = convertTimeUnixToString(response.sys.sunset, "HH:mm")
        txtViewPressure.text = "${response.main.pressure}$pascal"
        txtViewFeelsLike.text = "${response.main.feels_like.roundToInt()}$celcius"
        floating_search_view.setSearchBarTitle("${response.name}, ${response.sys.country}")
        Glide.with(this@MainActivity)
            .load(URLIcon + response.weather[0].icon + "@2x.png")
            .fitCenter()
            .placeholder(R.drawable.example_icon_weather)
            .into(imgViewIcon)
    }
    private fun convertTimeUnixToString(timeUnix: Long, pattern: String): String{
        val sdf = java.text.SimpleDateFormat(pattern)
        val date = java.util.Date(timeUnix * 1000)
        sdf.format(date)
        return sdf.format(date)
    }
    private suspend fun getAllCities(): List<City>{
        return GlobalScope.async(Dispatchers.IO) {
            val reader = applicationContext.assets.open("city.list.json").bufferedReader()
            val cities = Gson().fromJson(reader, Array<City>::class.java).toList()
            Log.e("JSON file", cities[0].toString())
            return@async cities
        }.await()
    }
    private fun setOnQueryChangeListenerSearchBox(cities: List<City>){
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
    }
    private fun setOnClickSuggestionSearchBox(){
        floating_search_view.setOnSearchListener(object : FloatingSearchView.OnSearchListener {
            override fun onSuggestionClicked(searchSuggestion: SearchSuggestion) {
                val mLastCity: City = searchSuggestion as City
                Log.e("OnSuggestionClicked", mLastCity.toString())
                getCityInfoByID(mLastCity.id)
                floating_search_view.clearSearchFocus()
            }
            override fun onSearchAction(query: String) {
                val mLastQuery = query
                Log.e("OnSearchAction", mLastQuery)
                floating_search_view.clearSearchFocus()
            }
        })
    }
    private fun buildRetrofit(){
        val retrofit = Retrofit.Builder()
            .baseUrl(URLOpenWeather)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        jsonWeatherHolderApi = retrofit.create(
            JsonWeatherHolderApi::class.java
        )
    }
    fun changeVisibleUI(visible: Boolean){
        if(visible){
            cardViewExtraData.visibility = View.VISIBLE
            linearLayoutContentUp.visibility = View.VISIBLE
        }
        else{
            cardViewExtraData.visibility = View.INVISIBLE
            linearLayoutContentUp.visibility = View.INVISIBLE
        }
    }
    private fun chooseTheme(temp: Int?){
        if(temp != null) {
            when {
                temp >= 25 -> AppSetings.theme = R.style.AppThemeHot
                temp >= 15 -> AppSetings.theme = R.style.AppThemeNormal
                else -> AppSetings.theme = R.style.AppThemeCold
            }
        }
        else{
            AppSetings.theme = R.style.AppThemeNormal
        }
    }
    companion object{
        const val URLOpenWeather: String = "http://api.openweathermap.org/"
        const val API_KEY: String = "2c3a4150c327f165dd4e954b18ca56bf"
        const val URLIcon = "http://openweathermap.org/img/wn/"
        const val numberOfPrompt = 3
        const val messageFailureApi = "Coś poszło nie tak, włącz internet"
        const val messageResponseNoSuccessful = "Brak odpowiedzi. Kod: "
        const val celcius = "°C"
        const val pascal = "hPa"
        const val IDCityOnStart = 3086800
    }
}
