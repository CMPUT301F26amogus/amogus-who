package com.example.code

import Model
import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.code.ui.theme.CodeTheme

class MainActivity : ComponentActivity() {
    // the device id is how we identify users, so we read it on purpose
    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val model = Model()
        model.setDeviceInformation(deviceId)

        setContent {
            CodeTheme {
                App(model)
            }
        }
    }
}
