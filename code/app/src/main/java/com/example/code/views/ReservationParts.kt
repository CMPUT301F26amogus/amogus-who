package com.example.code.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// small pieces the reservation cards and the reserve screen share so they all look the same
// see [26] in Citation.md

private val pillBackground = Color(0xfff7f2fa)
private val pillText = Color(0xFF6750A4)
private val boxText = Color(0xff4a4459)

/**
 * shows a saved date like 2027-06-12 as Jun 12, 2027
 *
 * @param date the date as firestore saves it
 * @return the nicer date or the same text if it cant be read
 */
fun prettyDate(date: String): String =
    runCatching { LocalDate.parse(date).format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())) }
        .getOrDefault(date)

/**
 * the purple star pill in the top right of a card
 *
 * @param rating what to show next to the star eg 4.5 or a dash
 * @param tag test tag for the rating text
 */
@Composable
fun RatingPill(rating: String, tag: String = "") {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(pillBackground)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(Icons.Outlined.Star, contentDescription = "Rating", tint = pillText, modifier = Modifier.size(16.dp))
        Text(rating, color = pillText, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.testTag(tag))
    }
}

/**
 * the check in and check out of a stay side by side in a small box
 *
 * @param checkIn saved check in date like 2027-06-12
 * @param checkOut saved check out date
 * @param background colour of the box
 */
@Composable
fun StayDatesBox(checkIn: String, checkOut: String, background: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .height(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(prettyDate(checkIn), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = boxText)
        Spacer(Modifier.width(6.dp))
        VerticalDivider()
        Spacer(Modifier.width(6.dp))
        Text(prettyDate(checkOut), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = boxText)
    }
}

/**
 * how many guests with a person icon like the mockup
 *
 * @param guests number of guests
 * @param background colour of the box
 */
@Composable
fun GuestsBox(guests: Int, background: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .height(16.dp)
            // reads as guests 3 instead of an icon and a colon for screen readers and tests
            .clearAndSetSemantics { text = AnnotatedString("Guests: $guests") },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.Person, contentDescription = "Guests", tint = boxText, modifier = Modifier.size(16.dp))
        Text(" : $guests", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = boxText)
    }
}

/**
 * a flat button in a cards bottom bar eg X Cancel or > Details
 *
 * @param label the buttons text
 * @param icon drawn before the text or null for none
 * @param tag test tag for the button
 * @param onClick what happens when its tapped
 */
@Composable
fun CardAction(label: String, icon: ImageVector?, tag: String = "", onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.testTag(tag)) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = pillText, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(label, color = pillText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
