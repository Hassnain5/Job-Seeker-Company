package com.example.companyapp.viewapplicants

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.companyapp.R
import com.example.companyapp.adapter.ApplicantsViewPagerAdapter
import com.example.companyapp.databinding.ActivityApplicantsViewProfileBinding
import com.google.android.gms.tasks.Tasks
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ApplicantsViewProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityApplicantsViewProfileBinding
    private val storage = FirebaseStorage.getInstance().reference
    private val db = FirebaseDatabase.getInstance().reference
    private var pendingStatusUpdate: String? = null
    private var targetJobId: String? = null
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityApplicantsViewProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get user data from intent
        userId = intent.getStringExtra("USER_ID") ?: ""
        val userName = intent.getStringExtra("USER_NAME") ?: ""
        targetJobId = intent.getStringExtra("JOB_ID") ?: ""
        val applicationStatus = intent.getStringExtra("Application_Status") ?: ""

        Toast.makeText(this, "Status: $applicationStatus", Toast.LENGTH_SHORT).show()

        if (applicationStatus == "Applied") {
            binding.btnHier.text = "Shortlist This Applicant"
            binding.btnHier.setOnClickListener {
                showConfirmationDialog("Shortlisted")
            }
        } else if (applicationStatus == "Shortlisted") {
            binding.btnHier.text = "Schedule Interview"
            binding.btnHier.setOnClickListener {
                // showConfirmationDialog("Interview")
                val intent = Intent(this, ScheduleInterviewActivity::class.java)
                intent.putExtra("JOB_ID", targetJobId)
                intent.putExtra("User_Email", userId)
                startActivity(intent)
            }
        } else if (applicationStatus == "Interview") {
            binding.btnHier.text = "See Interview Slots"
            checkBookedSlotAndCompareTime()
//            binding.btnHier.text = "See Interview Slots"
            binding.btnHier.setOnClickListener {
                showConfirmationDialog("Hired")
            }
        } else {
            binding.btnHier.visibility = View.GONE
        }

        binding.btnReject.setOnClickListener {
            showConfirmationDialog("Rejected")
        }

        binding.tvProfileTitle.text = userName
        loadProfileImage(userId!!.replace(".", "_"))
        setupViewPager(userId!!, targetJobId ?: "")
    }

    private fun setupViewPager(userId: String , jobId: String) {
        val adapter = ApplicantsViewPagerAdapter(this, userId , jobId)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "CV"
                1 -> "Education"
                2 -> "Experience"
                else -> ""
            }
        }.attach()
    }

    private fun loadProfileImage(userId: String) {
        val profileImageRef = storage.child("User/$userId/profile.jpg")

        profileImageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.profile_img)
                .error(R.drawable.profile_img)
                .into(binding.ivProfileImage)
        }.addOnFailureListener {
            binding.ivProfileImage.setImageResource(R.drawable.profile_img)
        }
    }

    private fun showConfirmationDialog(newStatus: String) {
        pendingStatusUpdate = newStatus
        val dialog = Dialog(this).apply {
            setContentView(R.layout.dialog_status_update)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
            )

            val title = findViewById<TextView>(R.id.dialog_title)
            val message = findViewById<TextView>(R.id.dialog_message)
            val icon = findViewById<ImageView>(R.id.dialog_icon)
            val okButton = findViewById<Button>(R.id.ok_button)
            val cancelButton = findViewById<Button>(R.id.cancel_button)

            when (newStatus) {
                "Shortlisted" -> {
                    title.text = "Shortlist This Applicant?"
                    message.text = "Are you sure you want to shortlist this applicant?"
                }
                "Interview" -> {
                    title.text = "Schedule Interview?"
                    message.text = "Are you sure you want to schedule an interview with this applicant?"
                }
                "Hired" -> {
                    title.text = "Hire This Applicant?"
                    message.text = "Are you sure you want to hire this applicant?"
                }
                "Rejected" -> {
                    title.text = "Reject This Applicant?"
                    message.text = "Are you sure you want to reject this applicant?"
                    title.setTextColor(resources.getColor(R.color.red600))
                    icon.setImageResource(R.drawable.ic_reject)
                    okButton.setBackgroundColor(resources.getColor(R.color.red600))
                }
            }

            okButton.setOnClickListener {
                pendingStatusUpdate?.let { status ->
                    updateApplicationStatus(status)
                }
                dismiss()
            }

            cancelButton.setOnClickListener {
                pendingStatusUpdate = null
                dismiss()
            }

            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }
        dialog.show()
    }

    private fun updateApplicationStatus(newStatus: String) {
        targetJobId?.let { jobId ->
            val database = FirebaseDatabase.getInstance()
            val appliedJobsRef = database.getReference("AppliedJobs")
//Toast.makeText(this, userId ,Toast.LENGTH_SHORT).show()
            val processedEmail= userId?.replace("_",".")
            // Query for records matching both job_id and user_email
            appliedJobsRef.orderByChild("job_id").equalTo(jobId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (jobSnapshot in dataSnapshot.children) {
                            // Check if this record also belongs to the current user
                            if (jobSnapshot.child("user_email").value == processedEmail) {
                                jobSnapshot.ref.child("job_status")
                                    .setValue("${jobSnapshot.child("job_status").value} , $newStatus")
                                    .addOnSuccessListener {
                                        Log.d("Firebase", "Job status updated successfully")
                                        runOnUiThread {
//                                            showStatusUpdatedDialog(newStatus)
                                            updateButtonStatus(newStatus)
                                            if (newStatus == "Interview") {
                                                checkBookedSlotAndCompareTime()
                                            }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Firebase", "Error updating job status", e)
                                        runOnUiThread {
                                            Toast.makeText(
                                                this@ApplicantsViewProfileActivity,
                                                "Failed to update status",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                // Break after finding the matching record since there should be only one
                                return
                            }
                        }
                        // If we get here, no matching record was found
                        runOnUiThread {
                            Toast.makeText(
                                this@ApplicantsViewProfileActivity,
                                "No application found for this user and job",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("Firebase", "Query cancelled", databaseError.toException())
                        runOnUiThread {
                            Toast.makeText(
                                this@ApplicantsViewProfileActivity,
                                "Database error",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
        } ?: run {
            runOnUiThread {
                Toast.makeText(
                    this@ApplicantsViewProfileActivity,
                    "No job selected",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun checkBookedSlotAndCompareTime() {
        val jobId = targetJobId ?: return
        val processedEmail = userId?.replace("_", ".") ?: return

        val bookedSlotsRef = FirebaseDatabase.getInstance().getReference("BookedSlots")
        val query = bookedSlotsRef.orderByChild("job_id").equalTo(jobId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    handleNoBookedSlot()
                    return
                }

                var bookedSlotFound = false
                for (slotSnapshot in snapshot.children) {
                    val slotEmail = slotSnapshot.child("applicant_email").getValue(String::class.java)
                    if (slotEmail == processedEmail) {
                        bookedSlotFound = true
                        val slotDate = slotSnapshot.child("Date").getValue(String::class.java) ?: ""
                        val slotTime = slotSnapshot.child("Time").getValue(String::class.java) ?: ""


                        val bookedDateTime = parseDateTime(slotDate, slotTime)
                        val currentDateTime = Calendar.getInstance()

                        when {
                            bookedDateTime == null -> {
                                handleNoBookedSlot()
                            }
                            currentDateTime.before(bookedDateTime) -> {
                                handleUpcomingInterview()
                            }
                            else -> {
                                handlePassedInterview()
                            }
                        }
                        break
                    }
                }

                if (!bookedSlotFound) {
                    handleNoBookedSlot()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("BookedSlotCheck", "Database error", error.toException())
                // Default to no booked slot state if there's an error
                handleNoBookedSlot()
            }
        })
    }

    private fun handleNoBookedSlot() {
        runOnUiThread {
            binding.btnHier.text = "Schedule Interview"
            binding.btnHier.setOnClickListener {
                val intent = Intent(this, ScheduleInterviewActivity::class.java).apply {
                    putExtra("JOB_ID", targetJobId)
                    putExtra("User_Email", userId)
                }
                startActivity(intent)
            }
        }
    }

    private fun handleUpcomingInterview() {
        runOnUiThread {
            binding.btnHier.text = "See Interview Slots"
            binding.btnHier.setOnClickListener {
                val intent = Intent(this, ScheduleInterviewActivity::class.java).apply {
                    putExtra("JOB_ID", targetJobId)
                    putExtra("User_Email", userId)
                }
                startActivity(intent)
            }
        }
    }

    private fun handlePassedInterview() {
        runOnUiThread {
            binding.btnHier.text = "Hire This Applicant"
            binding.btnHier.setOnClickListener {
                showConfirmationDialog("Hired")
            }
        }
    }

    private fun parseDateTime(dateStr: String, timeStr: String): Calendar? {
        return try {
            val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
            val dateTimeStr = "$dateStr $timeStr"
            val date = dateFormat.parse(dateTimeStr)

            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar
        } catch (e: Exception) {
            Log.e("DateTimeParse", "Error parsing date/time", e)
            null
        }
    }

    private fun noBookedSlotFound() {
        // Handle case where no booked slot was found
        Log.d("SlotCheck", "No booked slot found")
        Toast.makeText(this, "No booked slot found", Toast.LENGTH_SHORT).show()
        // Call your function for no booked slot case
    }




    private fun updateButtonStatus(newStatus: String) {
        when (newStatus) {
            "Shortlisted" -> {
                binding.btnHier.text = "Schedule Interview"
                binding.btnHier.setOnClickListener {
                    // showConfirmationDialog("Interview")
                    val intent = Intent(this, ScheduleInterviewActivity::class.java)
                    intent.putExtra("JOB_ID", targetJobId)
                    intent.putExtra("User_Email", userId)
                    startActivity(intent)
                }
            }
            "See Interview Slots" -> {
                binding.btnHier.text = "See Interview Slots"
                binding.btnHier.setOnClickListener {
                    // showConfirmationDialog("Interview")
                    val intent = Intent(this, ScheduleInterviewActivity::class.java)
                    intent.putExtra("JOB_ID", targetJobId)
                    intent.putExtra("User_Email", userId)
                    startActivity(intent)
                }
            }
            "Hire" -> {
                binding.btnHier.text = "Hire This Applicant"
                binding.btnHier.setOnClickListener {
                    showConfirmationDialog("Hired")
                }
            }

            "Hired" -> {
                binding.btnHier.visibility = View.GONE
            }
            "Rejected" -> {
                binding.btnHier.visibility = View.GONE
                binding.btnReject.visibility = View.GONE
            }
        }
    }
}
