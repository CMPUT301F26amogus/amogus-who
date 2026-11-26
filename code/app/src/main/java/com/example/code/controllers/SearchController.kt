package com.example.code.controllers

import Model
import androidx.navigation.NavController
import com.example.code.Routes

class SearchController(
    private val model : Model
) {
    /**
     * Searches for campsites
     */
    public fun search(name: String, navController: NavController){
        // searchCampsite is async so the results are only here inside the callback,
        // save them and navigate in there or the results screen opens with an old list
        model.searchCampsite(name){ result ->
            model.settCampsiteList(ArrayList(result))
            navController.navigate(Routes.SEARCH_RESULTS)
        }
    }
}