package com.example.code.views

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.insert
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.then
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.text.isDigitsOnly
import java.util.Date
import kotlin.text.substring


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
        s: TextFieldState,
        labelText: String,
        inputLen: Int,
        modifier: Modifier,
        placeholder: String = ""
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
                Text(placeholder)
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
        s: TextFieldState,
        labelText: String,
        modifier: Modifier,
        placeholder: String = "Year/Mo/Da"
    ) {
        TextField(
            state = s,
            label = { Text(labelText) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),
            placeholder = {
                Text(placeholder)
            },
            inputTransformation = InputTransformation.maxLength(8).then {
                if (!asCharSequence().isDigitsOnly()) {
                    revertAllChanges()
                }
            },
            outputTransformation = OutputTransformation {
                if (length > 2) insert(4, "/")
                if (length > 5) insert(7, "/")
            },
            modifier = modifier
        )
    }

    @Composable
    fun StringField(
        s: TextFieldState,
        labelText: String,
        modifier: Modifier,
        placeholder: String = ""
    ) {
        TextField(
            state = s,
            label = { Text(labelText) },
            modifier = modifier,
            placeholder = { Text(placeholder) }
        )
    }

    /**
     * Helper function for taking in money based inputs.
     * Restricts the user to inputting digits and displays the last two inputted digits
     * as 10 and 1 cent values*/
    @Composable
    fun MoneyField(
        s: TextFieldState,
        labelText: String,
        modifier: Modifier,
        placeholder: String = ""
    ) {
        TextField(
            state = s,
            label = { Text(labelText) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),
            placeholder = {
                Text(placeholder)
            },
            inputTransformation = InputTransformation.maxLength(8).then {
                if (!asCharSequence().isDigitsOnly()) {
                    revertAllChanges()
                }
            },
            outputTransformation = OutputTransformation {
                if (length == 1) {insert(0, "0.0")}
                else if (length == 2) {insert(0, "0.")}
                else if (length > 2) {insert(length-2, ".")}
            },
            modifier = modifier
        )
    }

    fun textStateToDouble(s: TextFieldState): Double {
        val old = s.text.toString()
        var out = "0.00"
        if (old.length == 1) {
            out = "0.0%s".format(old)
        }
        else if (old.length == 2) {
            out = "0.%s".format(old)
        }
        else if (old.length > 2) {
            out = "%s.%s".format(old.substring(0, (old.length - 3)), old.substring(old.length - 2, old.length-1))
        }
        return out.toDouble()
    }


    fun dateToTextState(s: String): Date {
        if (s.length == 8) {
            val mo: String = s.substring(0, 1)
            val da: String = s.substring(2, 3)
            val year: String = s.substring(4, 7)

            val d = Date(year.toInt() + 1900, mo.toInt(), da.toInt())
            return d
        }
        return Date()
    }

    /**
     * Takes a textState, which presumably contains an 8 character long string of digits
     * if it is 8 characters long then it will add dashes between the year, month, and date sections
     * for input into the database
     * */
    fun textStateToDate(s: TextFieldState): String {
        val old: String = s.text.toString()
        if (old.length == 8) {
            val year: String = old.substring(0, 3)
            val mo: String = old.substring(4, 5)
            val da: String = old.substring(6, 7)

            return "%04d-%02d-%02d".format(year, mo, da)
        }else {
            return "0000-00-00"
        }
    }
}

