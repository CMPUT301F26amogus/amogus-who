package com.example.code

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

/**
 * stands in for the system save as picker so a test can read the csv the app writes
 *
 * the app asks android where to save with ACTION_CREATE_DOCUMENT
 * this answers straight away with a file in the apps cache instead of opening the picker
 * close it when the test is done so the picker works normally again
 */
class CsvPickerStub : AutoCloseable {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    /** where the app ends up writing the csv */
    val file: File = File(instrumentation.targetContext.cacheDir, "us-test-${System.nanoTime()}.csv")

    /** the file name the app suggested in the picker or null if it never opened */
    @Volatile
    var suggestedName: String? = null
        private set

    // the no argument monitor lets us look at each intent and only answer the save as one
    private val monitor = object : Instrumentation.ActivityMonitor() {
        override fun onStartActivity(intent: Intent): Instrumentation.ActivityResult? {
            if (intent.action != Intent.ACTION_CREATE_DOCUMENT) return null
            suggestedName = intent.getStringExtra(Intent.EXTRA_TITLE)
            return Instrumentation.ActivityResult(Activity.RESULT_OK, Intent().setData(Uri.fromFile(file)))
        }
    }

    init {
        file.delete()
        instrumentation.addMonitor(monitor)
    }

    /** the lines of the saved csv without the newline at the end */
    fun lines(): List<String> = file.readText().trimEnd().lines()

    override fun close() {
        instrumentation.removeMonitor(monitor)
        file.delete()
    }
}
