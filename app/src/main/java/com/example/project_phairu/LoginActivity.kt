package com.example.project_phairu

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.project_phairu.DataStore.UserSessionDataStore
import com.example.project_phairu.Model.UserModel
import com.example.project_phairu.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {


    //binding
    private lateinit var binding: ActivityLoginBinding

    //Firebase Declaration and Reference
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    //Datastore
    private lateinit var userSessionDataStore: UserSessionDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //binding
        binding = ActivityLoginBinding.inflate(layoutInflater)

        //firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.getReference("Users")

        //DataStore
        userSessionDataStore = UserSessionDataStore(this)

        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find the "Login" button
        binding.loginButton.setOnClickListener {
            // Get the email/username and password entered by the user
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()

            // Check internet connection first
            if (isNetworkConnected()) {
                if (email.isEmpty() || password.isEmpty()) {
                    // Handle empty fields
                    if (email.isEmpty()) {
                        Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                    if (password.isEmpty()) {
                        Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                } else if (email.isValidEmail() && password.isValidPassword()) {
                    loginUser(email, password)
                } else {
                    // Show specific error messages for invalid input (non-empty)
                    if (!email.isValidEmail()) {
                        Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                    }
                    if (!password.isValidPassword()) { // Check for invalid password
                        Toast.makeText(this, "Invalid password format", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            }
        }

        //signup Redirect
        binding.signUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish() // Close the current activity
        }

        //forgot password
        binding.forgotpsswd.setOnClickListener {
            val buider = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_forgot, null)


            buider.setView(view)
            val dialog = buider.create()

            view.findViewById<Button>(R.id.btnReset).setOnClickListener {
                val userEmail = view.findViewById<EditText>(R.id.resetEmail).text.toString().trim()

                if (userEmail.isEmpty()) {
                    Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                } else if (!userEmail.isValidEmail()) {
                    Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                } else {
                    resetEmail(userEmail)
                    dialog.dismiss()
                }
            }

            view.findViewById<ImageView>(R.id.btnCancel).setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

    }

    //signup user function

    private fun loginUser(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful){

                // Get the current user
                val user = firebaseAuth.currentUser

                // Extract the user ID
                val userId = user?.uid

                if (userId != null) {
                    lifecycleScope.launch {
                        userSessionDataStore.storeUserSession(true, userId)
                    }
                }

                Toast.makeText(this@LoginActivity, "Login Success", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish() // Close the current activity
            } else {
                val exception = task.exception
                if (exception is FirebaseAuthInvalidUserException) {
                    Toast.makeText(this@LoginActivity, "Invalid Email", Toast.LENGTH_SHORT).show()
                } else if (exception is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(this@LoginActivity, "Invalid Password", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@LoginActivity, "Login Failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //Email Reset Function

    private fun resetEmail(email: String) {
            firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                if (task.isSuccessful){
                    Toast.makeText(this@LoginActivity, "Reset Email Sent, Check your email", Toast.LENGTH_SHORT).show()
                }

             }

    }

    // Helper function for email validation
    private fun String.isValidEmail(): Boolean {
        return !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

    // Helper function for password validation
    private fun String.isValidPassword(): Boolean {
        return length >= 8 // Example: Password must be at least 8 characters long
    }

    //Check Internet Connection
    private fun isNetworkConnected(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
