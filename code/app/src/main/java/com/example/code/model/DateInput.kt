package com.example.code.model

import java.time.LocalDate

/**
 * Reads dates the way people type them into the app's date boxes.
 *
 * The boxes say mm/dd/yy, and DateField only keeps the digits, so this accepts
 * "06/01/2027", "06012027", "06/01/27" or "060127". Dates are saved as YYYY-MM-DD
 * (like lotteries), which is also accepted.
 */
class DateInput {
    /**
     * @return the date, or null if the text is blank or isn't a real date
     */
    fun toLocalDate(text: String): LocalDate? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null
        // already saved as YYYY-MM-DD
        if (trimmed.contains("-")) {
            return runCatching { LocalDate.parse(trimmed) }.getOrNull()
        }
        val digits = trimmed.filter { it.isDigit() }
        val month = digits.take(2).toIntOrNull() ?: return null
        val day = digits.drop(2).take(2).toIntOrNull() ?: return null
        val year = when (digits.length) {
            8 -> digits.drop(4).toInt()
            6 -> 2000 + digits.drop(4).toInt()
            else -> return null
        }
        return runCatching { LocalDate.of(year, month, day) }.getOrNull()
    }

    /**
     * @return the date as YYYY-MM-DD for saving, or "" if it isn't a real date
     */
    fun toSaved(text: String): String {
        return toLocalDate(text)?.toString() ?: ""
    }
}
