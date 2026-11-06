package com.example.code

import Model
import View
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.code.Views.AdminHomeView
import com.example.code.Views.CampsiteOwnerHomeView
import com.example.code.Views.LotteriesView
import com.example.code.Views.MapView
import com.example.code.ui.theme.CodeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val id = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID)
            val M = Model()
            M.setDeviceInformation(id)
            CodeTheme {
                App(id,M)
            }
        }
    }
}