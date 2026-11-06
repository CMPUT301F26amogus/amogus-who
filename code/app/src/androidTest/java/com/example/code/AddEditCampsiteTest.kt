package com.example.code

import View
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.tooling.preview.Preview
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.Views.AddEditCampsite
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddEditCampsiteTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    @Composable
    fun Check() {
        val aView = AddEditCampsite(false)
        aView.ShowScreen()

        composeTestRule

    }


}