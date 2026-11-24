package com.example.code.controllers

import Model
import androidx.navigation.NavController
import com.example.code.model.Campsite
import com.example.code.Routes

class SearchController(
    private val model : Model
) {
    /**
     * Searches for campsites
     */
    public fun search(name: String, navController: NavController){
        var tempResult: ArrayList<Campsite>? = null
        model.searchCampsite(name){
                result ->
            model.settCampsiteList(ArrayList(result))
            navController.navigate(Routes.SEARCH_RESULTS)
        }
    }
}