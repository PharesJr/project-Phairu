package com.example.project_phairu.Fragments

import android.net.Uri
import android.os.Bundle
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
import androidx.navigation.ui.setupWithNavController
import com.example.project_phairu.Model.PostsModel
import com.example.project_phairu.R
import com.example.project_phairu.databinding.FragmentCreateBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateFragment : Fragment() {

    //navController
    private lateinit var navController: NavController

    //binding
    private lateinit var binding: FragmentCreateBinding

    //firebase User
    private lateinit var firebaseUser: FirebaseUser

    //firebaseStorage
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
        binding = FragmentCreateBinding.inflate(inflater, container, false)


        // Initialize pickImage Launcher
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {imageUri = uri
                binding.postImage.setImageURI(imageUri)
                binding.postImage.visibility = View.VISIBLE
            } else {
                Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //make content visible
        binding.createContent.visibility = View.VISIBLE
        //progress bar gone
        binding.createPageLoader.visibility = View.GONE


        // Initialize navController
        navController = findNavController()

        //firebase User (get the current user ID)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        // Initialize Firebase Storage reference
        storageRef = FirebaseStorage.getInstance().reference.child("Post Pictures")


        // Find the BottomNavigation
        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.nav_view)

        // Set up the BottomNavigationView with the NavController
        bottomNavigationView.setupWithNavController(navController)


        // Add a Post Picture
        binding.imagePicker.setOnClickListener {
            // Open the image picker
            pickImageLauncher.launch(PickVisualMediaRequest.Builder().build())
        }

        //upload a post
        binding.sendPostBtn.setOnClickListener {
            uploadPost()
        }

    }

    // Upload an Image to Firebase Storage
    private fun uploadProfilePictureToFirebaseStorage(onSuccess: (String) -> Unit) {
        imageUri?.let { uri ->

            // Validate file type
            if (!isValidImageFile(uri)) {
                Toast.makeText(context, "Please select a valid image file", Toast.LENGTH_SHORT).show()
                binding.createPageLoader.visibility = View.GONE
                return // Stop further processing
            }

            // Show progress indicator
            binding.createPageLoader.visibility = View.VISIBLE
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
                        val fileRef = storageRef.child(System.currentTimeMillis().toString() + ".webp")
                        fileRef.putFile(compressedImageUri)
                            .addOnSuccessListener {
                                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->

                                    // Store the download URL
                                    val postPictureUrl = downloadUri.toString()

                                    // Call the success callback with the download URL
                                    onSuccess(postPictureUrl)

                                    // Hide progress indicator
                                    binding.createPageLoader.visibility = View.GONE
                                }
                            }
                            .addOnFailureListener { e ->
                                // Hide progress indicator
                                binding.createPageLoader.visibility = View.GONE

                                // Show error message
                                Toast.makeText(context, "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
                                Log.e("CreatePostFragment", "Error uploading image: ${e.message}")
                            }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        // Hide progress indicator
                        binding.createPageLoader.visibility = View.GONE
                        //show the scrollview
                        binding.createPageLoader.visibility = View.VISIBLE
                        Toast.makeText(context, "Error compressing image: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("CreatePostFragment", "Error compressing image: ${e.message}")
                    }
                }
            }
        }
    }


    // Helper function for image file validation
    private fun isValidImageFile(uri: Uri): Boolean {
        val mimeType = requireContext().contentResolver.getType(uri)
        val validImageTypes = listOf("image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp")
        val fileName = uri.lastPathSegment
        val validExtensions = listOf(".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp")

        return (mimeType in validImageTypes) || validExtensions.any { fileName?.endsWith(it) == true }
    }

    private fun uploadPost() {
        val postDescription = binding.postText.text.toString()
        if (postDescription.isBlank() && imageUri == null) {
            Toast.makeText(context, "Please add an image or description", Toast.LENGTH_SHORT).show()
            return
        }

        // Hide content and show progress bar
        binding.createContent.visibility = View.GONE
        binding.createPageLoader.visibility = View.VISIBLE

        if (imageUri !=null) {
            uploadProfilePictureToFirebaseStorage { postPictureUrl ->
                savePostToFirebaseDatabase(postDescription, postPictureUrl)
            }
        } else {
            savePostToFirebaseDatabase(postDescription, null)
        }
    }

    private fun savePostToFirebaseDatabase(postDescription: String, postPictureUrl: String?) {
        val postId = System.currentTimeMillis().toString()
        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val post = PostsModel(postId, firebaseUser.uid, postDescription, postPictureUrl,  time, date)

        FirebaseDatabase.getInstance().reference.child("Posts").child(postId)
            .setValue(post)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Post uploaded successfully", Toast.LENGTH_SHORT).show()
                    // Show content and hide progress bar (in both success and failure cases)
                    binding.createContent.visibility = View.VISIBLE
                    binding.createPageLoader.visibility = View.GONE
                    // Clear the post text and image after successful upload
                    binding.postText.text.clear()
                    binding.postImage.visibility = View.GONE
                    imageUri = null
                } else {
                    Toast.makeText(context, "Error uploading post", Toast.LENGTH_SHORT).show()
                    // Show content and hide progress bar (in both success and failure cases)
                    binding.createContent.visibility = View.VISIBLE
                    binding.createPageLoader.visibility = View.GONE
                }
            }
    }
}