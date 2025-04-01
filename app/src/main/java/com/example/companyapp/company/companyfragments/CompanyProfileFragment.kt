package com.example.companyapp.company.companyfragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.companyapp.R
import com.example.companyapp.company.CompanyPersonalDetailsActivity
import com.example.companyapp.company.CompanySignInActivity
import com.example.companyapp.databinding.FragmentCompanyProfileBinding
import com.example.joby.models.CompanyProfile
import com.example.joby.utils.FirebaseHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView

class CompanyProfileFragment : Fragment() {
    private var _binding: FragmentCompanyProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using View Binding
        _binding = FragmentCompanyProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch the company profile when the fragment's view is created
        fetchUserProfile()

        // Edit profile button click listener
        binding.editProfile.setOnClickListener {
            startActivity(Intent(requireContext(), CompanyPersonalDetailsActivity::class.java))
        }

        // Setup click listeners for logout
        setupClickListeners()
    }

    private fun fetchUserProfile() {
        val currentUserEmail = FirebaseHelper.getAuth().currentUser?.email

        if (currentUserEmail != null) {
            FirebaseHelper.companyprofileRef.orderByChild("company_email").equalTo(currentUserEmail)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (child in snapshot.children) {
                                val profile = child.getValue(CompanyProfile::class.java)
                                profile?.let {
                                    // Set profile details
                                    binding.profileName.text = "Hi, ${it.company_name}"
                                    binding.profileMail.text = it.company_email
                                    loadImage(it.company_profile_pic, binding.profile)
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        binding.profileName.text = "Cannot fetch"
                    }
                })
        } else {
            binding.profileName.text = "Cannot fetch"
        }
    }

    private fun loadImage(imageUrl: String?, imageView: CircleImageView?) {
        if (imageView == null || imageUrl.isNullOrEmpty()) {
            imageView?.setImageResource(R.drawable.profile_img)
            return
        }

        Glide.with(requireContext()) // Use context from the fragment
            .load(imageUrl)
            .into(imageView)
    }

    private fun setupClickListeners() {
        binding.logout.setOnClickListener { showLogoutDialog() }
    }

    private fun showLogoutDialog() {
        // Inflate the bottom sheet layout
        val view: View = layoutInflater.inflate(R.layout.bottom_sheet_profile, null)

        // Create the BottomSheetDialog
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(view)

        // Log dialog inflating
        Log.d("CompanyProfileFragment", "Inflating BottomSheetDialog")

        // Set click listeners for logout actions
        view.findViewById<View>(R.id.yes_log_out).setOnClickListener {
            Log.d("CompanyProfileFragment", "Logging out")
            logout()
            dialog.dismiss()
        }
        view.findViewById<View>(R.id.no_stay_logged_in).setOnClickListener {
            Log.d("CompanyProfileFragment", "User chose to stay logged in")
            dialog.dismiss()
        }

        // Show the dialog
        dialog.show()
    }

    private fun logout() {
        // Clear shared preferences and sign out from Firebase
        requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE).edit().clear().apply()
        FirebaseAuth.getInstance().signOut()

        // Redirect to Sign In activity
        val intent = Intent(requireContext(), CompanySignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clean up binding
    }
}
