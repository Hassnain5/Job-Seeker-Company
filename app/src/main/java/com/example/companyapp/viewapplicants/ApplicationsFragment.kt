package com.example.companyapp.viewapplicants

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.companyapp.adapter.ApplicationsAdapter
import com.example.companyapp.databinding.FragmentApplicationsBinding
import com.example.companyapp.models.Applicant
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ApplicationsFragment : Fragment() {
    private var _binding: FragmentApplicationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ApplicationsAdapter
    private val database = FirebaseDatabase.getInstance().reference
    private val storage = FirebaseStorage.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApplicationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ApplicationsAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ApplicationsFragment.adapter
        }

        val jobId = arguments?.getString("JOB_ID") ?: ""
        loadApplicants(jobId)
    }

    private fun loadApplicants(jobId: String) {
        database.child("AppliedJobs")
            .orderByChild("job_id")
            .equalTo(jobId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val applicants = mutableListOf<Applicant>()
                    for (childSnapshot in snapshot.children) {
                        val status = childSnapshot.child("job_status").getValue(String::class.java)
                        if (status == "Submitted") {
                            val email = childSnapshot.child("user_email").getValue(String::class.java) ?: ""
                            val userId = email.substringBefore("@").replace(".", "") // Generate user ID from email

                            // Fetch user details from Users table
                            fetchUserDetails(userId, email) { user ->
                                applicants.add(user)
                                adapter.submitList(applicants.toList())
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    private fun fetchUserDetails(userId: String, email: String, callback: (Applicant) -> Unit) {
        // 1. Fetch user info from Users table

        database.child("User").child(userId).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(userSnapshot: DataSnapshot) {
                    val name = userSnapshot.child("profileName").getValue(String::class.java) ?: "No Name"
                    val profileImagePath = userSnapshot.child("profileImage").getValue(String::class.java)

                    // 2. Get profile image URL from Storage
                    if (profileImagePath != null) {
                        storage.child(profileImagePath).downloadUrl.addOnSuccessListener { uri ->
                            val applicant = Applicant(
                                userId = userId,
                                name = name,
                                email = email,
                                info = "", // Will be filled from AppliedJobs
                                cvUrl = "", // Will be filled from AppliedJobs
                                portfolioUrl = "", // Will be filled from AppliedJobs
                                jobName = "", // Will be filled from AppliedJobs
                                jobDate = "", // Will be filled from AppliedJobs
                                profileImageUrl = uri.toString()
                            )
                            callback(applicant)
                        }.addOnFailureListener {
                            createApplicantWithoutProfile(userId, name, email, callback)
                        }
                    } else {
                        createApplicantWithoutProfile(userId, name, email, callback)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    createApplicantWithoutProfile(userId, "Unknown", email, callback)
                }
            }
        )
    }

    private fun createApplicantWithoutProfile(
        userId: String,
        name: String,
        email: String,
        callback: (Applicant) -> Unit
    ) {
        val applicant = Applicant(
            userId = userId,
            name = name,
            email = email,
            info = "", // Will be filled from AppliedJobs
            cvUrl = "", // Will be filled from AppliedJobs
            portfolioUrl = "", // Will be filled from AppliedJobs
            jobName = "", // Will be filled from AppliedJobs
            jobDate = "", // Will be filled from AppliedJobs
            profileImageUrl = null
        )
        callback(applicant)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(jobId: String) = ApplicationsFragment().apply {
            arguments = Bundle().apply {
                putString("JOB_ID", jobId)
            }
        }
    }
}