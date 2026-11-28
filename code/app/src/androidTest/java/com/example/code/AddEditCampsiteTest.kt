package com.example.code

import Model
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.model.Campsite
import com.example.code.views.AddEditCampsite
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddEditCampsiteTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    /* Gemini 3.6 Flash
     * "There seem to be a ton of files in the test suite that also have build errors, please go through them and apply a fix."
     * 2026-11-11
     */
    @Test
    fun Check() {
        val model = Model()
        val aView = AddEditCampsite(model)
    }

    // the isEdit flag was taken out so the view picks add or edit from the models temp campsite
    @Test
    fun noTempCampsite_isAddMode() {
        val model = Model()
        model.settTempCampsite(null)
        val aView = AddEditCampsite(model)
        aView.update(model)
        assertEquals("Add Campsite", aView.headerName)
    }

    @Test
    fun tempCampsite_isEditMode() {
        val model = Model()
        model.settTempCampsite(Campsite(name = "Pine Ridge", location = "Jasper", maxOccupancy = 6))
        val aView = AddEditCampsite(model)
        aView.update(model)
        assertEquals("Edit Campsite", aView.headerName)
        assertEquals("Pine Ridge", aView.campsiteName)
        assertEquals("6", aView.maxGuests)
    }
}