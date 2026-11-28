package com.example.code.views

import Model
import View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.code.model.Notification
import com.example.code.ui.theme.CodeTheme

/**
 * Shows a campsite owner every notification they have sent (US 02.10.02):
 * lottery win and lose notices, rejected reservations and removed campsites.
 *
 * the list loads in update(), which the model calls when the app starts and
 * whenever data changes, like after the owner sends a new notification
 *
 * @param M the model to get the sent notifications from
 */
class OwnerSentNotificationsView(M: Model) : View() {
    // state list so the screen redraws when the notifications load
    val sentNotifications = mutableStateListOf<Notification>()

    /** update
     * gets the notifications this owner sent (UML: views only call model get-functions here)
     *
     * @param model the model, passed in when it notifies its views
     */
    override fun update(model: Model) {
        model.getSentNotifications { notifications ->
            sentNotifications.clear()
            sentNotifications.addAll(notifications)
        }
    }

    @Composable
    fun ShowScreen(navController: NavController) {
        Column {
            Row {
                Header(
                    headerText = "Sent Notifications",
                    backgroundColor = ownerHeaderBackgroundColor,
                    textColor = ownerHeaderTextColor,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(secondaryGreenColor)
            ) {
                if (sentNotifications.isEmpty()) {
                    Text(
                        text = "You haven't sent any notifications",
                        color = gray,
                        fontSize = 18.sp,
                        modifier = Modifier.align(Alignment.Center),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("SentNotificationsList"),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(sentNotifications) { notification ->
                            SentNotificationRow(notification)
                        }
                    }
                }
            }
            Row {
                Footer(
                    navController = navController,
                    textColor = ownerFooterTextColor,
                    backgroundColor = ownerFooterBackgroundColor,
                    buttonColor = ownerFooterButtonColor,
                )
            }
        }
    }

    @Composable
    private fun SentNotificationRow(notification: Notification) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(light)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Text(
                text = notification.content,
                color = textColor,
                fontSize = 16.sp,
            )
            // timestamp is YYYY-MM-DDTHH:MM:SS, show it as "YYYY-MM-DD HH:MM"
            Text(
                text = "Sent " + notification.timestamp.replace("T", " ").take(16),
                color = gray,
                fontSize = 13.sp,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OwnerSentNotificationsViewPreview() {
    val view = OwnerSentNotificationsView(Model())
    view.sentNotifications.addAll(
        listOf(
            Notification(content = "The Pine Lake lottery draw is this Friday!", timestamp = "2026-11-24T10:15:00"),
            Notification(content = "Reminder: registration for Elk Island closes tomorrow.", timestamp = "2026-11-20T18:02:41"),
        )
    )
    CodeTheme {
        view.ShowScreen(rememberNavController())
    }
}
