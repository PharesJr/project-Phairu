package com.example.project_phairu.Fragments

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.project_phairu.DataStore.UserSessionDataStore
import com.example.project_phairu.R
import com.example.project_phairu.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch


class LoginFragment : Fragment() {

    //binding
    private lateinit var binding: FragmentLoginBinding

    //Firebase Declaration and Reference
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    //Datastore
    private lateinit var userSessionDataStore: UserSessionDataStore

    //navController
    private lateinit var navController: NavController


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize navController
        navController = findNavController()


        //firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.getReference("Users")

        //DataStore
        userSessionDataStore = UserSessionDataStore(requireContext())

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
                        Toast.makeText(requireContext(), "Email cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                    if (password.isEmpty()) {
                        Toast.makeText(requireContext(), "Password cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                } else if (email.isValidEmail() && password.isValidPassword()) {
                    //Function LoginUser
                    loginUser(email, password)
                } else {
                    // Show specific error messages for invalid input (non-empty)
                    if (!email.isValidEmail()) {
                        Toast.makeText(requireContext(), "Invalid email format", Toast.LENGTH_SHORT).show()
                    }
                    if (!password.isValidPassword()) { // Check for invalid password
                        Toast.makeText(requireContext(), "Invalid password format", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "No Internet Connection", Toast.LENGTH_SHORT).show()
            }
        }

        //signup Redirect
        binding.signUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }

        //forgot password
        binding.forgotpsswd.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            val view = layoutInflater.inflate(R.layout.dialog_forgot, null)


            builder.setView(view)
            val dialog = builder.create()

            view.findViewById<Button>(R.id.btnReset).setOnClickListener {
                val userEmail = view.findViewById<EditText>(R.id.resetEmail).text.toString().trim()

                if (userEmail.isEmpty()) {
                    Toast.makeText(requireContext(), "Email cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                } else if (!userEmail.isValidEmail()) {
                    Toast.makeText(requireContext(), "Invalid email format", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                } else {
                    resetEmail(userEmail)
                    dialog.dismiss()
                }
            }

            view.findViewById<ImageView>(R.id.backIcon).setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

    }

    //signup user function

    private fun loginUser(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                // Get the current user
                val user = firebaseAuth.currentUser// Extract the user ID
                val userId = user?.uid

                if (userId != null) {
                    // Check user type in the database
                    val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val userType = snapshot.child("type").getValue(String::class.java)
                                if (userType == "admin") {
                                    // Navigate to adminFragment
                                    findNavController().navigate(R.id.action_loginFragment_to_adminFragment)
                                    Toast.makeText(requireContext(), "Welcome Admin", Toast.LENGTH_SHORT).show()
                                } else {
                                    // Navigate to homeFragment
                                    lifecycleScope.launch {
                                        userSessionDataStore.storeUserSession(true, userId)
                                    }
                                    Toast.makeText(requireContext(), "Login Success", Toast.LENGTH_SHORT).show()
                                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("LoginFragment", "Database Error: ${error.message}")
                        }
                    })
                }
            } else {
                when (val exception = task.exception) {
                    is FirebaseAuthInvalidUserException -> {
                        Toast.makeText(requireContext(), "Invalid Email", Toast.LENGTH_SHORT).show()
                    }

                    is FirebaseAuthInvalidCredentialsException -> {
                        Toast.makeText(requireContext(), "Invalid Password", Toast.LENGTH_SHORT).show()
                    }

                    else -> {
                        Toast.makeText(requireContext(), "Login Failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    //Email Reset Function

    private fun resetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Reset Email Sent, Check your email", Toast.LENGTH_SHORT).show()} else {
                // Handle the case where the email could not be sent (optional)
                Toast.makeText(requireContext(), "Failed to send reset email", Toast.LENGTH_SHORT).show()
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
        val connectivityManager= requireContext().getSystemService(ConnectivityManager::class.java)
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)}
}