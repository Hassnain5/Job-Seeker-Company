package com.example.companyapp.viewapplicants

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.companyapp.adapter.ShortlistedApplicantsAdapter
import com.example.companyapp.databinding.FragmentShortlistedBinding
import com.example.companyapp.models.Applicant
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ShortlistedFragment : Fragment() {
    private var _binding: FragmentShortlistedBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ShortlistedApplicantsAdapter
    private val database = FirebaseDatabase.getInstance().reference
    private val storage = FirebaseStorage.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShortlistedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ShortlistedApplicantsAdapter(context ?: return)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ShortlistedFragment.adapter
        }

        val jobId = arguments?.getString("JOB_ID") ?: ""
        Toast.makeText(requireContext(), "$jobId", Toast.LENGTH_SHORT).show()
        loadShortlistedApplicants(jobId)
    }

    private fun loadShortlistedApplicants(jobId: String) {
        database.child("AppliedJobs")
            .orderByChild("job_id")
            .equalTo(jobId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val applicants = mutableListOf<Applicant>()
                    for (childSnapshot in snapshot.children) {
                        val status = childSnapshot.child("job_status").getValue(String::class.java)
                        val info = childSnapshot.child("info").getValue(String::class.java) ?:"No Description"
                        if (status == "Submitted , Shortlisted") {
                            val email = childSnapshot.child("user_email").getValue(String::class.java) ?: ""
                           Toast.makeText(requireContext(), "$email", Toast.LENGTH_SHORT).show()
                            val userId = email.replace(".", "_")

                            fetchUserDetails(userId, email,jobId,info) { user ->
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

    private fun fetchUserDetails(userId: String, email: String,jobId: String, info: String, callback: (Applicant) -> Unit) {
        database.child("User").child(userId).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(userSnapshot: DataSnapshot) {
                    val name = userSnapshot.child("profileName").getValue(String::class.java) ?: "No Name"

                    Toast.makeText(requireContext(), "$name", Toast.LENGTH_SHORT).show()
                    if (name != null) {
                        storage.child("User/$userId/profile.jpg").downloadUrl.addOnSuccessListener { uri ->
                            callback(createApplicant(jobId,userId, name, email,info, uri.toString()))
                        }.addOnFailureListener {
                            callback(createApplicant(jobId,userId, name, email, info,null))
                        }
                    } else {
                        callback(createApplicant(jobId,userId, name, email, info,null))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(createApplicant(jobId,userId, "Unknown", email, "Error",null))
                }
            }
        )
    }

    private fun createApplicant(
        jobId: String,
        userId: String,
        name: String,
        email: String,
        info: String,
        profileImageUrl: String?
    ): Applicant {
        return Applicant(
            jobId = jobId,
            userId = userId,
            name = name,
            email = email,
            info = info,
            cvUrl = "",
            portfolioUrl = "",
            jobName = "",
            jobDate = "",
            profileImageUrl = profileImageUrl
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(jobId: String) = ShortlistedFragment().apply {
            arguments = Bundle().apply {
                putString("JOB_ID", jobId)
            }
        }
    }
}
