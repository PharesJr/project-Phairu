package com.example.project_phairu

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.project_phairu.Model.CommentsModel
import com.example.project_phairu.Model.UserModel
import com.example.project_phairu.databinding.ActivityCommentsBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentsActivity : AppCompatActivity() {

    //binding
    private lateinit var binding: ActivityCommentsBinding

    //postId
    private var postId: String? = null
    //senderId
    private var senderId: String? = null

    //firebaseUser
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        postId = intent.getStringExtra("postId")
        senderId = intent.getStringExtra("senderId")

        Log.d("CommentsActivity", "Received postId: $postId")
        if (postId == null) {
            Log.d("CommentsActivity", "Null check failed: postId is null")
           Toast.makeText(this, "Error: Post is missing", Toast.LENGTH_SHORT).show()
            return
        }


        // Initialize binding
        binding = ActivityCommentsBinding.inflate(layoutInflater)
        setContentView(binding.root)



        //Initialize firebaseUser
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        //get the user info
        userInfo()

        //Post Comment Button
        binding.postComment.setOnClickListener {
            addComment()
            Log.d("CommentsActivity", "Comment added successfully")
        }

        //back Icon
        binding.cancelComment.setOnClickListener {
            finish()
        }

    }

    private fun userInfo () {
        val userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.uid)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    val user = snapshot.getValue(UserModel::class.java)

                    // Get current user Profile Picture
                    Picasso.get().load(user?.profilePicture)
                        .placeholder(R.drawable.profile_placeholder).into(binding.commentsUserProfilePic)

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CommentsActivity", "Error fetching data: ${error.message}")
                Toast.makeText(this@CommentsActivity, "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addComment() {

        if (postId == null) {
            Toast.makeText(this, "Error: Post is missing", Toast.LENGTH_SHORT).show()
            return
        }

        val commentsRef = FirebaseDatabase.getInstance().reference.child("Comments").child(postId!!)

        // Generate a unique comment ID
        val commentId = commentsRef.push().key

        // Get the comment text
        val commentText = binding.addComment.text.toString().trim()

        // Check if the comment text is not empty
        if (commentText.isNotBlank()) {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val comment = CommentsModel(
                senderId = firebaseUser?.uid,
                comment = commentText,
                commentDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(
                    Date()
                )
            )

            if (commentId != null) {
                commentsRef.child(commentId).setValue(comment)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Comment added successfully", Toast.LENGTH_SHORT).show()
                        binding.addComment.text.clear()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error adding comment: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show()
        }

    }
}