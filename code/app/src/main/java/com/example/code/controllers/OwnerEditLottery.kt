package com.example.code.controllers

import Model
import androidx.navigation.NavController


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