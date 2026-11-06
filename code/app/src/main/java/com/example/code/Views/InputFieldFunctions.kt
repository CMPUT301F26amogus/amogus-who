package com.example.code.Views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.insert
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.then
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import java.util.Date


interface InputFieldFunctions {
    /** variableDigitIntField
     *
     * draws a TextField that specifically limits it's input
     * to ints of a specified number of digits long
     *
     * @param labelText     the text displayed as the TextField's label
     * @param inputLen      the length or number of digits an input is allowed to be
     * @param modifier           the modifier of the TextField, lets you designate aspects like
     *                      its orientation and colour
     * @return Int          the int inputted to the TextField
     * */
    @Composable
    fun VariableDigitIntField(
        s : TextFieldState,
        labelText: String,
        inputLen: Int,
        modifier: Modifier,
        placeholder : String = ""
    ) {
        TextField(
            state = s,
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
            modifier = modifier,
            placeholder = {
                Text( placeholder )
            }
        )
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
     * @param modifier           the modifier of the TextField, lets you designate aspects like
     *                      its orientation and colour
     * @return String       8 character string of digits representing a date, 2 for month + 2 for day + 4 for year
     * */
    @Composable
    fun DateField(
        s : TextFieldState,
        labelText: String,
        modifier: Modifier,
        placeholder: String = "Mo/Da/Year"
    ){
        TextField(
            state = s,
            label = { Text(labelText) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),
            placeholder = {
                Text( placeholder )
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
            modifier = modifier
        )
    }

    @Composable
    fun StringField(
        s : TextFieldState,
        labelText: String,
        modifier: Modifier,
        placeholder : String = ""
    ) {
        TextField(
            state = s,
            label = { Text(labelText) },
            modifier = modifier,
            placeholder = { Text(placeholder) }
        )
    }


    fun stringToDate(s: String) : Date{
        if (s.length == 8) {
            val mo : String  = s.substring(0, 1)
            val da: String = s.substring(2, 3)
            val year : String = s.substring(4, 7)

            val d = Date(year.toInt() + 1900, mo.toInt(), da.toInt())
            return d
        }
        return Date()
    }
//
    fun dateToString(d: Date) : String {
        var s : String = "%02d%02d%04d"
        val mo : String  = d.month.toString()
        val da: String = d.date.toString()
        val year : String = d.year.toString()

        s = s.format(s, mo, da, year + 1900)
        return s
    }
}

