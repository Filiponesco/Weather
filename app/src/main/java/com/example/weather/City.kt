package com.example.weather


import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion
import kotlinx.android.parcel.Parcelize

@Parcelize
data class City(var id: Int, var name: String, var country: String): SearchSuggestion{
    override fun getBody(): String {
        return "$name, $country"
    }
}