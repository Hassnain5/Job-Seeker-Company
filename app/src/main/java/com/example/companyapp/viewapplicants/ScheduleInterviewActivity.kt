
package com.example.companyapp.viewapplicants

import android.annotation.SuppressLint
import com.example.companyapp.adapter.TimeSlotAdapter
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.companyapp.R
import com.example.companyapp.databinding.ActivityScheduleInterviewBinding
import com.example.companyapp.models.TimeSlot
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ScheduleInterviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleInterviewBinding
    private val timeSlots = mutableListOf<TimeSlot>()
    private lateinit var timeSlotAdapter: TimeSlotAdapter
    private val calendar = Calendar.getInstance()
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleInterviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        TestSlotMatching.testSlotMatching(this)
        val jobId = intent.getStringExtra("JOB_ID")
        val userEmail = intent.getStringExtra("User_Email")
        setupRecyclerView()
        setupDateAndTimePickers()
        checkForBookedSlot()
        setupButtons()
    }

    private fun setupRecyclerView() {
        timeSlotAdapter = TimeSlotAdapter(timeSlots) { slotToRemove ->
            val position = timeSlots.indexOf(slotToRemove)
            if (position != -1) {
                timeSlots.removeAt(position)
                timeSlotAdapter.notifyItemRemoved(position)
                updateSlotsUI()
            }
        }

        binding.rvTimeSlots.apply {
            layoutManager = LinearLayoutManager(this@ScheduleInterviewActivity)
            adapter = timeSlotAdapter
            setHasFixedSize(true)
            itemAnimator = DefaultItemAnimator()
        }
    }

    private fun setupDateAndTimePickers() {
        binding.etDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, day)

                    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    binding.etDate.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.minDate = System.currentTimeMillis() - 1000
                show()
            }
        }

        binding.etTime.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)

                    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    binding.etTime.setText(timeFormat.format(calendar.time))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        }
    }

    private fun setupButtons() {
        binding.btnAddSlot.setOnClickListener {
            val date = binding.etDate.text.toString().trim()
            val time = binding.etTime.text.toString().trim()

            if (date.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "Please select both date and time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (timeSlots.size >= 5) {
                Toast.makeText(this, "Maximum 5 slots allowed", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            checkSlotAvailability(date, time)
        }

        binding.btnSubmit.setOnClickListener {
            if (timeSlots.size < 3) {
                Toast.makeText(this, "Please add at least 3 time slots", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveSlotsToFirebase()
            Toast.makeText(this, "${timeSlots.size} slots submitted successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun checkForExistingSlots() {
        val jobId = intent.getStringExtra("JOB_ID") ?: return
        val userEmail = intent.getStringExtra("User_Email") ?: return
        val firebaseSafeEmail = userEmail.replace(".", "_")

        val progressDialog = ProgressDialog(this).apply {
            setMessage("Checking for existing slots...")
            setCancelable(false)
            show()
        }

        database = FirebaseDatabase.getInstance().reference
        database.child("InterviewSlots")
            .child(jobId)
            .child(firebaseSafeEmail)
            .get()
            .addOnSuccessListener { snapshot ->
                progressDialog.dismiss()

                if (snapshot.exists()) {
                    // User already has slots for this job
                    timeSlots.clear()

                    snapshot.children.forEach { slotSnapshot ->
                        val date = slotSnapshot.child("date").getValue(String::class.java) ?: ""
                        val time = slotSnapshot.child("time").getValue(String::class.java) ?: ""
                        val dayOfWeek = slotSnapshot.child("dayOfWeek").getValue(String::class.java) ?: ""

                        if (date.isNotEmpty() && time.isNotEmpty()) {
                            timeSlots.add(TimeSlot(date, time, dayOfWeek))
                        }
                    }

                    timeSlotAdapter.notifyDataSetChanged()
                    updateSlotsUI()

                    // Hide the add slots layout
                    binding.layoutAddSlots.visibility = View.GONE
                    binding.btnSubmit.visibility = View.GONE
                } else {
                    // No existing slots, show the add slots layout
                    binding.layoutAddSlots.visibility = View.VISIBLE
                    binding.btnSubmit.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to check existing slots", Toast.LENGTH_SHORT).show()
                // On failure, still allow adding slots
                binding.layoutAddSlots.visibility = View.VISIBLE
                binding.btnSubmit.visibility = View.VISIBLE
            }
    }

    private fun checkForBookedSlot() {
        val jobId = intent.getStringExtra("JOB_ID") ?: run {
            Toast.makeText(this, "Job ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        val applicantEmail = intent.getStringExtra("User_Email") ?: run {
            Toast.makeText(this, "Applicant email not found", Toast.LENGTH_SHORT).show()
            return
        }
        val processedEmail = applicantEmail.replace("_", ".")

        val progressDialog = ProgressDialog(this).apply {
            setMessage("Checking for booked slot...")
            setCancelable(false)
            show()
        }

        database = FirebaseDatabase.getInstance().reference
        database.child("BookedSlots").get()
            .addOnSuccessListener { snapshot ->
                progressDialog.dismiss()

                var bookedSlotFound = false
                var bookedDate = ""
                var bookedTime = ""

                if (!snapshot.exists()) {
                    Log.d("BookedSlotCheck", "No booked slots found at all")
                    checkForExistingSlots()
                    return@addOnSuccessListener
                }

                for (slotNode in snapshot.children) {
                    val bookedJobId = slotNode.child("job_id").getValue(String::class.java)
                    val bookedApplicantEmail = slotNode.child("applicant_email").getValue(String::class.java)
                    val slotDate = slotNode.child("Date").getValue(String::class.java)
                    val slotTime = slotNode.child("Time").getValue(String::class.java)

                    Log.d("BookedSlotCheck", "Checking slot: Job=$bookedJobId, Applicant=$bookedApplicantEmail")

                    if (bookedJobId == jobId && bookedApplicantEmail == processedEmail) {
                        bookedSlotFound = true
                        bookedDate = slotDate ?: ""
                        bookedTime = slotTime ?: ""
                        Log.d("BookedSlotCheck", "Found booked slot: $bookedDate $bookedTime")
                        break
                    }
                }

                if (bookedSlotFound) {
                    // Convert date format from "dd-MM-yyyy" to "dd MMM yyyy" to match TimeSlot format
                    val formattedDate = try {
                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(
                            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(bookedDate))
                    } catch (e: Exception) {
                        bookedDate
                    }

                    // Convert time format from "HH:mm" to "hh:mm a" to match TimeSlot format
                    val formattedTime = try {
                        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(
                            SimpleDateFormat("HH:mm", Locale.getDefault()).parse(bookedTime))
                    } catch (e: Exception) {
                        bookedTime
                    }

                    // Get day of week
                    val dayOfWeek = try {
                        SimpleDateFormat("EEEE", Locale.getDefault()).format(
                            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(bookedDate))
                    } catch (e: Exception) {
                        ""
                    }

                    // Clear existing slots and show only the booked slot
                    timeSlots.clear()
                    timeSlots.add(TimeSlot(formattedDate, formattedTime, dayOfWeek))
                    timeSlotAdapter.notifyDataSetChanged()
                    updateSlotsUI()

                    // Disable adding more slots
                    binding.layoutAddSlots.visibility = View.GONE
                    binding.btnSubmit.visibility = View.GONE

                    Toast.makeText(this, "Showing booked interview slot", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d("BookedSlotCheck", "No booked slot found for this job and applicant")
                    checkForExistingSlots()
                }
            }
            .addOnFailureListener { exception ->
                progressDialog.dismiss()
                Log.e("BookedSlotCheck", "Error checking booked slots", exception)

                val errorMsg = when (exception) {
                    is DatabaseException -> "Database error"
                    is FirebaseNetworkException -> "Network unavailable"
                    else -> "Failed to check booking status"
                }

                Toast.makeText(this, "$errorMsg. Showing interview slots...", Toast.LENGTH_LONG).show()
                // Fallback to checking existing slots
                checkForExistingSlots()
            }
    }


    private fun saveSlotsToFirebase() {
        val jobId = intent.getStringExtra("JOB_ID") ?: return
        val userEmail = intent.getStringExtra("User_Email") ?: return

        if (timeSlots.isEmpty()) {
            Toast.makeText(this, "No slots to save", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSubmit.isEnabled = false
        database = FirebaseDatabase.getInstance().reference.child("InterviewSlots")

        val slotsRef = database.child(jobId).child(userEmail.replace(".", ","))

        timeSlots.forEach { slot ->
            val slotData = hashMapOf(
                "date" to slot.date,
                "time" to slot.time,
                "dayOfWeek" to slot.dayOfWeek,
                "createdAt" to ServerValue.TIMESTAMP
            )

            slotsRef.push().setValue(slotData)
                .addOnSuccessListener {
                    Log.d("Firebase", "Slot saved successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Error saving slot", e)
                }
        }.also {
            Toast.makeText(this, "${timeSlots.size} slots submitted", Toast.LENGTH_SHORT).show()
            finish()
        }
        updateApplicationStatus("Interview",jobId)

    }
    private fun updateApplicationStatus(newStatus: String,targetJobId : String) {
        targetJobId?.let { jobId ->
            val database = FirebaseDatabase.getInstance()
            val appliedJobsRef = database.getReference("AppliedJobs")

            val query = appliedJobsRef.orderByChild("job_id").equalTo(jobId)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (jobSnapshot in dataSnapshot.children) {
                        jobSnapshot.ref.child("job_status")
                            .setValue("${jobSnapshot.child("job_status").value} , $newStatus")
                            .addOnSuccessListener {
                                Log.d("Firebase", "Job status updated successfully")
                                runOnUiThread {

                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firebase", "Error updating job status", e)
                                runOnUiThread {
                                    Toast.makeText(this@ScheduleInterviewActivity, "Failed to update status", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Query cancelled", databaseError.toException())
                    runOnUiThread {
                        Toast.makeText(this@ScheduleInterviewActivity, "Database error", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }
    private fun checkSlotAvailability(date: String, time: String) {
        val jobId = intent.getStringExtra("JOB_ID") ?: return
        val currentUserEmail = getCurrentUserEmail() // Firebase key-safe email
        Log.d("CheckSlot", "CurrentUserEmail: $currentUserEmail, JobId: $jobId, Date: $date, Time: $time")

        val progressDialog = ProgressDialog(this).apply {
            setMessage("Checking slot availability...")
            setCancelable(false)
            show()
        }
        database = FirebaseDatabase.getInstance().reference

        database.child("BookedSlots").get()
            .addOnSuccessListener { snapshot ->
                progressDialog.dismiss()
                var isAvailable = true
                var conflictDate = ""
                var conflictTime = ""

                if (!snapshot.exists()) {
                    Log.d("CheckSlot", "No booked slots found")
                }

                for (slotNode in snapshot.children) {
                    val bookedCompanyEmail = slotNode.child("company_email").getValue(String::class.java)
                    val bookedJobId = slotNode.child("job_id").getValue(String::class.java)
                    val bookedDate = slotNode.child("Date").getValue(String::class.java)
                    val bookedTime = slotNode.child("Time").getValue(String::class.java)

                    Log.d("CheckSlot", "Slot: CompanyEmail=$bookedCompanyEmail, JobId=$bookedJobId, Date=$bookedDate, Time=$bookedTime")

                    if (bookedCompanyEmail == currentUserEmail && bookedJobId == jobId) {
                        if (bookedDate == date) {
                            val bookedTimeInMinutes = convertTimeToMinutes(bookedTime ?: "")
                            val userTimeInMinutes = convertTimeToMinutes(time)
                            Log.d("CheckSlot", "BookedTimeInMinutes=$bookedTimeInMinutes, UserTimeInMinutes=$userTimeInMinutes")

                            if (bookedTimeInMinutes == -1 || userTimeInMinutes == -1) {
                                Log.e("CheckSlot", "Skipping slot due to invalid time format")
                                continue
                            }

                            if (userTimeInMinutes == bookedTimeInMinutes ||
                                (userTimeInMinutes > bookedTimeInMinutes &&
                                        userTimeInMinutes <= bookedTimeInMinutes + 30) ||
                                (userTimeInMinutes < bookedTimeInMinutes &&
                                        userTimeInMinutes >= bookedTimeInMinutes - 30)) {
                                isAvailable = false
                                conflictDate = bookedDate ?: ""
                                conflictTime = bookedTime ?: ""
                                Log.d("CheckSlot", "Conflict detected")
                                break
                            }
                        }
                    }
                }

                if (isAvailable) {
                    Toast.makeText(this, "Slot is available", Toast.LENGTH_SHORT).show()
                    Log.d("CheckSlot", "No conflict, adding slot")
                    addSlotToLocalList(date, time)
                } else {
                    showBookedSlotPopup(conflictDate, conflictTime)
                }
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Failed to check availability. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("CheckSlot", "Firebase query failed", it)
            }
    }

    private fun showBookedSlotPopup(bookedDate: String, bookedTime: String) {
        val dialog = Dialog(this).apply {
            setContentView(R.layout.booked_slot_popup_dialogue)
            setCancelable(true)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        val tvWarningMessage = dialog.findViewById<TextView>(R.id.tv_warning_massage)
        val btnOk = dialog.findViewById<Button>(R.id.tv_btn_okky)

        // Format the date and time for display
        val formattedDate = try {
            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val date = inputFormat.parse(bookedDate)
            val outputFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
            outputFormat.format(date!!)
        } catch (e: Exception) {
            bookedDate
        }

        val formattedTime = try {
            val inputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val time = inputFormat.parse(bookedTime)
            val outputFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            outputFormat.format(time!!)
        } catch (e: Exception) {
            bookedTime
        }

        // Create the full message with highlighted parts
        val fullMessage = "Slot is already booked on $formattedDate, $formattedTime. Please try 30 minutes before or after this time."
        val spannableMessage = SpannableString(fullMessage)

        // Find positions of date and time in the message
        val dateStart = fullMessage.indexOf(formattedDate)
        val dateEnd = dateStart + formattedDate.length
        val timeStart = fullMessage.indexOf(formattedTime)
        val timeEnd = timeStart + formattedTime.length

        // Apply bold and color to date
        spannableMessage.setSpan(
            StyleSpan(Typeface.BOLD),
            dateStart,
            dateEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableMessage.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.blue500)),
            dateStart,
            dateEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Apply bold and color to time
        spannableMessage.setSpan(
            StyleSpan(Typeface.BOLD),
            timeStart,
            timeEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableMessage.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.blue500)),
            timeStart,
            timeEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        tvWarningMessage.text = spannableMessage

        btnOk.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun getCurrentUserEmail(): String {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        return sharedPref.getString("user_email", "") ?: ""
    }

    private fun convertTimeToMinutes(timeString: String): Int {
        return try {
            // Normalize time string: remove extra spaces
            val normalizedTime = timeString.trim().replace("\\s+".toRegex(), " ")
            Log.d("TimeConversion", "Parsing time: $normalizedTime")
            val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val date = format.parse(normalizedTime) ?: throw Exception("Invalid time format")
            Calendar.getInstance().apply { time = date }.let {
                val minutes = it.get(Calendar.HOUR_OF_DAY) * 60 + it.get(Calendar.MINUTE)
                Log.d("TimeConversion", "Converted $normalizedTime to $minutes minutes")
                minutes
            }
        } catch (e: Exception) {
            Log.e("TimeConversion", "Error parsing time: $timeString", e)
            -1 // Return invalid value to skip conflict check
        }
    }

    private fun updateSlotsUI() {
        binding.tvSlotsCount.text = "(${timeSlots.size}/5)"
        binding.btnSubmit.isEnabled = timeSlots.size >= 3
    }

    private fun addSlotToLocalList(date: String, time: String) {
        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val dateObj = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(date)
        val dayOfWeek = dayFormat.format(dateObj ?: return)

        val newSlot = TimeSlot(date, time, dayOfWeek)
        timeSlots.add(newSlot)
        timeSlotAdapter.notifyDataSetChanged()
        updateSlotsUI()

        binding.etDate.text?.clear()
        binding.etTime.text?.clear()

        binding.rvTimeSlots.smoothScrollToPosition(timeSlots.size - 1)
    }
}
