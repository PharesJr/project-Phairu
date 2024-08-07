package com.example.project_phairu.Fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.project_phairu.Model.EventsModel
import com.example.project_phairu.Model.PostsModel
import com.example.project_phairu.R
import com.example.project_phairu.databinding.FragmentEventsUploadBinding
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EventsUploadFragment : Fragment() {

    // Binding
    private lateinit var binding: FragmentEventsUploadBinding

    //navController
    private lateinit var navController: NavController

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
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentEventsUploadBinding.inflate(inflater, container, false)

        // Initialize pickImage Launcher
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {imageUri = uri
                binding.eventPoster.setImageURI(imageUri)
            } else {
                Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize navController
        navController = findNavController()

        //firebase User (get the current user ID)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        // Initialize Firebase Storage reference
        storageRef = FirebaseStorage.getInstance().reference.child("Event Posters Pictures")

        // Add a Post Picture
        binding.imagePicker.setOnClickListener {
            // Open the image picker
            pickImageLauncher.launch(PickVisualMediaRequest.Builder().build())
        }

        //Add an event date
        binding.eventDate.setOnClickListener {
            showDatePicker()
        }

        //Add an event start time
        binding.eventStartTime.setOnClickListener {
            showTimePicker(binding.eventStartTime)
        }

        //Add an event end time
        binding.eventEndTime.setOnClickListener {
            showTimePicker(binding.eventEndTime)
        }

        //upload a post
        binding.uploadEventBtn.setOnClickListener {
            uploadEvent()
        }
    }

    // Upload an Image to Firebase Storage
    private fun uploadProfilePictureToFirebaseStorage(onSuccess: (String) -> Unit) {
        imageUri?.let { uri ->

            // Validate file type
            if (!isValidImageFile(uri)) {
                Toast.makeText(context, "Please select a valid image file", Toast.LENGTH_SHORT).show()
                return // Stop further processing
            }

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
                                    val eventsPictureUrl = downloadUri.toString()

                                    // Call the success callback with the download URL
                                    onSuccess(eventsPictureUrl)
                                }
                            }
                            .addOnFailureListener { e ->
                                // Show error message
                                Toast.makeText(context, "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
                                Log.e("CreatePostFragment", "Error uploading image: ${e.message}")
                            }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error compressing image: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("CreatePostFragment", "Error compressing image: ${e.message}")
                    }
                }
            }
        }
    }


    private fun uploadEvent() {
        val eventTitle = binding.eventTitle.text.toString().lowercase()
        val eventLocation = binding.eventLocation.text.toString().lowercase()
        val eventDate = binding.eventDate.text.toString()
        val eventStartTime = binding.eventStartTime.text.toString()
        val eventEndTime = binding.eventEndTime.text.toString()
        val eventDescription = binding.eventDescription.text.toString().lowercase()
        val eventPoster = binding.eventPoster.toString()

        if (eventTitle.isBlank()) {
            Toast.makeText(context, "Please enter an event title", Toast.LENGTH_SHORT).show()
            return
        } else if (eventLocation.isBlank()) {
            Toast.makeText(context, "Please enter an event location", Toast.LENGTH_SHORT).show()
            return
        } else if (eventDate.isBlank()) {
            Toast.makeText(context, "Please enter an event date", Toast.LENGTH_SHORT).show()
            return
        } else if (eventStartTime.isBlank()) {
            Toast.makeText(context, "Please enter an event start time", Toast.LENGTH_SHORT).show()
            return
        } else if (eventEndTime.isBlank()) {
            Toast.makeText(context, "Please enter an event end time", Toast.LENGTH_SHORT).show()
            return
        } else if (imageUri == null) {
            Toast.makeText(context, "Please select a valid event poster", Toast.LENGTH_SHORT).show()
            return
        } else if (eventPoster.isBlank()){
            Toast.makeText(context, "Please select an event poster", Toast.LENGTH_SHORT).show()
            return
        }
        else {
            // All fields are filled
            uploadProfilePictureToFirebaseStorage { eventPosterUrl ->
                saveEventToFirebaseDatabase(eventTitle, eventLocation, eventDate, eventStartTime, eventEndTime, eventDescription, eventPosterUrl)
            }
        }
    }

    private fun saveEventToFirebaseDatabase(eventTitle: String, eventLocation: String, eventDate: String, eventStartTime: String, eventEndTime: String, eventDescription: String, eventPosterUrl: String) {
        val eventId = System.currentTimeMillis().toString()
        val userId = firebaseUser.uid

        val event = EventsModel(
            eventId = eventId,
            eventCreatorId = userId,
            eventName = eventTitle,
            eventLocation = eventLocation,
            eventDate = eventDate,
            eventStartTime = eventStartTime,
            eventEndTime = eventEndTime,
            eventDescription = eventDescription,
            eventPicture = eventPosterUrl
        )
        FirebaseDatabase.getInstance().reference.child("Events").child(eventId)
            .setValue(event)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Event uploaded successfully", Toast.LENGTH_SHORT).show()

                    // Clear the post text and image after successful upload
                    binding.eventTitle.text.clear()
                    binding.eventLocation.text.clear()
                    binding.eventDate.text.clear()
                    binding.eventStartTime.text.clear()
                    binding.eventEndTime.text.clear()
                    binding.eventDescription.text.clear()
                    // Clear the ImageView
                    binding.eventPoster.setImageURI(null)
                    //Navigate to other Fragment
                    navController.popBackStack()
                } else {
                    Toast.makeText(context, "Error uploading post", Toast.LENGTH_SHORT).show()

                }
            }
    }

    // Helper function for image file validation
    private fun isValidImageFile(uri: Uri): Boolean {
        val mimeType = requireContext().contentResolver.getType(uri)
        val validImageTypes = listOf("image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp")
        return mimeType in validImageTypes
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                // Handle the selected date
             val selectedDate = "$dayOfMonth/${month + 1}/$year"
                // Update the EditText with the selected date
                binding.eventDate.setText(selectedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun showTimePicker(editText: EditText) {
        val calendar = android.icu.util.Calendar.getInstance()
        val hour = calendar.get(android.icu.util.Calendar.HOUR_OF_DAY)
        val minute = calendar.get(android.icu.util.Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                editText.setText(selectedTime)
            },
            hour,
            minute,
            false
        )
        timePickerDialog.show()
    }

}