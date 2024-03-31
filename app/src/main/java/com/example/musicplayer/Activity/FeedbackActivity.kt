package com.example.musicplayer.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.musicplayer.databinding.ActivityFeedbackBinding

class FeedbackActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFeedbackBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentThemeNav[MainActivity.themeIndex])
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Feedback"

        binding.sendFA.setOnClickListener {
            val subject = binding.topicFA.text.toString().trim()
            val msg = binding.feedbackMsgFA.text.toString().trim()

            val noOfStar = binding.ratingBar.numStars
            val getRating = binding.ratingBar.rating

            val feedback = "$msg\nRating App: ${getRating}/${noOfStar}"
            sendEmail(subject, feedback)
        }
    }
    private fun sendEmail(subject: String, msg: String) {
        val mIntent = Intent(Intent.ACTION_SEND)
        mIntent.type = "text/plain"
        mIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("ducht.21it@vku.udn.vn"))
        mIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        mIntent.putExtra(Intent.EXTRA_TEXT, msg)

        try {
            startActivityForResult(Intent.createChooser(mIntent, "Send Email"), 1)
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.topicFA.setText("")
        binding.feedbackMsgFA.setText("")
        binding.ratingBar.rating = 0F
    }
}