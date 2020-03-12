package com.example.weather

import android.os.Parcelable

interface SearchSuggestion: Parcelable {
    /**
     * Returns the text that should be displayed
     * for the suggestion represented by this object.
     *
     * @return the text for this suggestion
     */
    fun getBody(): String
}