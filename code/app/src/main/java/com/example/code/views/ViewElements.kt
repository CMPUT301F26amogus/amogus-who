package com.example.code.views

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.example.code.ui.theme.Purple80
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.code.ui.theme.PurpleGrey40

/** Element
 *
 * drawn elements like buttons or formatted cards,
 * which may make use of remembered variables, flags, or other instances of element
 * */
interface Element {
    /**Draw
     *
     * The drawn, visual aspect of element instances.
     * Liken to calling Button() or Text() to draw some button or string.
     * All instances of Element are visual and thus MUST have a draw function*/
    @Composable
    fun Draw()
}

/**ViewCards
 *
 * ViewCards are the template on which Campsite and Lottery cards can be built.
 * Utilising template design principles to give room to make concrete subclasses
 *
 * Each card consists of a top section in a primary colour for details
 * and a bottom section in a secondary colour for action buttons.
 * Draw is readily available to accept overrides for each different element of the card, whilst
 * maintaining the format!
 * */
abstract class ViewCard(
    val title: String,
//    val subtitle: String? = null,
//    val quickDetails: List<QuickDetail>? = null,
//    val actionButtons: List<ActionButton>? = null,
    val colourPrimary: Color = PurpleGrey40,
    val colourSecondary: Color = Purple80,
    val tag: String
) : Element{
    @Composable
    override fun Draw() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp)
                .background(
                    color = colourPrimary,
                    shape = RoundedCornerShape(8.dp)
                )
                .testTag(tag),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            HeaderRow()
            Spacer(Modifier.padding(vertical = 4.dp))
            QuickDetailsRow()
            ActionButtonRow()
        }
    }

    /**Draws Title and optional SubTitle and TopRightIcon elements in format*/
    @Composable
    protected fun HeaderRow() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center,
            ) {
                /**title text*/
                DrawTitle()
                /**subtitle*/
                DrawSubtitle()
            }
            Spacer(
                modifier = Modifier.weight(1f)
            )
            DrawTopRightIcon()
        }
    }

    /**Draws optional QuckDetail elements in format*/
    @Composable
    protected fun QuickDetailsRow() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DrawQuickDetails()
        }
    }

    /**Draws optional ActionButton elements in format*/
    @Composable
    protected fun ActionButtonRow() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(
                        color = colourSecondary,
                        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)),
                verticalAlignment = Alignment.CenterVertically,
            ){
                DrawActionButtons()
            }
        }
    }

    /**Default function for drawing the Tittle in format*/
    @Composable
    protected open fun DrawTitle() {
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight(5),
            textAlign = TextAlign.Start,
            modifier = Modifier.testTag("title")
        )
    }

    /**Hook for optional subtitle*/
    @Composable
    protected open fun DrawSubtitle() {}

    /**Hook for optional button in the top right (like for rating or lottery status)*/
    @Composable
    protected open fun DrawTopRightIcon() {}

    /**Hook for optional quick details in the card*/
    @Composable
    protected open fun DrawQuickDetails() {}

    /**Hook for card action buttons*/
    @Composable
    protected open fun DrawActionButtons() {}
}


/**CampsiteCard
 *
 * Subclass of ViewCard, CampsiteCard simply draws a card for a campsite with only a details button,
 * TODO: turn this into a general campsite card, then specialize for different views (including reserve for Lottery options and edit for owners etc.)
 *
 * @param title             the name of the campsite
 * @param subtitle          the park / area of the campsite
 * @param maxOccupants      the value for Max Occupants quick detail box
 * @param address           the address of the campsite
 * @param rating            the star rating of the campsite
 * @param onDetail          what happens when the `> Details` button is pressed, place some controller call here
 * @param colourPrimary     the colour of the card's main background (the background of the upper part of the card)
 * @param colourSecondary   the colour of the card's secondary background (quick details boxes, and the lower part of the card, were action buttons are)
 * @param colourTitleText   the text colour of the title and subtitle
 * @param colourActionText  the text colour of the `> Details` actionButton
 * */
open class EntrantCampsiteCard(
    title: String,
    subtitle: String,
    val maxOccupants: String,
    val address: String,
    rating: String,
    val onDetail: () -> Unit,
    colourPrimary: Color,
    colourSecondary: Color,
    colourTitleText: Color,
    colourActionText: Color,
    tag: String
) : CampsiteCard(
    title = title,
    subtitle = subtitle,
    rating = rating,
    quickDetails = List<QuickDetail>(
        size = 2,
        init = {
            if (it == 0){
                QuickDetail(
                    text = "Max Occupants: $maxOccupants",
                    color = colourSecondary,
                    tag = "maxOccupantsDetail"
                )
            } else {
                QuickDetail(
                    text = "Address: $address",
                    color = colourSecondary,
                    tag = "addressDetail"
                )
            }
        }
    ),
    actionButtons = List<ActionButton>(
        size = 1, init = {
            ActionButton(
                onPress = onDetail,
                label = "> Details",
                tag = "DetailButton"
            )
        }
    ),
    colourPrimary = colourPrimary,
    colourSecondary = colourSecondary,
    colourTitleText = colourTitleText,
    colourActionText = colourActionText,
    tag = tag
)

/**CampsiteCard
 *
 * Subclass of ViewCard, CampsiteCard simply draws a card for a campsite with only a details button,
 * TODO: turn this into a general campsite card, then specialize for different views (including reserve for Lottery options and edit for owners etc.)
 *
 * @param title             the name of the campsite, text with testTag "title"
 * @param subtitle          the park / area of the campsite, text with testTag "Subtitle"
 * @param rating            the star rating of the campsite, text with testTag "Rating"
 * @param quickDetails      list of QuickDetails for the middle section of the card, drawn in a row, testTags are defined by QuickDetail (input)
 * @param actionButtons     list of ActionButtons on the lower half of the card, drawn in a row, testTags are defined by ActionButton (input)
 * @param colourPrimary     the colour of the card's main background (the background of the upper part of the card)
 * @param colourSecondary   the colour of the card's secondary background (quick details boxes, and the lower part of the card, were action buttons are)
 * @param colourTitleText   the text colour of the title and subtitle
 * @param colourActionText  the text colour of the `> Details` actionButton
 * */
open class CampsiteCard(
    title: String,
    val subtitle: String,
    val rating: String,
    val quickDetails: List<QuickDetail>,
    val actionButtons: List<ActionButton>,
    colourPrimary: Color,
    colourSecondary: Color,
    val colourTitleText: Color,
    val colourActionText: Color,
    tag: String
) : ViewCard(
    title = title,
    colourPrimary = colourPrimary,
    colourSecondary = colourSecondary,
    tag = tag
) {
    @Composable
    override fun DrawTitle() {
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colourTitleText,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .testTag("Title")
        )
    }

    @Composable
    override fun DrawSubtitle() {
        Text(
            text = subtitle,
            fontSize = 15.sp,
            color = colourTitleText,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .testTag("Subtitle")
        )
    }

    @Composable
    override fun DrawQuickDetails() {
        for (detail in quickDetails){
            detail.Draw()
        }
    }

    @Composable
    override fun DrawActionButtons() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.End
        ) {
            for (button in actionButtons){
                button.Draw()
            }
        }
    }

    @Composable
    override fun DrawTopRightIcon() {
        ExtendedFloatingActionButton(
            modifier = Modifier
                .height(35.dp),
            onClick = { /* Not a button in practice */ },
            shape = CircleShape,
            containerColor = Color(0xfff7f2fa),
            contentColor = Color(0xFF6750A4),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = "Star",
                )
            },
            text = { Text(
                text = rating,
                modifier = Modifier.testTag("Rating")) }
        )
    }

}
//
//class OwnerCampsiteCard(
//    title: String,
//    val subtitle: String? = null,
////    val quickDetails: List<QuickDetail>? = null,
//    val onEdit : () -> Unit,
//    val onDetails : () -> Unit,
//    colourPrimary: Color = PurpleGrey40,
//    colourSecondary: Color = Purple80,
//    tag: String
//): ViewCard(
//    title = title,
//    colourPrimary = colourPrimary,
//    colourSecondary = colourSecondary,
//    tag = tag
//) {
//
//}



/**ActionButton
 *
 * An instance of Element representing a default button without any icon (I couldn't get it to work).
 * ActionButton is also used as the template of a template design pattern,
 * allowing a variety of buttons to be used simply.
 *
 * @param onPress       the action performed when the button is pressed
 * @param fillColour    liken to the 'Fill' field in figma, it is the colour of the button's background
 * @param textColor     the color of the button's text, the default value is the default text value
 * @param label         the text displayed in the button
 * @param tag           the testTag of the button*/
open class ActionButton(
    val onPress: () -> Unit,
    val fillColour: Color? = null,
    val textColor: Color = Color.Unspecified,
    val label: String,
    val tag: String
): Element {

    @Composable
    override fun Draw() {
        Row(
        ) {
            Button(
                onClick = onPress,
                shape = CircleShape,
                colors =
                    if (fillColour != null)
                        {(ButtonDefaults.buttonColors(containerColor = fillColour))}
                    else
                        {(ButtonDefaults.buttonColors())},
                modifier = Modifier
                    .testTag(tag),
            ){
                Row() {
                    Text(
                        text =label,
                        color = textColor
                    )
                }
            }
        }
    }

}



/**DeleteButton
 *
 * Subclass of ActionButton,
 * DeleteButton is a redish button which upon pressing hides the original delete button
 * and draws two new ones, which promts the user to confirm their action.
 * These buttons are: `x DELETE subject`, `Are You Sure?`, and `No`
 * Their tags are `tag`, `tag confirmation`, and `tag cancellation` respectively
 *
 * `Are You Sure?` and `No` are drawn as a row with a 20.dp space between the two buttons
 * */
class DeleteButton(
    onPress: () -> Unit,
    subject: String = "",
    tag: String
): ActionButton(
    onPress = onPress,
    label = "x DELETE $subject",
    fillColour = Color(0xfffad2cc),
    tag = tag
) {
    @Composable
    override fun Draw() {
        var showConfirm by remember {mutableStateOf(false)}

        val delete = ActionButton(
            onPress = {
                showConfirm = true
            },
            fillColour = fillColour,
            label = label,
            tag = tag)
        val areYouSure = ActionButton(
            onPress = {
                showConfirm = false
                onPress
            },
            fillColour = fillColour,
            label = "Are You Sure ?",
            tag = "$tag confirmation"
        )
        val no = ActionButton(
            onPress = {
                showConfirm = false
            },
            label = "No",
            tag = "$tag cancellation"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ){
            if (!showConfirm) {
                delete.Draw()
            }
            else {
                areYouSure.Draw()
                no.Draw()
            }
        }
    }
}


/**QuickDetail
 *
 * Instance of Element, QuickDetail draws the small box representing sub details of a Card
 * visible from the list page
 *
 * @param text      The string to be printed as text
 * @param tag       The testTag of the text
 * */
open class QuickDetail(
    val text: String,
    val color: Color,
    val tag: String
): Element{
    @Composable
    override fun Draw(){
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(
                    color = color,
                    shape = RoundedCornerShape(8.dp)
                )
        ){
            Contents()
        }
    }

    /**Contents
     *
     * The Contents of a single box of QuickDetail
     * */
    @Composable
    fun Contents() {
        Text(
            text = text,
            fontSize = 12.sp,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .padding(start = 8.dp, end = 24.dp)
                .testTag(tag)
        )
    }
}