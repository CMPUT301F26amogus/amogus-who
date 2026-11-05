package com.example.code.Views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.insert
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.then
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly


interface InputFieldFunctions {
    /** variableDigitIntField
     *
     * draws a TextField that specifically limits it's input
     * to ints of a specified number of digits long
     *
     * @param labelText     the text displayed as the TextField's label
     * @param inputLen      the length or number of digits an input is allowed to be
     * @param mod           the modifier of the TextField, lets you designate aspects like
     *                      its orientation and colour
     * @return Int          the int inputted to the TextField
     * */
    @Composable
    fun variableDigitIntField(
        labelText: String,
        inputLen: Int,
        mod: Modifier
    ): Int {
        val thisState = rememberTextFieldState("")
        TextField(
            state = thisState,
            label = {
                Text(labelText)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),
            inputTransformation = InputTransformation.maxLength(inputLen).then {
                if (!asCharSequence().isDigitsOnly()) {
                    revertAllChanges()
                }
            },
            modifier = mod
        )
        return thisState.text.toString().toInt()
    }

    /** dateField
     *
     * draws a TextField that takes a date input as a numiceral input of
     * XX/XX/XXXX or Mo/Da/Year, returns string of 8 number characters
     *
     * draws a TextField which specifically limits input
     * to ints 8 digits long, 2 for month + 2 for day + 4 for year
     * additionally it will automatically add '/' characters to divide
     * month/day/year for only the user
     *
     * @param labelText     the text displayed as TextField's label
     * @param mod           the modifier of the TextField, lets you designate aspects like
     *                      its orientation and colour
     * @return String       8 character string of digits representing a date, 2 for month + 2 for day + 4 for year
     * */
    @Composable
    fun dateField(
        labelText: String,
        mod: Modifier
    ): String {
        val thisState = rememberTextFieldState("")
        TextField(
            state = thisState,
            label = { Text(labelText) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),
            placeholder = {
                Text("Mo/Da/Year")
            },
            inputTransformation = InputTransformation.maxLength(8).then {
                if (!asCharSequence().isDigitsOnly()) {
                    revertAllChanges()
                }
            },
            outputTransformation = OutputTransformation {
                if (length > 2) insert(2, "/")
                if (length > 5) insert(5, "/")
            },
            modifier = mod
        )
        return thisState.text.toString()
    }
}