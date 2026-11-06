package com.example.code

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.code.Views.EntrantHomeView

@Composable
fun App(){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {

    }
}