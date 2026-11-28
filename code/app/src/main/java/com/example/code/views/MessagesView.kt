package com.example.code.views

import Model
import View
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.code.controllers.MessagesController
import com.example.code.model.Message
import com.example.code.ui.theme.CodeTheme

/**
 * one reservation you can message about, shown in the list of conversations
 *
 * @param reservationId the reservation the conversation is about
 * @param title who or where, eg the campsite name or the entrant's name
 * @param detail extra info like the dates of the stay
 */
class ConversationRow(val reservationId: String, val title: String, val detail: String)

/**
 * Messaging between entrants and campsite owners about a reservation
 * (US 01.14.01 entrant to owner, US 02.12.01 owner to entrant)
 *
 * shows the list of reservations you can message about, or the open conversation
 */
class MessagesView(M: Model) : View() {
    val controller = MessagesController(M)

    // state so the screen redraws when the data loads
    var myId by mutableStateOf("")
    var openReservationId by mutableStateOf("")
    var openTitle by mutableStateOf("")
    val messages = mutableStateListOf<Message>()
    val guestConversations = mutableStateListOf<ConversationRow>()
    val ownerConversations = mutableStateListOf<ConversationRow>()

    /** update
     * gets the open conversation's messages, or the reservations you can message about
     * (UML: views only call model get-functions here)
     *
     * @param model the model, passed in when it notifies its views
     */
    override fun update(model: Model) {
        myId = model.currentDeviceId
        openReservationId = model.messageReservationId
        openTitle = model.messageTitle

        if (openReservationId.isBlank()) {
            // reservations you made, to message the owner (US 01.14.01)
            model.getEntrantActiveReservations(model.currentDeviceId) { list ->
                guestConversations.clear()
                guestConversations.addAll(list.map {
                    ConversationRow(
                        it.reservation.id,
                        it.campsite?.name ?: "Campsite",
                        "Your stay ${it.reservation.checkIn} to ${it.reservation.checkOut}",
                    )
                })
            }
            // reservations at your campsites, to message the entrant (US 02.12.01)
            model.getOwnerActiveReservations { list ->
                ownerConversations.clear()
                ownerConversations.addAll(list.map {
                    ConversationRow(
                        it.reservation.id,
                        it.entrantName.ifBlank { "Camper" },
                        "${it.campsite?.name ?: "Campsite"}, ${it.reservation.checkIn} to ${it.reservation.checkOut}",
                    )
                })
            }
        } else {
            model.getMessagesForReservation(openReservationId) { loaded ->
                messages.clear()
                messages.addAll(loaded)
            }
        }
    }

    @Composable
    fun ShowScreen(navController: NavController) {
        var draft by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        // leaving the screen goes back to the list, so the next visit doesn't open an old chat
        DisposableEffect(Unit) {
            onDispose { controller.showConversationList() }
        }

        Column {
            Row {
                Header(
                    headerText = if (openReservationId.isBlank()) "Messages" else openTitle,
                    backgroundColor = entrantHeaderBackgroundColor,
                    textColor = entrantHeaderTextColor,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(secondaryGreenColor)
            ) {
                if (openReservationId.isBlank()) {
                    ConversationList()
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            "< All conversations",
                            color = Color(0xff5530a4),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(16.dp)
                                .clickable { controller.showConversationList() }
                                .testTag("backToConversations"),
                        )
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .testTag("messagesList"),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (messages.isEmpty()) {
                                item {
                                    Text("No messages yet, say hi!", color = gray, fontSize = 16.sp)
                                }
                            }
                            items(messages) { message ->
                                MessageBubble(message, mine = message.senderId == myId)
                            }
                        }
                        errorMessage?.let {
                            Text(
                                it,
                                color = Color(0xffb3261e),
                                modifier = Modifier.padding(horizontal = 16.dp).testTag("messageError"),
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(light)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextField(
                                value = draft,
                                onValueChange = { draft = it },
                                label = { Text("Message") },
                                modifier = Modifier.weight(1f).testTag("messageInput"),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            ElevatedButton(
                                onClick = {
                                    controller.sendMessage(draft) { error ->
                                        errorMessage = error
                                        if (error == null) draft = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryPurple),
                                modifier = Modifier.testTag("sendMessage"),
                            ) {
                                Text("Send", color = Color(0xff5530a4))
                            }
                        }
                    }
                }
            }
            Row {
                Footer(
                    navController = navController,
                    textColor = entrantFooterTextColor,
                    backgroundColor = entrantFooterBackgroundColor,
                    buttonColor = entrantFooterButtonColor,
                )
            }
        }
    }

    @Composable
    private fun ConversationList() {
        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("conversationList"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (guestConversations.isEmpty() && ownerConversations.isEmpty()) {
                item {
                    Text(
                        "You can message about your active reservations here. You don't have any yet.",
                        color = gray,
                        fontSize = 16.sp,
                    )
                }
            }
            if (guestConversations.isNotEmpty()) {
                item { SectionTitle("Message a campsite owner") }
                items(guestConversations) { ConversationCard(it) }
            }
            if (ownerConversations.isNotEmpty()) {
                item { SectionTitle("Message a camper at your campsites") }
                items(ownerConversations) { ConversationCard(it) }
            }
        }
    }

    @Composable
    private fun SectionTitle(text: String) {
        Text(text, color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }

    @Composable
    private fun ConversationCard(row: ConversationRow) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(light)
                .clickable { controller.showConversation(row.reservationId, row.title) }
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Text(row.title, color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(row.detail, color = gray, fontSize = 14.sp)
        }
    }

    @Composable
    private fun MessageBubble(message: Message, mine: Boolean) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (mine) primaryPurple else light)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(message.contents, color = textColor, fontSize = 16.sp)
                // timestamp is YYYY-MM-DDTHH:MM:SS, show it as "YYYY-MM-DD HH:MM"
                Text(
                    (if (mine) "You" else "Them") + " · " + message.timestamp.replace("T", " ").take(16),
                    color = gray,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MessagesViewPreview() {
    val view = MessagesView(Model())
    view.myId = "me"
    view.openReservationId = "r1"
    view.openTitle = "Pine Lake"
    view.messages.addAll(
        listOf(
            Message(senderId = "me", contents = "Hi! Is there firewood at the site?", timestamp = "2026-11-27T10:15:00"),
            Message(senderId = "owner", contents = "Yes, there's a stack by the fire pit.", timestamp = "2026-11-27T10:20:00"),
        )
    )
    CodeTheme {
        view.ShowScreen(rememberNavController())
    }
}
