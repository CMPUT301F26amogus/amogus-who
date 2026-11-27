package com.example.code.model

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * makes and reads the QR codes for campsites
 *
 * a campsite's code holds the text campsite://campsite/<campsite id>
 * nothing is stored in the database, the code is worked out from the id every time
 */
object QRCodeUtils {
    private const val PREFIX = "campsite://campsite/"

    /** the text that goes inside the QR code for a campsite */
    fun contentFor(campsiteId: String): String = PREFIX + campsiteId

    /**
     * gets the campsite id back out of scanned text
     *
     * @return the id, or null when the text is not one of our campsite codes
     */
    fun parseCampsiteId(raw: String?): String? {
        val id = raw?.trim()?.takeIf { it.startsWith(PREFIX) }?.removePrefix(PREFIX) ?: return null
        return id.takeIf { it.isNotBlank() && '/' !in it }
    }

    fun generateQRCode(content: String, size: Int = 512): Bitmap {
        val hints = mapOf(
            EncodeHintType.MARGIN to 1,
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M
        )

        val bitMatrix = QRCodeWriter()
            .encode(content, BarcodeFormat.QR_CODE, size, size, hints)

        val bitMap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)

        for (x in 0 until size) {
            for (y in 0 until size) {
                bitMap.setPixel(x, y, if (bitMatrix[x, y])
                                                Color.BLACK
                                            else
                                                Color.WHITE)
            }
        }

        return bitMap
    }

}