package com.example.project_phairu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Find the "Sign Up" TextView
        val signUpTextview = findViewById<TextView>(R.id.sign_up)

        // Set a click listener on the "Sign Up" TextView
        signUpTextview.setOnClickListener {
            // Start the SignUpActivity
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // Find the "Login" Button
        val loginButton = findViewById<Button>(R.id.login_button)

        // Set a click listener on the "Login" Button
        loginButton.setOnClickListener {
            // Start the SignUpActivity
            startActivity(Intent(this, MainActivity::class.java))
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}