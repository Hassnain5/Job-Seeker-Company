
package com.example.companyapp.viewapplicants

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object TestSlotMatching {

    fun testSlotMatching(context: Context) {
        // Hardcoded booked slot (from BookedSlots)
        val bookedCompanyEmail = "muhammadxhah249@gmail.com".replace(".", ",")
        val bookedJobId = "-OP9b5wEmkMpC6LOC4LJ"
        val bookedDate = "30 May 2025"
        val bookedTime = "12:00 PM" // Normalized from "12: 00 PM"

        // Hardcoded input slot
        val inputCompanyEmail = "muhammadxhah249@gmail.com".replace(".", ",")
        val inputJobId = "-OP9b5wEmkMpC6LOC4LJ"
        val inputDate = "30 May 2025"
        val inputTime = "12:10 PM"

        Toast.makeText(context, "Testing slot matching...", Toast.LENGTH_SHORT).show()
        Toast.makeText(
            context,
            "Booked: Email=$bookedCompanyEmail, JobId=$bookedJobId, Date=$bookedDate, Time=$bookedTime",
            Toast.LENGTH_LONG
        ).show()
        Toast.makeText(
            context,
            "Input: Email=$inputCompanyEmail, JobId=$inputJobId, Date=$inputDate, Time=$inputTime",
            Toast.LENGTH_LONG
        ).show()

        // Step 1: Check email and job ID
        if (bookedCompanyEmail != inputCompanyEmail || bookedJobId != inputJobId) {
            Toast.makeText(context, "No conflict: Email or Job ID mismatch", Toast.LENGTH_SHORT).show()
            Toast.makeText(context, "Slot is available", Toast.LENGTH_SHORT).show()
            return
        }

        // Step 2: Check date
        if (bookedDate != inputDate) {
            Toast.makeText(context, "No conflict: Date mismatch", Toast.LENGTH_SHORT).show()
            Toast.makeText(context, "Slot is available", Toast.LENGTH_SHORT).show()
            return
        }

        // Step 3: Check time conflict
        val bookedTimeInMinutes = convertTimeToMinutes(context, bookedTime)
        val inputTimeInMinutes = convertTimeToMinutes(context, inputTime)

        Toast.makeText(
            context,
            "BookedTimeInMinutes=$bookedTimeInMinutes, InputTimeInMinutes=$inputTimeInMinutes",
            Toast.LENGTH_SHORT
        ).show()

        if (bookedTimeInMinutes == -1 || inputTimeInMinutes == -1) {
            Toast.makeText(context, "Error: Invalid time format, cannot check conflict", Toast.LENGTH_LONG).show()
            return
        }

        if (inputTimeInMinutes == bookedTimeInMinutes ||
            (inputTimeInMinutes > bookedTimeInMinutes && inputTimeInMinutes <= bookedTimeInMinutes + 30)) {
            Toast.makeText(context, "Conflict detected", Toast.LENGTH_SHORT).show()
            Toast.makeText(
                context,
                "Slot unavailable: Already booked at $bookedTime on $bookedDate",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(context, "No conflict: Time difference outside 30-minute window", Toast.LENGTH_SHORT).show()
            Toast.makeText(context, "Slot is available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun convertTimeToMinutes(context: Context, timeString: String): Int {
        return try {
            // Normalize time string: remove extra spaces
            val normalizedTime = timeString.trim().replace("\\s+".toRegex(), " ")
            Toast.makeText(context, "Parsing time: $normalizedTime", Toast.LENGTH_SHORT).show()
            val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
            format.isLenient = false // Strict parsing
            val date = format.parse(normalizedTime) ?: throw Exception("Invalid time format")
            Calendar.getInstance().apply { time = date }.let {
                val minutes = it.get(Calendar.HOUR_OF_DAY) * 60 + it.get(Calendar.MINUTE)
                Toast.makeText(context, "Converted $normalizedTime to $minutes minutes", Toast.LENGTH_SHORT).show()
                minutes
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error parsing time: $timeString", Toast.LENGTH_LONG).show()
            -1 // Return invalid value
        }
    }
}
