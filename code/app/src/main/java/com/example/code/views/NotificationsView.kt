package com.example.code.views

import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavController
import View
import Model
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
import com.example.code.controllers.NotificationsController
import com.example.code.ui.theme.CodeTheme

/**
 * Displays the notifications that belong to the current user.
 *
 * When [model] is given, the screen loads the user's notifications through
 * [NotificationsController] when it opens. This is where reservation and
 * cancellation confirmations show up (US 01.05.01, 01.07.02).
 *
 * @param model the app's model, or null to skip loading (used by the preview)
 * @param initialNotifications messages to show before anything is loaded (used by the preview)
 */
class NotificationsView(
    model: Model? = null,
    initialNotifications: List<String> = emptyList(),
) : View() {
    private val notifications = mutableStateListOf<String>().apply { addAll(initialNotifications) }
    private val controller = model?.let { NotificationsController(it) }

    private val bgColor = secondaryGreenColor
    private val headerbgColor = entrantHeaderBackgroundColor
    private val headerTextColor = entrantHeaderTextColor
    private val footerTextColor = entrantFooterTextColor
    private val footerbgColor = entrantFooterBackgroundColor
    private val footerButtonColor = entrantFooterButtonColor

    /**
     * Loads the current user's notifications, newest first, and shows their messages.
     *
     * Does nothing if there's no model. The query runs asynchronously, so [onResult]
     * is called with the messages once they arrive.
     *
     * @param onResult called with the loaded messages
     */
    fun getMyNotifications(onResult: (List<String>) -> Unit = {}) {
        val c = controller ?: return
        c.onNotifications { loaded ->
            val messages = loaded.map { it.content }.filter { it.isNotBlank() }
            notifications.clear()
            notifications.addAll(messages)
            onResult(messages)
        }
    }

    @Composable
    fun ShowScreen(navController: NavController) {
        LaunchedEffect(Unit) { getMyNotifications() }
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