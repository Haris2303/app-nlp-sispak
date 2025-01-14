package com.example.sispaknlp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Delay splash selama 3 detik
        Handler(Looper.getMainLooper()).postDelayed({
            // pindah ke chatbot activity
            startActivity(Intent(this, ChatbotActivity::class.java))
            // Tutup splash
            finish()
        }, 1500)
    }
}