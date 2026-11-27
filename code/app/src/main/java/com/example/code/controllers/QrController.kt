package com.example.code.controllers

import Model
import com.example.code.model.Campsite
import com.example.code.model.QRCodeUtils
import com.google.zxing.common.BitMatrix
import android.graphics.Bitmap
import androidx.navigation.NavController
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.code.Routes
import java.io.File
import java.io.FileOutputStream

class QrController(private val model: Model) {
    fun loadMyCampsite(onResult: (List<Campsite>) -> Unit) {
        model.getCampsiteByOwner(model.currentDeviceId, onResult)
    }

    fun qrFor(campsite: Campsite): Bitmap = QRCodeUtils.generateQRCode(QRCodeUtils.contentFor(campsite.id))
/**
    fun scan(context: Context, navController: NavController, onMessage: (String) -> Unit) {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build()
        GmsBarcodeScanning.getClient(context, options).startScan()
            .addOnSuccessListener { barcode -> openCode(barcode.rawValue, navController, onMessage) }
            .addOnFailureListener { exception -> onMessage("Couldn't scan: ${exception.message ?: "unknown error"}") }
    }
**/

    fun openCode(raw: String?, navController: NavController, onMessage: (String) -> Unit) {
        val id = QRCodeUtils.parseCampsiteId(raw)

        if (id == null) {
            onMessage("That QR Code isn't a campsite code")
            return
        }
        model.getCampsite(id) { campsite ->
            if (campsite == null) {
                onMessage("That campsite couldn't be found")
                return@getCampsite
            }
            model.settTempCampsite(campsite)
            navController.navigate(Routes.CAMPSITE_DETAILS)
        }
    }

    fun share(context: Context, bitmap: Bitmap) {
        val dir = File(context.cacheDir, "qr").apply { mkdirs() }
        val file = File(dir, "campsite-qr.png")

        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Share QR Code"))
    }
}