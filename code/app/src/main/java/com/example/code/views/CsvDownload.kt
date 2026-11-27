package com.example.code.views

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * Gives back a function that asks the user where to save a csv file and writes it there
 * (US 01.05.02, 01.07.02).
 *
 * Uses the system "save as" picker so no storage permission is needed.
 * In a preview there's nothing to open the picker with, so the function does nothing.
 *
 * @param onResult called with a message to show once the file is saved or saving failed,
 * not called if the user backs out of the picker
 * @return call it with a file name like reservation-abc123.csv and the csv text
 */
@Composable
fun rememberCsvDownloader(onResult: (String) -> Unit = {}): (fileName: String, csv: String) -> Unit {
    // previews have no activity so the launcher below would crash
    if (LocalActivityResultRegistryOwner.current == null) return { _, _ -> }

    val context = LocalContext.current
    // the csv waiting for the user to pick where it goes
    var pendingCsv by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        val csv = pendingCsv
        pendingCsv = null
        // null uri means they backed out
        if (uri == null || csv == null) return@rememberLauncherForActivityResult
        val saved = runCatching {
            context.contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray()) } != null
        }.getOrDefault(false)
        onResult(if (saved) "CSV saved" else "Couldn't save the CSV")
    }

    return { fileName, csv ->
        pendingCsv = csv
        launcher.launch(fileName)
    }
}
