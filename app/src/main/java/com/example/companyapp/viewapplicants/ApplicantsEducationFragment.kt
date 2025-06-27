package com.example.companyapp.viewapplicants

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.companyapp.adapter.EducationAdapter
import com.example.companyapp.databinding.FragmentApplicantsEducationBinding
import com.example.companyapp.models.Education
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ApplicantsEducationFragment : Fragment() {

    private var _binding: FragmentApplicantsEducationBinding? = null
    private val binding get() = _binding!!
    private lateinit var educationAdapter: EducationAdapter
    private val database = FirebaseDatabase.getInstance()
    private val educationRef = database.getReference("Education")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApplicantsEducationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        educationAdapter = EducationAdapter(emptyList())
        binding.recView.layoutManager = LinearLayoutManager(requireContext())
        binding.recView.adapter = educationAdapter

        // Get user email from arguments
        val userEmail = arguments?.getString("USER_ID") ?: ""

        if (userEmail.isNotEmpty()) {
           val processedEmail=userEmail.replace("_",".")

            loadUserEducation(processedEmail)
        } else {
            Toast.makeText(requireContext(), "User email not provided", Toast.LENGTH_SHORT).show()
        }
//
//        // Setup button click listeners
//        binding.shortListThisApplicant.setOnClickListener {
//            Toast.makeText(requireContext(), "Shortlisted", Toast.LENGTH_SHORT).show()
//        }
//
//        binding.reject.setOnClickListener {
//            Toast.makeText(requireContext(), "Rejected", Toast.LENGTH_SHORT).show()
//        }
    }

    private fun loadUserEducation(userEmail: String) {
        educationRef.orderByChild("user_email").equalTo(userEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val educations = mutableListOf<Education>()
                    for (educationSnapshot in snapshot.children) {
                        val education = educationSnapshot.getValue(Education::class.java)
                        education?.let { educations.add(it) }
                    }
//                    println(educations)
//                    Toast.makeText(requireContext(), "Education: $educations", Toast.LENGTH_SHORT).show()
                    educationAdapter.updateList(educations)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}