package com.example.code.views

import Model
import View
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.code.controllers.QrController
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicBoolean

/** the entrant points the phone's camera at a campsite's QR code and lands on that campsite's details screen */
class ScanQRView(model: Model) : View() {
    private val controller = QrController(model)

    private fun cameraAllowed(context: Context) =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    @Composable
    fun ShowScreen(navController: NavController) {
        val context = LocalContext.current
        var hasPermission by remember { mutableStateOf(cameraAllowed(context)) }
        var message by remember { mutableStateOf<String?>(null) }
        // true while a scanned code is being looked up. The camera sees the same code about 30 times a
        // second, so without this one scan would open the details screen many times over
        val busy = remember { AtomicBoolean(false) }

        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted -> hasPermission = granted }

        // ask for the camera as soon as the screen opens
        LaunchedEffect(Unit) {
            if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
        }

        // show an error for a moment and then let the camera scan again
        LaunchedEffect(message) {
            if (message != null) {
                delay(2500)
                message = null
                busy.set(false)
            }
        }

        Column {
            Header(
                headerText = "Scan QR Code",
                backgroundColor = entrantHeaderBackgroundColor,
                textColor = entrantHeaderTextColor,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(secondaryGreenColor)
            ) {
                if (hasPermission) {
                    QRCameraPreview(modifier = Modifier.fillMaxSize()) { raw ->
                        if (busy.compareAndSet(false, true)) {
                            controller.openCode(raw, navController) { message = it }
                        }
                    }
                    // a square to show the user where to aim
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(240.dp)
                            .border(3.dp, Color.White, RoundedCornerShape(16.dp))
                    )
                    Text(
                        text = "Point the camera at a campsite QR code",
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .background(Color(0x99000000))
                            .padding(10.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("The camera is needed to scan QR codes")
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text("Allow camera")
                        }
                        Spacer(Modifier.height(10.dp))
                        // if it was turned off for good the system will not ask again, so go to the app's settings
                        Button(onClick = {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", context.packageName, null)
                                )
                            )
                        }) {
                            Text("Open settings")
                        }
                    }
                }

                message?.let {
                    Text(
                        text = it,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                            .background(Color(0xCC000000))
                            .padding(12.dp)
                    )
                }
            }
            Footer(
                navController = navController,
                textColor = entrantFooterTextColor,
                backgroundColor = entrantFooterBackgroundColor,
                buttonColor = entrantFooterButtonColor,
            )
        }
    }
}