package com.example.companyapp.company.companyfragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.companyapp.R
import com.example.joby.Adapters.ListingAdapter
import com.example.joby.models.PostJob
import com.example.companyapp.databinding.FragmentYourListingBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class YourListingFragment : Fragment() {

    private var _binding: FragmentYourListingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using View Binding
        _binding = FragmentYourListingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch all job posts from Firebase
        fetchAllJobs()
    }

    private fun fetchAllJobs() {
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val jobPostRef: DatabaseReference = database.getReference("Jobs")

        // Fetch all jobs from the "Jobs" collection
        jobPostRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val jobList = mutableListOf<PostJob>()

                // Iterate through the snapshot and add each job to the list
                for (jobSnapshot in snapshot.children) {
                    val job = jobSnapshot.getValue(PostJob::class.java)
                    job?.let { jobList.add(it) }
                }

                // Pass this jobList to the RecyclerView adapter to update the UI
                // Ensure the binding is not null before accessing it
                _binding?.let {
                    updateRecyclerView(jobList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors here
                Log.e("JobFetchError", "Error fetching jobs: ${error.message}")
            }
        })
    }

    private fun updateRecyclerView(jobList: List<PostJob>) {
        val recyclerView = binding.rvJobListing // Access RecyclerView via View Binding
        val adapter = ListingAdapter(jobList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext()) // Use context for RecyclerView
        recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks by clearing the binding reference
    }
}
