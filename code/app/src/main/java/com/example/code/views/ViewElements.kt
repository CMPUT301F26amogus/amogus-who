package com.example.code.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.example.code.ui.theme.Purple80
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

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

abstract class ViewCards(
    title: String,
    subTitle: String?,
    rank: String?,
    quickDetails: List<QuickDetail>?,
    actionButtons: List<ActionButton>?,
    m: Modifier,
    tag: String
) : Element{
    @Composable
    override fun Draw() {

    }
}

/**ActionButton
 *
 * An instance of Element representing a default button without any icon (I couldn't get it to work).
 * ActionButton is also used as the template of a template design pattern,
 * allowing a variety of buttons to be used simply.
 *
 * @param onPress       the action performed when the button is pressed
 * @param fillColour    liken to the 'Fill' field in figma, it is the colour of the button's background
 * @param label         the text displayed in the button
 * @param tag           the testTag of the button*/
open class ActionButton(
    /**Action performed upon pressing ActionButton*/
    val onPress: () -> Unit,
    /**Background colour of ActionButton*/
    val fillColour: Color? = null,
    /**Displayed text of ActionButton*/
    val label: String,
    /**TestTag of ActionButton*/
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
                    Text(label)
                }
            }
        }
    }
}

/**DeleteButton
 *
 * Subclass of ActionButton, DeleteButton
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



abstract class QuickDetail(
    val lable: String,
    var value: String,
    val tag: String
){
    fun draw(){

    }
}