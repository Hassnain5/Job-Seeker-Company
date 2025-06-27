package com.example.companyapp.viewapplicants

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.companyapp.R
import com.example.companyapp.databinding.FragmentApplicantsCVBinding
import com.example.companyapp.models.Applicant
import com.example.companyapp.models.Application
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ApplicantsCVFragment : Fragment() {
    private var _binding: FragmentApplicantsCVBinding? = null
    private val binding get() = _binding!!
    private val database = FirebaseDatabase.getInstance().reference
    private lateinit var currentUserEmail: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApplicantsCVBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = arguments?.getString("USER_ID") ?: ""
//        val userEmail = arguments?.getString("USER_EMAIL") ?: ""
        val jobId = arguments?.getString("JOB_ID") ?: ""
        currentUserEmail = userId.replace("_", ",")

//        Toast.makeText(requireContext(), "User ID: $userId, Email: $currentUserEmail", Toast.LENGTH_SHORT).show()
        if (currentUserEmail.isEmpty()) {
            showError("User email not provided")
            return
        }

        // Fetch applicant data including CV and Portfolio URLs
//        fetchApplicantData(currentUserEmail)

        binding.viewCv.setOnClickListener {
            openCV(currentUserEmail.replace(",", "."))
        }

        binding.viewPortfolio.setOnClickListener {
            openPortfolio(jobId)
        }
        binding.viewQuestionAnswers.setOnClickListener {
            val intent = Intent(requireContext(), MyAnswersActivity::class.java)
            intent.putExtra("USER_EMAIL", userId) // Replace with actual email
            startActivity(intent)
        }

    }

    private fun fetchApplicantData(userId: String) {

        database.child("Application").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val application = snapshot.getValue(Application::class.java)

                        if (application != null) {
                            // Update UI based on CV availability
                            if (application.user_cv_url.isNotEmpty()) {
                                binding.cvImage.setImageResource(R.drawable.cv_placeholder)
                                binding.viewCv.isEnabled = true
                                binding.viewCv.text = getString(R.string.view_cv)
                            } else {
                                binding.cvImage.setImageResource(R.drawable.cv_placeholder)
                                binding.viewCv.isEnabled = false
                                binding.viewCv.text = "CV not available"
                            }

                            // Update UI based on Portfolio availability

                        } else {
                            showError("Applicant data not valid")
                        }
                    } else {
                        showError("Applicant data not found")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showError("Failed to load applicant data")
                }
            })
    }

    private fun openCV(userId: String) {
//        Toast.makeText(requireContext(), "User ID: $userId", Toast.LENGTH_SHORT).show()

        database.child("Application")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var cvFound = false

                    for (emailSnapshot in snapshot.children) {
                        for (applicationSnapshot in emailSnapshot.children) {
                            val application = applicationSnapshot.getValue(Application::class.java)
                            if (application?.user_email == userId) {
                                cvFound = true
                                val url = application.user_cv_url
                                if (!url.isNullOrEmpty()) {
//                                    Toast.makeText(requireContext(), "URL: $url", Toast.LENGTH_SHORT).show()
                                    openPdfViewer(url)
                                } else {
                                    showError("CV not available")
                                }
                                break
                            }
                        }
                        if (cvFound) break
                    }

                    if (!cvFound) {
                        showError("Applicant data not found")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showError("Failed to load CV")
                }
            })
    }


    private fun openPortfolio(jobId: String) {
        Toast.makeText(requireContext(), "Job ID: $jobId", Toast.LENGTH_SHORT).show()
        database.child("AppliedJobs")
            .orderByChild("job_id")
            .equalTo(jobId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        Toast.makeText(requireContext(), "No job found with this ID", Toast.LENGTH_SHORT).show()
                        return
                    }

                    for (childSnapshot in snapshot.children) {
                        val url = childSnapshot.child("portfolio_url").getValue(String::class.java)
//                        Toast.makeText(requireContext(), "URL: $url", Toast.LENGTH_SHORT).show()

                        if (!url.isNullOrEmpty()) {
                            openWebBrowser(url)
                        } else {
                            showError("Portfolio not available")
                        }
                        return // Assuming you only need the first match
                    }

                    // If we get here, no child was found with portfolio_url
                    Toast.makeText(requireContext(), "Portfolio URL not found", Toast.LENGTH_SHORT).show()
                }

                override fun onCancelled(error: DatabaseError) {
                    showError("Failed to load portfolio: ${error.message}")
                }
            })
    }
    private fun openPdfViewer(pdfUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(pdfUrl), "application/pdf")
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
        } catch (e: Exception) {
            showError("No PDF viewer installed. Please install a PDF viewer app.")
        }
    }

    private fun openWebBrowser(url: String) {
        try {
            // Ensure the URL has a proper scheme (http:// or https://)
            val webUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "http://$url"
            } else {
                url
            }

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
            startActivity(intent)
        } catch (e: Exception) {
            showError("No browser installed or invalid URL")
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}