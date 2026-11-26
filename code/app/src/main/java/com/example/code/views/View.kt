import androidx.navigation.NavController
import com.example.code.Routes
import com.example.code.goHome
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

interface ViewObserver<M> {
    fun update( model: M )
}

abstract class View: ViewObserver<Model>{
    override fun update(model: Model) {

    }
    //Common colors go here
    protected val light = Color(0xfff3fff3)
    protected val dark = Color(0xff000000)
    protected val gray = Color(0xff7d7e7d)
    protected val primaryGreenColor = Color(0xff93cd8b)
    protected val secondaryGreenColor = Color(0xffcae99d)
    protected val primaryGreenTextColor = Color(0xff054f33)
    protected val primaryTextColor = Color(0xff4a4459)
    protected val primaryPurple = Color(0xffe8def8)
    protected val secondaryPurple = Color(0xFF6750A4)
    protected val tertiaryPurple = Color(0xfff7f2fa)
    protected val entrantHeaderBackgroundColor = Color(0xff28b019)
    protected val entrantHeaderTextColor = Color(0xffffffff)
    protected val entrantFooterBackgroundColor = primaryGreenColor
    protected val entrantFooterButtonColor = Color(0xff9bfa8e)
    protected val entrantFooterTextColor = primaryTextColor

    protected val ownerHeaderBackgroundColor = primaryGreenTextColor
    protected val ownerHeaderTextColor = Color(0xffffffff)
    protected val ownerFooterBackgroundColor = Color(0xff57bc78)
    protected val ownerFooterButtonColor = Color(0xff9bfa8e)
    protected val ownerFooterTextColor = primaryTextColor

    protected val adminHeaderBackgroundColor = Color(0xff033131)
    protected val adminHeaderTextColor = Color(0xffffffff)
    protected val adminFooterBackgroundColor = Color(0xff5aae8c)
    protected val adminFooterButtonColor = Color(0xff9bfa8e)
    protected val adminFooterTextColor = primaryTextColor
    protected val textColor = Color(0xff12553a)
    protected val creamBG = Color(0xffe4eeed)
    protected val cardPrimary = Color(0xffcad9c8)
    protected val cardAccent = light


    private val headerHeight = 120
    //Generic Component Composables go here
    @Composable
    protected fun Header(
        headerText: String = "",
        subheaderText: String = "",
        backgroundColor: Color = entrantHeaderBackgroundColor,
        textColor: Color = entrantHeaderTextColor,
        headerTextSize: Int = 40,
        subheaderTextSize: Int = 30,
        customContent: @Composable (() -> Unit)? = null,
    ) {
        Column(Modifier
            .fillMaxWidth()
            .height(headerHeight.dp)
            .background(backgroundColor)
        ) {
            if (customContent == null) {
                if (headerText != "") {
                    Spacer(Modifier.size(25.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(backgroundColor)
                    ) {
                        Spacer(Modifier.size(20.dp))
                        Text(
                            text = headerText,
                            fontSize = headerTextSize.sp,
                            color = textColor,
                            modifier = Modifier.testTag("Header: $headerText")
                        )
                    }
                }
                if (subheaderText != "") {
                    Spacer(Modifier.size(5.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(backgroundColor)
                    ) {
                        Spacer(Modifier.size(60.dp))
                        Text(
                            text = subheaderText,
                            fontSize = subheaderTextSize.sp,
                            color = textColor
                        )
                    }
                }
            } else {
                Spacer(Modifier.height(30.dp))
                customContent() // takes up 90.dp
            }
        }
    }
    @Composable
    protected fun Footer(
        navController: NavController,
        backgroundColor: Color,
        textColor: Color,
        buttonColor: Color,
        excludeButtons: Boolean = false,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(backgroundColor)
        ) {
            Spacer(Modifier.size(25.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(backgroundColor)
            ) {
                if (!excludeButtons) {
                    Spacer(Modifier.size(40.dp))
                    ElevatedButton(
                        onClick = {
                            // only go back if there is a screen to go back to
                            if (navController.previousBackStackEntry != null) {
                                navController.popBackStack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                    ) {
                        Text(
                            text = "Back",
                            color = textColor,
                        )
                    }
                    Spacer(Modifier.size(40.dp))
                    ElevatedButton(
                        onClick = { navController.goHome() },
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        modifier = Modifier.testTag("Home"),
                    ) {
                        Text(
                            text = "Home",
                            color = textColor,
                        )
                    }
                    Spacer(Modifier.size(40.dp))
                    ElevatedButton(
                        onClick = { navController.navigate(Routes.PROFILE) },
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        modifier = Modifier.testTag("Profile"),
                    ) {
                        Text(
                            text = "Profile",
                            color = textColor,
                        )
                    }
                }
            }
        }
    }

    /**
     * Component to display information and actions about a certain object
     * Super useful for lists of things, such as reservations and lotteries
     *
     * @param primaryColor background color of the upper half of the card
     * @param accentColor background color of the lower half of the card
     * @param primaryContent content in the upper half of the card
     * @param secondaryContent content in the lower half of the card, typically a few functions
     *
     */
    @Composable
    protected fun DuotoneCard(
        primaryColor: Color = cardPrimary,
        accentColor: Color = cardAccent,
        primaryContent: () -> Unit = {},
        secondaryContent: () -> Unit = {},
    ) {
        Column(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(12.dp))
                .background(color = primaryColor)
                .fillMaxWidth()
                .heightIn(min = 150.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
                    .weight(1f),
            ) {
                primaryContent()
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)  // hard height constraint by design
                    .background(color = accentColor)
                    .padding(10.dp),
            ) {
                secondaryContent()
            }
        }

    }

    @Composable
    protected fun TabManager(tabs: List<Tab>, state: String) {
        BoxWithConstraints(
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth()
        ) {
            /*
            * The following value was defined by Gemini 3.6 Flash
            * "I want the tab column in TabCol in View.kt to have a minimum width of 120.dp,
            *  but also have each tab be equally sized to fill the max width of the screen."
            * 2026-11-22
            * */
            val tabWidth = if (tabs.isNotEmpty()) {
                maxOf(120.dp, maxWidth / tabs.size)
            } else {
                120.dp
            }
            LazyRow(
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
            ) {
                itemsIndexed(tabs) { index, tab: Tab ->
                    TabCol(tab = tab, state = state, modifier = Modifier.width(tabWidth))
                }
            }
        }
    }
    @Composable
    private fun TabCol(tab: Tab, state: String, modifier: Modifier = Modifier) {
        Column(
            modifier = modifier
                .fillMaxHeight()
                .widthIn(min = 120.dp)
                .clickable(enabled = true, onClick = { tab.option(state) })
        ) {
            Row(
                modifier = Modifier
                    .background(tab.color)
                    .fillMaxSize()
                    .weight(1f)
            ) {
                Spacer(modifier = Modifier.width(10.dp))
                Column(Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = tab.name,
                        fontSize = 25.sp,
                        color = primaryGreenTextColor,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                }
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
    }
}
class Tab(var name: String, var color: Color, var option: (String) -> Unit) {}
class Option(var name: String, var route: String) {}
