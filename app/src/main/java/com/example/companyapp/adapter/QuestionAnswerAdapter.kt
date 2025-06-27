package com.example.companyapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.companyapp.R
import com.example.companyapp.models.QuestionAnswerPair
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class QuestionAnswerAdapter(private val pairs: List<QuestionAnswerPair>) :
    RecyclerView.Adapter<QuestionAnswerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val questionText: TextView = view.findViewById(R.id.question_text)
        val answerText: TextView = view.findViewById(R.id.answer_text)
        val categoryText: TextView = view.findViewById(R.id.category_text)
        val dateText: TextView = view.findViewById(R.id.date_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_question_answer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pair = pairs[position]
        holder.questionText.text = pair.question.question
        holder.answerText.text = pair.answer.answer
        holder.categoryText.text = pair.question.category

        // Format the date to show relative time
        holder.dateText.text = getRelativeTime(pair.answer.answerDateTime)
    }

    override fun getItemCount() = pairs.size

    private fun getRelativeTime(dateTimeString: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val pastDate = dateFormat.parse(dateTimeString) ?: return dateTimeString

        val now = Calendar.getInstance().time
        val diffInMillis = now.time - pastDate.time

        val seconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
        val days = TimeUnit.MILLISECONDS.toDays(diffInMillis)
        val weeks = days / 7
        val months = days / 30
        val years = days / 365

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "$minutes minute${if(minutes != 1L) "s" else ""} ago"
            hours < 24 -> "$hours hour${if(hours != 1L) "s" else ""} ago"
            days < 7 -> "$days day${if(days != 1L) "s" else ""} ago"
            weeks < 4 -> "$weeks week${if(weeks != 1L) "s" else ""} ago"
            months < 12 -> "$months month${if(months != 1L) "s" else ""} ago"
            else -> "$years year${if(years != 1L) "s" else ""} ago"
        }
    }
}