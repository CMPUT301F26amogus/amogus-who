package com.example.code.Controllers

import Model
import androidx.navigation.NavController
import com.example.code.Model.Campsite
import com.example.code.Routes

class SearchController(
    private val model : Model
) {
    /**
     * Searches for campsites
     */
    public fun search(name: String, navController: NavController){
        var tempResult: ArrayList<Campsite>? = null
        model.searchCampsite(campName = name){
                result ->
            if (tempResult != null) {
                tempResult = result as ArrayList<Campsite>?
            }
        }
        model.settCampsiteList(tempResult)
        navController.navigate(Routes.SEARCH_RESULTS)
    }
}