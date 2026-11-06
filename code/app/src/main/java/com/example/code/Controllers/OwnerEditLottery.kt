package com.example.code.Controllers

import Model
import androidx.navigation.NavController
import com.example.code.Model.Campsite


class OwnerEditLottery (
    private val model : Model
) {

    fun onCancel(navController: NavController){
        navController.navigate("owner_home")
    }

    fun onEditLottery() {

    }

    fun onAddLottery() {

    }
}