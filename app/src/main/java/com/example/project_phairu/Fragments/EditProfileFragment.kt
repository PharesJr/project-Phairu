package com.example.project_phairu.Fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class EditProfileFragment : Fragment() {

    //binding
    private lateinit var binding: FragmentEditProfileBinding

    //navController
    private lateinit var navController: NavController

    //firebase
    private lateinit var firebaseUser: FirebaseUser

    private lateinit var storageRef: StorageReference

    // To store the selected image URI
    private var imageUri: Uri? = null

    // Image Launcher
    private lateinit var pickImageLauncher: ActivityResultLauncher<PickVisualMediaRequest>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        // Initialize pickImage Launcher
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {imageUri = uri
                binding.profilePic.setImageURI(imageUri)
            } else {
                Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //show page content
        binding.editProfileScrollView.visibility = View.VISIBLE
        binding.editProfilePageLoader.visibility = View.GONE

        // Initialize navController
        navController = findNavController()

        //firebase User
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        // Initialize Firebase Storage reference
        storageRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")


        //get the user info
        userInfo()


        //find the back button
        binding.backIcon.setOnClickListener {
            //navigate back to profile fragment
            findNavController().popBackStack()
        }

        //save button functionality
        binding.saveBtn.setOnClickListener {

            //update the user Information
            updateUserInfo()
        }


        // Change Profile Picture Activity
        binding.changeProfilePicText.setOnClickListener {
            // Open the image picker
            pickImageLauncher.launch(PickVisualMediaRequest.Builder().build())
        }
    }




    // Upload an Image to Firebase Storage
    private fun uploadProfilePictureToFirebaseStorage(onSuccess: (String) -> Unit) {
        imageUri?.let { uri ->

            // Validate file type
            if (!isValidImageFile(uri)) {
                Toast.makeText(context, "Please select a valid image file", Toast.LENGTH_SHORT).show()
                binding.editProfilePageLoader.visibility = View.GONE
                return // Stop further processing
            }

            // Show progress indicator
            binding.editProfilePageLoader.visibility = View.VISIBLE
            // Start a coroutine to compress and upload the image
            lifecycleScope.launch(Dispatchers.IO) {
                try {

                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    val tempFile = File.createTempFile("temp_image", ".jpg", requireContext().cacheDir)
                    inputStream?.use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    val compressedImageFile = Compressor.compress(requireContext(), tempFile)
                    val compressedImageUri = Uri.fromFile(compressedImageFile)

                    withContext(Dispatchers.Main) {
                        val fileRef = storageRef.child(firebaseUser.uid + ".webp")
                        fileRef.putFile(compressedImageUri)
                            .addOnSuccessListener {
                                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->

                                    // Store the download URL
                                    val profilePictureUrl = downloadUri.toString()

                                    // Call the success callback with the download URL
                                    onSuccess(profilePictureUrl)

                                    // Hide progress indicator
                                    binding.editProfilePageLoader.visibility = View.GONE
                                }
                            }
                            .addOnFailureListener { e ->
                                // Hide progress indicator
                                binding.editProfilePageLoader.visibility = View.GONE

                                // Show error message
                                Toast.makeText(context, "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
                                Log.e("EditProfileFragment", "Error uploading image: ${e.message}")
                            }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        // Hide progress indicator
                        binding.editProfilePageLoader.visibility = View.GONE
                        //show the scrollview
                        binding.editProfileScrollView.visibility = View.VISIBLE
                        Toast.makeText(context, "Error compressing image: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("EditProfileFragment", "Error compressing image: ${e.message}")
                    }
                }
            }
        }
    }

    // Update user in database
    private fun performUpdate(userRef: DatabaseReference, firstName: String, lastName: String, username: String, email: String, bio: String, profilePictureUrl: String?) {
        val userMap = HashMap<String, Any>()
        userMap["firstname"] = firstName
        userMap["lastname"] = lastName
        userMap["username"] = username
        userMap["email"] = email
        userMap["bio"] = bio

        profilePictureUrl?.let {
            userMap["profilePicture"] = it
        }

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

    private fun updateUserInfo () {
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
        // Show progress indicator
        binding.editProfilePageLoader.visibility = View.VISIBLE

        val userRef = FirebaseDatabase.getInstance().reference.child("Users")

        // Check if a new image was selected
        if (imageUri != null) {

            // Upload the new image and then update user info
            uploadProfilePictureToFirebaseStorage { profilePictureUrl ->
                // This callback is executed after the image is uploaded
                checkAndPerformUpdate(userRef, firstName, lastName, username, email, bio, profilePictureUrl)
            }
        } else {
            // No new image, proceed with regular update
            checkAndPerformUpdate(userRef, firstName, lastName, username, email, bio, null)
        }


    }

    // Helper function to check username and perform update
    private fun checkAndPerformUpdate(userRef: DatabaseReference, firstName: String, lastName: String, username: String, email: String, bio: String, profilePictureUrl: String?) {
        userRef.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Check if the found username belongs to the current user
                        val currentUserSnapshot = dataSnapshot.children.find { it.key == firebaseUser.uid }
                        if (currentUserSnapshot == null) {
                            // Username is taken by another user
                            Toast.makeText(requireContext(), "Username already taken", Toast.LENGTH_SHORT).show()
                        } else {// Username is the current user's, proceed with update
                            performUpdate(userRef, firstName, lastName, username, email, bio, profilePictureUrl)
                        }
                    } else {
                        // Username is available, proceed with update
                        performUpdate(userRef, firstName, lastName, username, email, bio, profilePictureUrl)
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

    // Helper function for image file validation
    private fun isValidImageFile(uri: Uri): Boolean {
        val mimeType = requireContext().contentResolver.getType(uri)
        val validImageTypes = listOf("image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp")
        val fileName = uri.lastPathSegment
        val validExtensions = listOf(".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp")

        return (mimeType in validImageTypes) || validExtensions.any { fileName?.endsWith(it) == true }
    }


}