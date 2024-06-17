package com.example.project_phairu

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.project_phairu.Model.UserModel
import com.example.project_phairu.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SignUpActivity : AppCompatActivity() {

    //binding
    private lateinit var binding: ActivitySignUpBinding

    //Firebase Declaration and Reference
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //binding
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.getReference("Users")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // SignUp Button and the call function for user sign up

        binding.createAccountButton.setOnClickListener{
            val firstname = binding.Fname.text.toString().uppercase().trim()
            val lastname =  binding.Lname.text.toString().uppercase().trim()
            val username =  binding.Username.text.toString().trim()
            val email =  binding.email.text.toString().trim()
            val password =  binding.password.text.toString().trim()
            val confirmPassword =  binding.confirmPassword.text.toString().trim()

            if (isNetworkConnected()) {
            if (firstname.isNotEmpty() && lastname.isNotEmpty() && username.isNotEmpty() &&
                email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {

                if (password != confirmPassword) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                } else if (!password.isValidPassword()) {
                    // Use a helper function for password validation
                    // This will show the appropriate Toast message based on the failed condition
                } else if (!email.isValidEmail()) {
                    // Use a helper function for email validation
                    Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()
                } else if (password.contains(" ") || email.contains(" ") || username.contains(" ")) {
                    Toast.makeText(this, "Password, email, and username cannot contain spaces", Toast.LENGTH_SHORT).show()
                } else if (email.any { it.isUpperCase() }) {
                    Toast.makeText(this, "Email cannot contain capital letters", Toast.LENGTH_SHORT).show()
                } else {
                    signUpUser(firstname, lastname, username, email, password)
                }

            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
                } else {
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            }



        }

        // Set a click listener on the "Back" TextView
        binding.btnBack.setOnClickListener {
            // Start the LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Close the current activity
        }

    }

    //Fn to sign up a user to Firebase Realtime DB and Firebase Authentication

    private fun signUpUser(firstname: String, lastname: String, username: String, email: String, password: String) {
        databaseReference.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Username already exists
                        Toast.makeText(this@SignUpActivity, "Username already taken", Toast.LENGTH_SHORT).show()
                    } else {
                        // Username is available, proceed with Firebase Authentication
                        firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this@SignUpActivity) { task ->
                                if (task.isSuccessful) {
                                    // Authentication successful, now save to Realtime Database
                                    val user = firebaseAuth.currentUser
                                    val userId = user?.uid ?: return@addOnCompleteListener

                                    val userData = UserModel(userId, firstname, lastname, username, email)
                                    databaseReference.child(userId).setValue(userData)
                                        .addOnSuccessListener {
                                            Toast.makeText(this@SignUpActivity, "Sign Up Success", Toast.LENGTH_SHORT).show()
                                            startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
                                            finish()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(this@SignUpActivity, "Error saving user data: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    // Authentication failed
                                    Toast.makeText(this@SignUpActivity, "Sign Up Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SignUpActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Helper function for email validation
    private fun String.isValidEmail(): Boolean {
        return contains("@") && contains(".")
    }

    // Helper function for password validation
    private fun String.isValidPassword(): Boolean {
        if (!any { it.isUpperCase() }) {
            Toast.makeText(this@SignUpActivity, "Password must contain at least one capital letter", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!any { it.isLowerCase() }) {
            Toast.makeText(this@SignUpActivity, "Password must contain at least one lowercase letter", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!any { it.isDigit() }) {
            Toast.makeText(this@SignUpActivity, "Password must contain at least one Number", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!any { !it.isLetterOrDigit() }) { // Check for special character
            Toast.makeText(this@SignUpActivity, "Password must contain at least one special character", Toast.LENGTH_SHORT).show()
            return false
        }
        if (length < 8) {
            Toast.makeText(this@SignUpActivity, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show()
            return false
        }
        return true // Password meets all criteria
    }

    //Check Internet Connection
    private fun isNetworkConnected(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

}