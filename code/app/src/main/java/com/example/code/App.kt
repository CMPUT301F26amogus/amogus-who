package com.example.code

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@Composable
fun App() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {

    }
}