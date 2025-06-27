package com.example.companyapp.API

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.companyapp.R
import com.example.companyapp.databinding.ActivityJobDetailsBinding
import com.example.companyapp.viewapplicants.ApplicantsViewProfileActivity
import com.example.companyapp.viewapplicants.JobDetailActivity
import com.example.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class JobDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJobDetailsBinding
    private lateinit var jobId: String
//    private lateinit var resultTextView: TextView
    private val database = FirebaseDatabase.getInstance().reference
    private lateinit var geminiTextView: TextView

    // TODO: Replace with your actual Gemini API key
    private val apiKey = "AIzaSyAGHUu_1obej-CrOZ-UPYUDEMQRSHBU-Ls"
    private val modelName = "gemini-2.0-flash"
    private val executorService = Executors.newSingleThreadExecutor()
    private lateinit var generativeModel: GenerativeModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the GenerativeModel
        val config = generationConfig {
            temperature = 0.7f
            topK = 16
            topP = 0.1f
            maxOutputTokens = 2000
        }

        generativeModel = GenerativeModel(
            modelName = modelName,
            apiKey = apiKey,
            generationConfig = config
        )

//        resultTextView = binding.resultTextView
        geminiTextView = binding.geminiResultTextView
        jobId = intent.getStringExtra("JOB_ID") ?: ""
//        Toast.makeText(this, "Job ID: $jobId", Toast.LENGTH_LONG).show()
//        jobId = intent.getStringExtra("User_Email") ?: ""

        if (jobId.isNotEmpty()) {
            fetchJobDetails()
//            Toast.makeText(this , "Job ID in if statement: $jobId", Toast.LENGTH_LONG).show()
        } else {
//            Toast.makeText(this , "Job ID in if statement: $jobId", Toast.LENGTH_LONG).show()

                Toast.makeText(this, "Error: No job ID provided", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchJobDetails() {
        val result = StringBuilder()
        database.child("Jobs").orderByChild("job_id").equalTo(jobId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(jobSnapshot: DataSnapshot) {
                    if (!jobSnapshot.exists()) {
//                        resultTextView.text = "Job not found"
                        Toast.makeText(this@JobDetailsActivity, "Job not found", Toast.LENGTH_SHORT).show()
                        return
                    }

                    val job = jobSnapshot.children.first()
                    val jobDesc = job.child("job_description").value.toString()
                    val jobReqs = job.child("job_requirment").value.toString()

                    result.append("JOB DESCRIPTION:\n$jobDesc\n\n")
                    result.append("JOB REQUIREMENTS:\n$jobReqs\n\n")
                    result.append("----------------------------------------\n\n")
                    result.append("APPLICANTS:\n\n")

                    fetchApplicants(result)
                }

                override fun onCancelled(error: DatabaseError) {
//                    resultTextView.text = "Error fetching job details: ${error.message}"
                }
            })
    }

    private fun fetchApplicants(result: StringBuilder) {
        database.child("AppliedJobs")
            .orderByChild("job_id").equalTo(jobId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(appliedJobsSnapshot: DataSnapshot) {
                    if (!appliedJobsSnapshot.exists()) {
                        result.append("No applicants found for this job")
//                        resultTextView.text = result.toString()
                        processWithGemini(result.toString()) // Process even with no applicants
                        return
                    }

                    val applicants = mutableMapOf<String, MutableList<DataSnapshot>>()
                    for (application in appliedJobsSnapshot.children) {
                        val email = application.child("user_email").value.toString()
                        applicants.getOrPut(email) { mutableListOf() }.add(application)
                    }

                    // Process applicants sequentially
                    processApplicantsSequentially(applicants.keys.toList(), result, 0)
                }

                override fun onCancelled(error: DatabaseError) {
                    result.append("\nError fetching applicants: ${error.message}")
//                    resultTextView.text = result.toString()
                    processWithGemini(result.toString()) // Process even with errors
                }
            })
    }

    private fun processApplicantsSequentially(
        applicantEmails: List<String>,
        result: StringBuilder,
        index: Int
    ) {
        if (index >= applicantEmails.size) {
//            resultTextView.text = result.toString()
            processWithGemini(result.toString()) // Process after fetching all details
            return
        }

        val email = applicantEmails[index]
        result.append("Applicant: $email\n")
        fetchUserAnswers(email, result) {
            // Callback to process the next applicant after this one is done
            processApplicantsSequentially(applicantEmails, result, index + 1)
        }
    }

    private fun fetchUserAnswers(userEmail: String, result: StringBuilder, onComplete: () -> Unit) {
        database.child("Answers")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(answersSnapshot: DataSnapshot) {
                    if (!answersSnapshot.exists()) {
                        result.append("  No answers found\n\n")
                        onComplete()
                        return
                    }

                    val answers = mutableListOf<AnswerData>()
                    for (questionEntry in answersSnapshot.children) {
                        val questionId = questionEntry.key ?: continue
                        for (answerEntry in questionEntry.children) {
                            if (answerEntry.child("answeredBy").value.toString() == userEmail) {
                                answers.add(
                                    AnswerData(
                                        questionId,
                                        answerEntry.child("answer").value.toString(),
                                        answerEntry.child("answerDateTime").value.toString()
                                    )
                                )
                            }
                        }
                    }

                    if (answers.isEmpty()) {
                        result.append("  No answers found\n\n")
                        onComplete()
                        return
                    }

                    fetchQuestionsForAnswers(userEmail, answers, result, onComplete)
                }

                override fun onCancelled(error: DatabaseError) {
                    result.append("  Error fetching answers: ${error.message}\n\n")
                    onComplete()
                }
            })
    }

    private fun fetchQuestionsForAnswers(
        userEmail: String,
        answers: List<AnswerData>,
        result: StringBuilder,
        onComplete: () -> Unit
    ) {
        database.child("UserQuestions")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(questionsSnapshot: DataSnapshot) {
                    val questionsMap = mutableMapOf<String, QuestionData>()
                    for (userQuestions in questionsSnapshot.children) {
                        for (questionEntry in userQuestions.children) {
                            val questionId = questionEntry.key ?: continue
                            questionsMap[questionId] = QuestionData(
                                questionEntry.child("question").value.toString(),
                                questionEntry.child("emailId").value.toString(),
                                questionEntry.child("title").value.toString()
                            )
                        }
                    }

                    for (answer in answers) {
                        val question = questionsMap[answer.questionId]
                        if (question != null) {
                            result.append("  Question (from ${question.askerEmail}):\n")
                            result.append("  ${question.questionText}\n")
                            result.append("  Answer (by $userEmail on ${answer.answerDate}):\n")
                            result.append("  ${answer.answerText}\n\n")
                        } else {
                            result.append("  (Question not found for answer)\n\n")
                        }
                    }

                    onComplete()
                }

                override fun onCancelled(error: DatabaseError) {
                    result.append("  Error finding questions: ${error.message}\n\n")
                    onComplete()
                }
            })
    }

    private fun processWithGemini(jobDetails: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Set loading state
                runOnUiThread {
                    binding.progBar.visibility = View.VISIBLE
                    binding.matchedUserCard.visibility = View.GONE
                    geminiTextView.visibility = View.GONE
                    binding.animNoMatchFound.visibility = View.GONE
                }

                // Extract job description and requirements
                val jobDesc = jobDetails.substringAfter("JOB DESCRIPTION:\n").substringBefore("\n\nJOB REQUIREMENTS:")
                val jobReqs = jobDetails.substringAfter("JOB REQUIREMENTS:\n").substringBefore("\n\n----------------------------------------")

                // Build the prompt
                val prompt = """
                Carefully analyze the following components:
                1. JOB DESCRIPTION:
                $jobDesc
                
                2. JOB REQUIREMENTS:
                $jobReqs
                
                3. APPLICANTS' ANSWERS TO QUESTIONS:
                ${jobDetails.substringAfter("APPLICANTS:\n\n")}
                
                Evaluation Criteria:
                - For each required skill in the job requirements, examine if the applicant demonstrates knowledge through their answers
                - Check for technical accuracy in domain-specific responses
                - Verify depth of understanding in key areas mentioned in the job description
                - Assess problem-solving approach in their answers
                - Identify any direct experience mentioned that matches requirements
                
                Output Format Requirements:
                1. Only output one of these two options:
                   - "none" (if no suitable candidate found)
                   - "user_email" (the email of the single most qualified applicant)
                
                Selection Rules:
                - Choose the applicant whose answers most comprehensively cover the required skills
                - Prefer candidates who demonstrate practical application of required skills
                - The candidate must show competence in at least 60% of core requirements
                - If multiple candidates meet the threshold, select the one with the most complete coverage
                
                Important:
                - DO NOT include any explanations
                - DO NOT list multiple candidates
                - ONLY respond with either "none" or a single email address
            """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val text = response.text?.trim() ?: "none"  // Default to "none" if no response

                runOnUiThread {
                    // Hide loading indicator
                    binding.progBar.visibility = View.GONE

                    when {
                        text == "none" -> {
                            // No suitable candidate found
                            geminiTextView.text = "No suitable candidate found"
//                            geminiTextView.visibility = View.VISIBLE
                            binding.animNoMatchFound.visibility = View.VISIBLE

                            binding.matchedUserCard.visibility = View.GONE
                        }
                        text.contains("@") -> {
                            // Valid email found - show user card
//                            geminiTextView.visibility = View.GONE
                            binding.animNoMatchFound.visibility = View.GONE

                            binding.matchedUserCard.visibility = View.VISIBLE
                            displayCandidateDetails(text)
                        }
                        else -> {
                            // Unexpected response
                            geminiTextView.text = "Invalid response from analysis"
//                            geminiTextView.visibility = View.VISIBLE
                            binding.animNoMatchFound.visibility = View.VISIBLE

                            binding.matchedUserCard.visibility = View.GONE
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Gemini API error", e)
                runOnUiThread {
                    binding.progBar.visibility = View.GONE
                    geminiTextView.text = "Error generating analysis: ${e.localizedMessage}"
//                    geminiTextView.visibility = View.GONE
                    binding.animNoMatchFound.visibility = View.VISIBLE
                    binding.matchedUserCard.visibility = View.GONE
                }
            }
        }
    }
    private fun displayCandidateDetails(email: String) {
        // Clear previous data
        binding.profileImage.setImageResource(R.drawable.profile_img)
        binding.name.text = "Loading..."
        binding.tvEmail.text = email
//        binding.phone.text = ""
//        binding.address.text = ""
        binding.dateApplied.text = ""

        binding.matchedUserCard.setOnClickListener(){
            val intent = Intent(this, ApplicantsViewProfileActivity::class.java)
            intent.putExtra("JOB_ID", jobId)
            intent.putExtra("USER_ID" ,email)
            Toast.makeText(this, "Job ID: $jobId sent sucessfully ", Toast.LENGTH_LONG).show()
            startActivity(intent)
        }


        // Convert email to database key format (if needed)
        val dbKeyEmail = email.replace(".", "_") // Firebase doesn't allow . in keys

        // 1. Load profile image from Storage
        val storageRef = Firebase.storage.reference
        val profileImageRef = storageRef.child("User/${email.replace(".", "_")}/profile.jpg")

        profileImageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .circleCrop()
                .into(binding.profileImage)
        }.addOnFailureListener {
            binding.profileImage.setImageResource(R.drawable.profile)
        }

        // 2. Fetch user details - DIRECT ACCESS using email as key
        database.child("User").child(dbKeyEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        showUserNotFound(email)
                        return
                    }

                    try {
                        // Directly access fields since we're not using User model
                        binding.name.text = snapshot.child("profileName").value?.toString() ?: "No name"


//                        binding.phone.text = snapshot.child("profilePhone").value?.toString() ?: "No phone"
//                        binding.address.text = snapshot.child("profileText").value?.toString() ?: "No address"

                        // Fetch application date
                        fetchApplicationDate(email)
                    } catch (e: Exception) {
                        showError("Error loading user data")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showError("Database error: ${error.message}")
                }
            })
    }

    private fun fetchApplicationDate(email: String) {
        val formattedEmail = email.replace("_", ".") // Convert storage format back to email
        database.child("AppliedJobs")
            .orderByChild("user_email")
            .equalTo(formattedEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        binding.dateApplied.text = "Application date not found"
                        return
                    }

                    try {
                        val dateString = snapshot.children.first()
                            .child("job_date").value?.toString() ?: run {
                            binding.dateApplied.text = "Date not available"
                            return
                        }

                        // Parse the stored date
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val applicationDate = dateFormat.parse(dateString)

                        // Calculate time difference
                        val currentDate = Calendar.getInstance().time
                        val diffInMillis = currentDate.time - applicationDate.time
                        val days = TimeUnit.MILLISECONDS.toDays(diffInMillis)


                        // Format the output
                        val dateText = when {
                            days == 0L -> "Applied today"
                            days == 1L -> "Applied yesterday"
                            days < 7L -> "Applied $days days ago"
                            days < 30L -> "Applied ${days/7} weeks ago"
                            else -> "Applied on ${SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(applicationDate)}"
                        }

                        binding.dateApplied.text = dateText
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing date", e)
                        binding.dateApplied.text = "Date format error"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.dateApplied.text = "Date unavailable"
                }
            })
    }
    private fun showUserNotFound(email: String) {
        with(binding) {
            name.text = "User not found"
//            phone.text = ""
//            address.text = ""
            profileImage.setImageResource(R.drawable.profile)
        }
        Toast.makeText(this, "User $email not found", Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        binding.profileImage.setImageResource(R.drawable.profile)
    }

    private data class AnswerData(
        val questionId: String,
        val answerText: String,
        val answerDate: String
    )

    private data class QuestionData(
        val questionText: String,
        val askerEmail: String,
        val title: String
    )
}