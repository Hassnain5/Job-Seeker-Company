package com.example.companyapp.viewapplicants

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.companyapp.adapter.ExperienceAdapter
import com.example.companyapp.databinding.FragmentApplicantsExperienceBinding
import com.example.companyapp.models.Experience
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ApplicantsExperienceFragment : Fragment() {
    private var _binding: FragmentApplicantsExperienceBinding? = null
    private val binding get() = _binding!!

    private val experienceList = mutableListOf<Experience>()
    private lateinit var adapter: ExperienceAdapter
    private val database = FirebaseDatabase.getInstance()
    private val reference = database.getReference("Experience")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApplicantsExperienceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView
        setupRecyclerView()

        // Get user ID from arguments
        val userId = arguments?.getString("USER_ID") ?: ""
//        Toast.makeText(requireContext(), "Loading experience for user: $userId", Toast.LENGTH_SHORT).show()
        loadUserExperience(userId)
    }

    private fun setupRecyclerView() {
        adapter = ExperienceAdapter(experienceList) { experience ->
            // Handle item click here
            Toast.makeText(
                requireContext(),
                "Clicked: ${experience.user_company_name}",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.recView.layoutManager = LinearLayoutManager(requireContext())
        binding.recView.adapter = adapter
    }

    private fun loadUserExperience(userEmail: String) {

      val processedEmail=  userEmail.replace("_",".")
        Toast.makeText(requireContext(), "Loading experience for user: $processedEmail", Toast.LENGTH_SHORT).show()
        reference.orderByChild("user_email").equalTo(processedEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    experienceList.clear()
                    for (userSnapshot in snapshot.children) {
                        val experience = userSnapshot.getValue(Experience::class.java)
                        experience?.documentId = userSnapshot.key
                        experience?.let { experienceList.add(it) }
                    }

                    adapter.notifyDataSetChanged()

                    if (experienceList.isEmpty()) {
                        Toast.makeText(requireContext(), "No experience found", Toast.LENGTH_SHORT).show()
                    }
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