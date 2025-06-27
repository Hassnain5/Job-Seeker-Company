package com.example.companyapp.viewapplicants

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.companyapp.adapter.QuestionAnswerAdapter
import com.example.companyapp.databinding.ActivityMyAnswersBinding
import com.example.companyapp.models.Answer
import com.example.companyapp.models.FindQuestion
import com.example.companyapp.models.QuestionAnswerPair
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MyAnswersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyAnswersBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var adapter: QuestionAnswerAdapter
    private val questionAnswerPairs = mutableListOf<QuestionAnswerPair>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityMyAnswersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = QuestionAnswerAdapter(questionAnswerPairs)
        binding.recyclerView.adapter = adapter

        // Set click listener for back button
        binding.backButton.setOnClickListener {
            finish()
        }

        // Show loading state initially
        showLoadingState()

        // Get email from intent and load answers
        val userEmail = intent.getStringExtra("USER_EMAIL")
        loadUserAnswers(userEmail?.replace("_", "."))
    }

    private fun loadUserAnswers(userEmail: String?) {
        if (userEmail.isNullOrEmpty()) {
            showEmptyState("No user email provided")
            return
        }

        database.reference.child("Answers").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val answers = mutableListOf<Answer>()
                val questionIds = mutableSetOf<String>()

                for (questionSnapshot in dataSnapshot.children) {
                    val questionId = questionSnapshot.key ?: continue

                    for (answerSnapshot in questionSnapshot.children) {
                        val answer = answerSnapshot.getValue(Answer::class.java)?.copy(
                            answerId = answerSnapshot.key ?: "",
                            questionId = questionId
                        )

                        answer?.let {
                            if (it.answeredBy == userEmail) {
                                answers.add(it)
                                questionIds.add(questionId)
                            }
                        }
                    }
                }

                if (answers.isEmpty()) {
                    showEmptyState("You haven't answered any questions yet")
                } else {
                    fetchQuestionsForAnswers(answers, questionIds.toList())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                showErrorState("Failed to load answers: ${databaseError.message}")
            }
        })
    }

    private fun fetchQuestionsForAnswers(answers: List<Answer>, questionIds: List<String>) {
        database.reference.child("UserQuestions").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                questionAnswerPairs.clear()
                val questionMap = mutableMapOf<String, FindQuestion>()

                for (userSnapshot in dataSnapshot.children) {
                    for (questionSnapshot in userSnapshot.children) {
                        val questionId = questionSnapshot.key ?: continue
                        if (questionId in questionIds) {
                            questionSnapshot.getValue(FindQuestion::class.java)?.let { question ->
                                questionMap[questionId] = question.copy(questionId = questionId)
                            }
                        }
                    }
                }

                for (answer in answers) {
                    questionMap[answer.questionId]?.let { question ->
                        questionAnswerPairs.add(QuestionAnswerPair(question, answer))
                    }
                }

                questionAnswerPairs.sortByDescending { it.answer.answerDateTime }

                if (questionAnswerPairs.isEmpty()) {
                    showEmptyState("No questions found for your answers")
                } else {
                    showContentState()
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                showErrorState("Failed to load questions: ${databaseError.message}")
            }
        })
    }

    private fun showLoadingState() {
        binding.progBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.emptyState.visibility = View.GONE
    }

    private fun showContentState() {
        binding.progBar.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        binding.emptyState.visibility = View.GONE
    }

    private fun showEmptyState(message: String? = null) {
        binding.progBar.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.emptyState.visibility = View.VISIBLE

        message?.let {
            // You can update the empty state text views here if needed
            // For example:
            // binding.emptyState.textView.text = it
        }
    }

    private fun showErrorState(errorMessage: String) {
        showEmptyState(errorMessage)
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }
}