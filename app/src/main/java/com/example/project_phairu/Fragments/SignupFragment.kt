package com.example.project_phairu.Fragments
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.CONNECTIVITY_SERVICE
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.project_phairu.Model.UserModel
import com.example.project_phairu.databinding.FragmentSignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SignupFragment : Fragment() {

    //binding
    private lateinit var binding: FragmentSignupBinding

    //Firebase Declaration and Reference
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    //navController
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSignupBinding.inflate(inflater, container, false)
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

        // SignUp Button and the call function for user sign up

        binding.createAccountButton.setOnClickListener{
            val firstname = binding.Fname.text.toString().lowercase().trim()
            val lastname =  binding.Lname.text.toString().lowercase().trim()
            val username =  binding.Username.text.toString().lowercase().trim()
            val email =  binding.email.text.toString().lowercase().trim()
            val password =  binding.password.text.toString().trim()
            val confirmPassword =  binding.confirmPassword.text.toString().trim()


                if (isNetworkConnected()) {
                    if (firstname.isNotEmpty() && lastname.isNotEmpty() && username.isNotEmpty() &&
                        email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {

                        if (password != confirmPassword) {
                            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                        } else if (!password.isValidPassword()) {
                            // Helper function for password validation
                            // It will show the appropriate Toast message based on the failed condition. Message is in the helper function
                        } else if (!email.isValidEmail()) {
                            // Use a helper function for email validation
                            Toast.makeText(requireContext(), "Invalid email address", Toast.LENGTH_SHORT).show()
                        } else if (password.contains(" ") || email.contains(" ") || username.contains(" ")) {
                            Toast.makeText(requireContext(), "Password, email, and username cannot contain spaces", Toast.LENGTH_SHORT).show()
                        } else if (email.any { it.isUpperCase() }) {
                            Toast.makeText(requireContext(), "Email cannot contain capital letters", Toast.LENGTH_SHORT).show()
                        } else {
                            signUpUser(firstname, lastname, username, email, password)
                        }

                    } else {
                        Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "No Internet Connection", Toast.LENGTH_SHORT).show()
                }





        }

        // Set a click listener on the "Back" TextView
        binding.btnBack.setOnClickListener {
            //go back to the login page
            findNavController().popBackStack()
        }
    }

    //Fn to sign up a user to Firebase Realtime DB and Firebase Authentication

    private fun signUpUser(firstname: String, lastname: String, username: String, email: String, password: String) {
        databaseReference.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Username already exists
                        Toast.makeText(requireContext(),"Username already taken", Toast.LENGTH_SHORT).show()
                    } else {
                        // Username is available, proceed with Firebase Authentication
                        firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(requireActivity()) { task ->
                                if (task.isSuccessful) {
                                    // Authentication successful, now save to Realtime Database
                                    val user = firebaseAuth.currentUser
                                    val userId = user?.uid ?: return@addOnCompleteListener

                                    val userData = UserModel(userId, firstname, lastname, username, email)

                                    databaseReference.child(userId).setValue(userData)
                                        .addOnSuccessListener {
                                            Toast.makeText(requireContext(), "Sign Up Success", Toast.LENGTH_SHORT).show()
                                            // Navigate back to LoginFragment using Navigation Component
                                            findNavController().popBackStack()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(requireContext(), "Error saving user data: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    // Authentication failed
                                    Toast.makeText(requireContext(), "Sign Up Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(requireContext(), "Password must contain at least one capital letter", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!any { it.isLowerCase() }) {
            Toast.makeText(requireContext(), "Password must contain at least one lowercase letter", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!any { it.isDigit() }) {
            Toast.makeText(requireContext(), "Password must contain at least one Number", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!any { !it.isLetterOrDigit() }) { // Check for special character
            Toast.makeText(requireContext(), "Password must contain at least one special character", Toast.LENGTH_SHORT).show()
            return false
        }
        if (length < 8) {
            Toast.makeText(requireContext(), "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show()
            return false
        }
        return true // Password meets all criteria
    }

    //Check Internet Connection
    private fun isNetworkConnected(): Boolean {
        val connectivityManager = requireContext().getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}