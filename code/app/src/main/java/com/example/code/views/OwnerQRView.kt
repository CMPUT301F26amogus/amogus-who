package com.example.code.views

import Model
import View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.code.controllers.QrController
import com.example.code.model.Campsite
import androidx.compose.material3.Button
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.width

class OwnerQRView(model: Model): View() {
    private val controller = QrController(model)
    private val campsites = mutableStateListOf<Campsite>()
    private var loading by mutableStateOf(true)

    @Composable
    fun ShowScreen(navController: NavController) {
        val context = LocalContext.current
        var selected by remember { mutableStateOf<Campsite?>(null) }

        LaunchedEffect(Unit) {
            loading = true
            controller.loadMyCampsite { list->
                campsites.clear()
                campsites.addAll(list)
                loading = false
            }
        }

        Column {
            Header(
                headerText = "QR Codes",
                backgroundColor = ownerHeaderBackgroundColor,
                textColor = ownerHeaderTextColor
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(secondaryGreenColor)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (loading) {
                    Text("Loading your campsites...")
                } else if (campsites.isEmpty()) {
                    Text("Register a campsite first, then its QR Code will appear here")
                } else {
                    Text("Choose a campsite")
                    Spacer(Modifier.height(10.dp))
                    campsites.forEach { campsite ->
                        Button(onClick = {selected = campsite}, modifier = Modifier.fillMaxWidth()) {
                            Text(campsite.name)
                        }
                    }
                }

                selected?.let { campsite ->
                    val bitmap = remember(campsite.id) { controller.qrFor(campsite) }
                    Spacer(Modifier.height(20.dp))
                    Text(campsite.name)
                    Spacer(Modifier.height(10.dp))
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "QR Code for ${campsite.name}",
                        modifier = Modifier
                            .size(280.dp)
                            .background(Color.White)
                            .padding(8.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = { controller.share(context, bitmap) }) {
                        Text("Share")
                    }
                }
            }
            Footer(
                navController = navController,
                textColor = ownerFooterTextColor,
                backgroundColor = ownerFooterBackgroundColor,
                buttonColor = ownerFooterButtonColor
            )
        }
    }
}