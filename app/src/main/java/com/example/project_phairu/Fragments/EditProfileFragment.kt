package com.example.project_phairu.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.project_phairu.Model.UserModel
import com.example.project_phairu.R
import com.example.project_phairu.databinding.FragmentEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class EditProfileFragment : Fragment() {

    //binding
    private lateinit var binding: FragmentEditProfileBinding

    //navController
    private lateinit var navController: NavController

    //firebase
    private lateinit var firebaseUser: FirebaseUser

    //checker
    private var checker = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize navController
        navController = findNavController()

        //firebase User
        firebaseUser = FirebaseAuth.getInstance().currentUser!!


        //get the user info
        userInfo()


        //find the back button
        binding.backIcon.setOnClickListener {
            //navigate back to profile fragment
            findNavController().popBackStack()
        }

        //save button functionality
        binding.saveBtn.setOnClickListener {
            if (checker == "clicked") {}
            else {
                updateUserInfoOnly()
            }
        }
    }

    private fun userInfo () {
            val userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.uid)

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {
                        val user = snapshot.getValue(UserModel::class.java)

                        // Capitalize first and last names
                        val capitalizedFirstName = user?.firstname?.capitalize() ?: ""
                        val capitalizedLastName = user?.lastname?.capitalize() ?: ""

                        Picasso.get().load(user?.profilePicture)
                            .placeholder(R.drawable.profile_placeholder).into(binding.profilePic)
                        binding.firstName.setText(capitalizedFirstName)
                        binding.lastName.setText(capitalizedLastName)
                        binding.uName.setText(user?.username)
                        binding.email.setText(user?.email)
                        binding.editBio.setText(user?.bio)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileFragment", "Error fetching data: ${error.message}")
                    Toast.makeText(context, "Error loading profile data", Toast.LENGTH_SHORT).show()

                }
            })
    }

    private fun updateUserInfoOnly() {
        val firstName = binding.firstName.text.toString().lowercase().trim()
        val lastName = binding.lastName.text.toString().lowercase().trim()
        val username = binding.uName.text.toString().lowercase().trim()
        val email = binding.email.text.toString().lowercase().trim()
        val bio = binding.editBio.text.toString().trim()

        // Empty field checks
        if (firstName.isEmpty()) {
            Toast.makeText(context, "Please enter your first name", Toast.LENGTH_SHORT).show()
            return
        }
        if (lastName.isEmpty()) {
            Toast.makeText(context, "Please enter your last name", Toast.LENGTH_SHORT).show()
            return}
        if (username.isEmpty()) {
            Toast.makeText(context, "Please enter a username", Toast.LENGTH_SHORT).show()
            return
        }
        if (email.isEmpty()) {
            Toast.makeText(context, "Please enter an email", Toast.LENGTH_SHORT).show()
            return
        }
        if (bio.isEmpty()) {
            Toast.makeText(context, "Please enter a bio", Toast.LENGTH_SHORT).show()
            return
        }

        // Additional validation
        if (!email.isValidEmail()) {
            Toast.makeText(requireContext(), "Invalid email address", Toast.LENGTH_SHORT).show()
            return
        }
        if (email.any { it.isUpperCase() }) {
            Toast.makeText(requireContext(), "Email cannot contain capital letters", Toast.LENGTH_SHORT).show()
            return
        }

        val userRef = FirebaseDatabase.getInstance().reference.child("Users")

        // Check if username already exists (excluding the current user)
        userRef.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Check if the found username belongs to the current user
                        val currentUserSnapshot = dataSnapshot.children.find { it.key == firebaseUser.uid }
                        if (currentUserSnapshot == null) {
                            // Username is taken by another user
                            Toast.makeText(requireContext(), "Username already taken", Toast.LENGTH_SHORT).show()
                        } else {
                            // Username is the current user's, proceed with update
                            updateUserInDatabase(userRef, firstName, lastName, username, email, bio)
                        }
                    } else {
                        // Username is available, proceed with update
                        updateUserInDatabase(userRef, firstName, lastName, username, email, bio)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error checking username: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })


    }

    // Helper function for email validation
    private fun String.isValidEmail(): Boolean {
        return contains("@") && contains(".")
    }

    private fun updateUserInDatabase(userRef: DatabaseReference, firstName: String, lastName: String, username: String, email: String, bio: String) {
        val userMap = HashMap<String, Any>()
        userMap["firstname"] = firstName.lowercase().trim()
        userMap["lastname"] = lastName.lowercase().trim()
        userMap["username"] = username.lowercase().trim()
        userMap["email"] = email.lowercase().trim()
        userMap["bio"] = bio

        userRef.child(firebaseUser.uid).updateChildren(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(context, "Error updating profile", Toast.LENGTH_SHORT).show()
                    Log.e("EditProfileFragment", "Error updating profile: ${task.exception?.message}")
                }
            }
    }

}