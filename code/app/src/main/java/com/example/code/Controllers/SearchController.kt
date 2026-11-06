package com.example.code.Controllers

import Model

class SearchController(
    private val model : Model
) {
    /**
     * Searches for campsites
     */
    public fun search(name: String){
        var tempResult: List<Campsite>? = null
        model.searchCampsite(campName = name){
                result ->
            if (tempResult != null) {
                tempResult = result
            }
        }

    }
}