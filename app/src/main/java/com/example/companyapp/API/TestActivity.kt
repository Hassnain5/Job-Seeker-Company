//package com.example.companyapp.API
//import android.os.Bundle
//import android.view.View
//import android.widget.Toast
//import androidx.activity.enableEdgeToEdge
//import androidx.activity.viewModels
//import androidx.appcompat.app.AppCompatActivity
//import androidx.lifecycle.lifecycleScope
//import com.example.companyapp.databinding.ActivityTestBinding
//import kotlinx.coroutines.flow.collect
//
//class TestActivity : AppCompatActivity() {
//    private val viewModel: GeminiViewModel by viewModels {
//        GeminiViewModelFactory("AIzaSyAGHUu_1obej-CrOZ-UPYUDEMQRSHBU-Ls")  // ⚠️ Remove before committing to Git!
//    }
//    private lateinit var binding: ActivityTestBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        binding = ActivityTestBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        binding.sendButton.setOnClickListener {
//            val userInput = binding.userInputEditText.text.toString().trim()
//            if (userInput.isNotEmpty()) {
//                viewModel.sendToGemini(userInput)
//            } else {
//                Toast.makeText(this, "Please enter some text", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        lifecycleScope.launchWhenStarted {
//            viewModel.response.collect { response ->
//                response?.let {
//                    binding.responseTextView.text = it
//                }
//            }
//        }
//
//        lifecycleScope.launchWhenStarted {
//            viewModel.isLoading.collect { isLoading ->
//                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
//            }
//        }
//
//        lifecycleScope.launchWhenStarted {
//            viewModel.error.collect { error ->
//                error?.let {
//                    Toast.makeText(this@TestActivity, it, Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
//}