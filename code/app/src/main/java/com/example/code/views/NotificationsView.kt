/*
* NotificationsView.kt : the view for a user's list of notifications
* Copyright (C) 2026 Muhammad Adetunji (maadetun@ualberta.ca)
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published
* by the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
* */

package com.example.code.views

import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavController
import View
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.ui.theme.CodeTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Displays the notifications that belong to the current user.
 *
 * When [uid] is given, the screen loads that user's notifications from Firestore
 * with [getMyNotifications] when it first appears.
 *
 * @param uid the current user's id (their deviceId), or null to skip loading
 * @param initialNotifications messages to show before anything is loaded (used by the preview)
 */
class NotificationsView(
    private val uid: String? = null,
    initialNotifications: List<String> = emptyList(),
) : View() {
    private val notifications = mutableStateListOf<String>().apply { addAll(initialNotifications) }

    private val bgColor = secondaryGreenColor
    private val headerbgColor = entrantHeaderBackgroundColor
    private val headerTextColor = entrantHeaderTextColor
    private val footerTextColor = entrantFooterTextColor
    private val footerbgColor = entrantFooterBackgroundColor
    private val footerButtonColor = entrantFooterButtonColor

    /**
     * Loads every notification whose recipientId matches [uid] and shows their messages.
     *
     * Does nothing if [uid] is null. The query runs asynchronously, so [onResult]
     * is called with the messages once they arrive.
     *
     * @param onResult called with the loaded messages
     */
    fun getMyNotifications(onResult: (List<String>) -> Unit = {}) {
        val id = uid ?: return
        Firebase.firestore.collection(NOTIFICATIONS_COLLECTION)
            .whereEqualTo("recipientId", id)
            .get()
            .addOnSuccessListener { snapshot ->
                val messages = snapshot.documents.mapNotNull { it.getString("message") }
                notifications.clear()
                notifications.addAll(messages)
                onResult(messages)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to load notifications for $id", e)
            }
    }

    @Composable
    fun ShowScreen(navController: NavController) {
        LaunchedEffect(uid) { getMyNotifications() }
        Column {
            Row {
                Header(
                    headerText = "Notifications",
                    backgroundColor = headerbgColor,
                    textColor = headerTextColor,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(bgColor)
            ) {
                if (notifications.isEmpty()) {
                    Text(
                        text = "No notifications",
                        color = gray,
                        fontSize = 18.sp,
                        modifier = Modifier.align(Alignment.Center),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("NotificationsList"),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(notifications) { message ->
                            NotificationRow(message)
                        }
                    }
                }
            }
            Row {
                Footer(
                    navController = navController,
                    textColor = footerTextColor,
                    backgroundColor = footerbgColor,
                    buttonColor = footerButtonColor,
                )
            }
        }
    }

    @Composable
    private fun NotificationRow(message: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(light)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                tint = entrantHeaderBackgroundColor,
                modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = message,
                color = textColor,
                fontSize = 16.sp,
            )
        }
    }

    private companion object {
        const val TAG = "NotificationsView"
        const val NOTIFICATIONS_COLLECTION = "Notification Data"
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationsViewPreview() {
    val nv = NotificationsView(
        initialNotifications = listOf(
            "You won the lottery for Pine Ridge Campground!",
            "Your reservation at Lakeview Site 12 is confirmed.",
            "Lottery for Elk Island closes in 2 days.",
            "Your reservation at Banff Tunnel Mountain was cancelled.",
        )
    )
    CodeTheme {
        nv.ShowScreen(rememberNavController())
    }
}