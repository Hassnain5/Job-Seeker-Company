package com.example.companyapp.company.companyfragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.companyapp.company.PostNewJobNextActivity
import com.example.companyapp.databinding.FragmentPostNewJobBinding

class PostNewJobFragment : Fragment() {

    private var _binding: FragmentPostNewJobBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPostNewJobBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nextBtn.setOnClickListener {
            val title = binding.jobTitle.text.toString().trim()
            val descrip = binding.jobDescription.text.toString().trim()
            val requir = binding.jobRequirements.text.toString().trim()

            // Validation: Check if any field is empty
            if (title.isEmpty()) {
                binding.jobTitle.error = "Job title is required"
                binding.jobTitle.requestFocus()
                return@setOnClickListener
            }

            if (descrip.isEmpty()) {
                binding.jobDescription.error = "Job description is required"
                binding.jobDescription.requestFocus()
                return@setOnClickListener
            }

            if (requir.isEmpty()) {
                binding.jobRequirements.error = "Job requirements are required"
                binding.jobRequirements.requestFocus()
                return@setOnClickListener
            }

            // Proceed if all fields are filled
            val intent = Intent(requireContext(), PostNewJobNextActivity::class.java)
            intent.putExtra("title", title)
            intent.putExtra("descrip", descrip)
            intent.putExtra("requir", requir)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
