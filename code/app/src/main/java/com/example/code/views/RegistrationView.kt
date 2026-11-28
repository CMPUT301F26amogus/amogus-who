    package com.example.code.views

import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.code.Routes
import View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.code.model.Role
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.code.controllers.RegistrationController
import com.example.code.ui.theme.CodeTheme
import Model
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.sp

    class RegistrationView(M:Model): View() {
    var controller = RegistrationController(M)
    val tos: String = """
        According to all known laws of aviation, there is no way a bee should be able to fly. It’s wings are too small to get its fat little body off the ground. 

        The bee, of course, flies anyway, because bees don’t care what humans think is impossible.
        Yellow, black.
        Yellow, black.
        Yellow, black.
        Yellow, black. Ooh, black and yellow!

        Let’s shake it up a little.
        Barry! Breakfast is ready!
        Coming!
        Hang on a second.

        Hello?
        Barry?
        Adam?
        Can you believe this is happening?
        I can't.
        I'll pick you up.
        Looking sharp.
        Use the stairs, Your father paid good money for those.
        Sorry. I'm excited.
        Here's the graduate.
        We're very proud of you, son.
        A perfect report card, all B's.
        Very proud.
        Ma! I got a thing going here.
        You got lint on your fuzz.
        Ow! That's me!
        Wave to us! We'll be in row 118,000.
        Bye!
        Barry, I told you, stop flying in the house!
        Hey, Adam.
        Hey, Barry.
        Is that fuzz gel?
        A little. Special day, graduation.
        Never thought I'd make it.
        Three days grade school, three days high school.
        Those were awkward.
        Three days college. I'm glad I took a day and hitchhiked around The Hive.
        You did come back different.
        Hi, Barry. Artie, growing a mustache? Looks good.
        Hear about Frankie?
        Yeah.
    """.trimIndent()

    @Composable
    public fun ShowScreen(navController: NavController) {
        var showInputs by remember { mutableStateOf(true) }
        var acceptPolicy by remember {mutableStateOf(false)}
        Column() {
            Row() {  //  Header Row
                Header(
                    headerText = "Register",
                    backgroundColor = entrantHeaderBackgroundColor,
                    textColor = entrantHeaderTextColor,
                )
            }
            Row(modifier = Modifier.weight(1f)){  //  Body Row
                if (showInputs) {
                    InputBody(
                        acceptPolicy = acceptPolicy,
                        onSuccess = {
                            goToHome(navController, Routes.OWNER_HOME)
                        },
                        onTos = {
                            showInputs = !showInputs
                        }
                    )
                }
                else {
                    TosBody (
                        acceptPolicy = acceptPolicy,
                        onAccept = {
                            acceptPolicy = true
                            showInputs = !showInputs
                        },
                        onDecline = {
                            acceptPolicy = false
                            showInputs = !showInputs
                        }
                    )
                }
            }
            Row() {  //  Footer Row
                Footer(
                    navController = navController,
                    textColor = entrantFooterTextColor,
                    backgroundColor = entrantFooterBackgroundColor,
                    buttonColor = entrantFooterButtonColor,
                    excludeButtons = true
                )
            }
        }
    }

    /* Gemini 3.6 Flash
     * "There are build errors in RegistrationView.kt and OwnerEditCampsiteController.kt, can you find them and apply some fixes to them?"
     * 2026-11-11
     */
    private fun goToHome(navController: NavController, homeRoute: String) {
        navController.navigate(homeRoute) {
            // clear every screen before this, not just Register, so the new home is the only one left.
            // otherwise after Delete Profile -> Register, the old home stays underneath and the
            // footer's Home button (which goes to the first screen) takes an owner to the entrant home
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    @Composable
    fun TosBody(acceptPolicy: Boolean, onAccept: () -> Unit, onDecline: () -> Unit) {
        Column(
            modifier = Modifier
                .padding(top = 50.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column (
                modifier = Modifier
                    .background(
                        color = cardAccent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        width = 2.dp,
                        color = primaryGreenTextColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .height(500.dp)
                    .verticalScroll(rememberScrollState())

            ){
                Text(
                    tos,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp,
                    color = primaryTextColor
                )
            }
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ActionButton(
                    onPress = onDecline,
                    textColor = textColor,
                    label = "Decline",
                    tag = "DeclineButton"
                ).Draw()

                Row (
                    modifier = Modifier.padding (horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ){
                    ActionButton(
                        onPress = onAccept,
                        textColor = textColor,
                        label = "Accept",
                        tag = "AcceptButton"
                    ).Draw()
                    Checkbox(
                        checked = acceptPolicy,
                        onCheckedChange = {},
                        enabled = false,
                    )
                }
            }
        }
    }

    @Composable
    fun InputBody(acceptPolicy: Boolean, onSuccess: () -> Unit, onTos: () -> Unit) {
        var name by remember {
            mutableStateOf("")
        }
        var number by remember {
            mutableStateOf("")
        }
        var email by remember {
            mutableStateOf("")
        }
        // what's wrong with the form, shown under the buttons
        var errorMessage by remember {
            mutableStateOf<String?>(null)
        }
        Column(
            modifier = Modifier
                .padding(top = 50.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column {
                Text(
                    "Name",
                    modifier = Modifier
                         ,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                TextField(
                    value = name,
                    onValueChange = {
                        name = it
                    },
                    label = {
                        Text("Enter your full name")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column {
                Text(
                    "Email",
                    modifier = Modifier ,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                TextField(
                    value = email,
                    onValueChange = {
                        email = it
                    },
                    label = {
                        Text("Enter your email")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column {
                Text(
                    "Phone Number",
                    modifier = Modifier ,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                TextField(
                    value = number,
                    onValueChange = {
                        number = it
                    },
                    label = {
                        Text("Enter your phone number")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(){
                Text(
                    "Terms and Conditions",
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ){
                    ElevatedButton(
                        onClick = {
                            onTos()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xffe8def8)),
                        modifier = Modifier.height(60.dp)
                    ) {
                        Text(text = "View Terms and Conditions", color = Color(0xff40139a))
                    }
                    Checkbox(
                        modifier = Modifier.height(45.dp).width(45.dp),
                        checked = acceptPolicy,
                        onCheckedChange = {},
                        enabled = false,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ElevatedButton(
                    onClick = {
                        errorMessage = controller.register(name = name, email = email, phoneNumber = number)
                        if (errorMessage == null) {
                            if (!acceptPolicy) {
                                errorMessage = "Please Read and Accept TOS"
                            }
                            else {
                                onSuccess()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xffe8def8)),
                    modifier = Modifier
                        .width(160.dp)
                        .height(60.dp)
                         ,
                ) {
                    Text(text = "Register As Entrant", color = Color(0xff40139a))
                }
                ElevatedButton(
                    onClick = {
                        errorMessage = controller.register(name = name, email = email, phoneNumber = number, role = Role.OWNER)
                        if (errorMessage == null) {
                            if (!acceptPolicy) {
                                errorMessage = "Please Read and Accept TOS"
                            }
                            else {
                                onSuccess()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xffe8def8)),
                    modifier = Modifier
                        .width(160.dp)
                        .height(60.dp) ,
                ) {
                    Text(text = "Register As Owner", color = Color(0xff40139a))
                }
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = Color(0xffb3261e),
                    modifier = Modifier .testTag("registerError"),
                )
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun RegistrationViewPreview() {
    val M = Model()
    val mp = RegistrationView(M)
    CodeTheme {
        (
        mp.ShowScreen(rememberNavController())
        )
    }
}