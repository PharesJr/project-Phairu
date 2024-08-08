package com.example.project_phairu.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_phairu.Adapter.CommentsAdapter
import com.example.project_phairu.Model.BookmarkModel
import com.example.project_phairu.Model.CommentsModel
import com.example.project_phairu.Model.NotificationsModel
import com.example.project_phairu.Model.PostsModel
import com.example.project_phairu.Model.UserModel
import com.example.project_phairu.R
import com.example.project_phairu.databinding.FragmentCommentsBinding
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

class CommentsFragment : Fragment() {

    //binding
    private lateinit var binding: FragmentCommentsBinding

    //postId
    private var postId: String? = null
    //senderId
    private var senderId: String? = null

    //firebaseUser
    private lateinit var firebaseUser: FirebaseUser

    //Nav Controller
    private lateinit var navController: NavController

    //users Comments and commentsAdapter
    var commentsList: MutableList<CommentsModel>? = null
    private lateinit var commentsAdapter: CommentsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCommentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //postId
        postId = arguments?.getString("postId")
        //senderId
        senderId = arguments?.getString("senderId")

        Log.d("CommentsActivity", "Received postId: $postId")
        if (postId == null) {
            Log.d("CommentsActivity", "Null check failed: postId is null")
            Toast.makeText(requireContext(), "Error: Post is missing", Toast.LENGTH_SHORT).show()
            return
        }

        //Instantiate the Recyclerview
        val recyclerView = binding.commentsRecyclerView
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)

        // Initialize the commentsList and CommentsAdapter
        commentsList = mutableListOf()
        commentsAdapter = CommentsAdapter(requireContext(), commentsList as MutableList<CommentsModel>)
        recyclerView.adapter = commentsAdapter



        //Initialize firebaseUser
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        //get the user info
        userInfo()

        //get the post info
        getPostInfo()

        //read comments
        readComments()

        //check bookmark status
        checkBookmarkStatus(postId.toString(), binding.postSaveCommentBtn)


        // Check if liked.... and set likes count
        val likeBtn = binding.postImageLikeBtn
        val likesCount = binding.likesCount
        isLiked(postId.toString(), likeBtn)
        likesCount(postId.toString(), likesCount)

        //get Comments count
        val commentsCount = binding.commentsCount
        commentsCount(postId.toString(), commentsCount)

        // Like button functionality
        likeBtn.setOnClickListener {
            val likesRef = FirebaseDatabase.getInstance().reference.child("Likes").child(postId.toString())

            if (likeBtn.tag == "Liked") {
                likesRef.child(firebaseUser.uid).removeValue()
                    .addOnSuccessListener {
                        likeBtn.setImageResource(R.drawable.heart)
                        likeBtn.tag = "Like"
                        updateLikeButton(likeBtn, false)
                        updateLikesCount(postId.toString(), binding.likesCount)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error unliking post: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                likesRef.child(firebaseUser.uid).setValue(true)
                    .addOnSuccessListener {
                        likeBtn.setImageResource(R.drawable.heart_red)
                        likeBtn.tag = "Liked"
                        updateLikeButton(likeBtn, true)
                        updateLikesCount(postId.toString(), binding.likesCount)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error liking post: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // Bookmark button functionality
        binding.postSaveCommentBtn.setOnClickListener {
            if (binding.postSaveCommentBtn.tag == "Save") {

                val bookmark = BookmarkModel(System.currentTimeMillis().toString(), true)

                FirebaseDatabase.getInstance().reference.child("Bookmarks").child(firebaseUser.uid).child(
                    postId!!
                ).setValue(bookmark)
                Toast.makeText(requireContext(), "Post saved.", Toast.LENGTH_SHORT).show()
            } else{
                FirebaseDatabase.getInstance().reference.child("Bookmarks").child(firebaseUser.uid).child(
                    postId!!
                ).removeValue()
                Toast.makeText(requireContext(), "Post removed from bookmarks.", Toast.LENGTH_SHORT).show()
            }
        }


        //Post Comment Button
        binding.postComment.setOnClickListener {
            addComment()
            Log.d("CommentsActivity", "Comment added successfully")
        }



        //back Icon
        binding.cancelComment.setOnClickListener {
           //Navigate to back stack
            requireActivity().supportFragmentManager.popBackStack()
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
                Toast.makeText(requireContext(), "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getPostInfo () {
        val getPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postId.toString())

        getPostRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    val post = snapshot.getValue(PostsModel::class.java)

                    // Check for image URL (if empty, hide ImageView)
                    if (post != null) {
                        if (post.postPicture != null) {
                            // Load image using Picasso
                            Picasso.get().load(post.postPicture)
                                .placeholder(R.drawable.profile_placeholder)
                                .into(binding.commentsPostImage)
                            // Ensure ImageView is visible
                            binding.imageHolder.visibility = View.VISIBLE
                        } else {
                            // Hide ImageView
                            binding.imageHolder.visibility = View.GONE
                        }
                    }

                    // Check for post caption (if empty, hide Textview)
                    if (post != null) {
                        if (post.postDescription != null) {
                            // Ensure Textview is visible
                            binding.commentsText.visibility = View.VISIBLE
                        } else {
                            // Hide ImageView
                            binding.commentsText.visibility = View.GONE
                        }
                    }
                    // Get the Post Sender Details
                    getPostSenderDetails(post?.senderId)

                    //get the Post time and date and state the passed time
                    val relativeTime = getRelativeTime(post?.postId)
                    binding.postTime.text = relativeTime

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CommentsActivity", "Error fetching data: ${error.message}")
                Toast.makeText(requireContext(), "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getPostSenderDetails (senderId: String?) {
        val postSenderRef = FirebaseDatabase.getInstance().reference.child("Users").child(senderId.toString())

        postSenderRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(UserModel::class.java)

                    // Capitalize first and last names
                    val capitalizedFirstName = user?.firstname?.capitalize() ?: ""
                    val capitalizedLastName = user?.lastname?.capitalize() ?: ""
                    binding.postProfileName.text = "$capitalizedFirstName $capitalizedLastName"
                    binding.postProfileUsername.text = "@" + user?.username

                    // Get current user Profile Picture
                    Picasso.get().load(user?.profilePicture)
                        .placeholder(R.drawable.profile_placeholder).into(binding.postProfilePic)

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CommentsActivity", "Error fetching data: ${error.message}")
               Toast.makeText(requireContext(), "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addComment() {

        if (postId == null) {
            Toast.makeText(requireContext(), "Error: Post is missing", Toast.LENGTH_SHORT).show()
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
                    Date(),
                ),
                commentTimestamp = System.currentTimeMillis().toString()
            )

            if (commentId != null) {
                commentsRef.child(commentId).setValue(comment)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Comment added successfully", Toast.LENGTH_SHORT).show()
                        addNotification()
                        binding.addComment.text.clear()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error adding comment: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            Toast.makeText(requireContext(), "Please enter a comment", Toast.LENGTH_SHORT).show()
        }

    }

    private fun readComments() {
        val commentsRef = FirebaseDatabase.getInstance().reference.child("Comments").child(postId.toString())
        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    (commentsList as MutableList<CommentsModel>).clear()
                    for (commentSnapshot in snapshot.children) {
                        val comment = commentSnapshot.getValue(CommentsModel::class.java)
                        if (comment != null) {
                            (commentsList as MutableList<CommentsModel>).add(comment)
                        }
                    }
                    commentsAdapter.notifyDataSetChanged()
                } else {
                   Toast.makeText(requireContext(), "No comments found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
              Log.e("CommentsActivity", "Error fetching data: ${error.message}")
            }
        })
    }


    private fun isLiked(postId: String, likeButton: ImageView) {
        //get current user
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val likesRef = FirebaseDatabase.getInstance().reference.child("Likes").child(postId)

        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(firebaseUser?.uid.toString()).exists()) {
                    likeButton.setImageResource(R.drawable.heart_red)
                    likeButton.tag = "Liked"
                } else {
                    likeButton.setImageResource(R.drawable.heart)
                    likeButton.tag = "Like"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CommentsActivity", "Error checking if post is liked: ${error.message}")
            }
        })
    }

    private fun likesCount(postId: String, likesCount: TextView) {
        val likesRef = FirebaseDatabase.getInstance().reference.child("Likes").child(postId)

        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    likesCount.text = snapshot.childrenCount.toString()
                } else{
                    likesCount.text = "0"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PostAdapter", "Error getting likes count: ${error.message}")
            }
        })
    }

    private fun commentsCount(postId: String, commentsCount: TextView) {
        val commentsRef = FirebaseDatabase.getInstance().reference.child("Comments").child(postId)

        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    commentsCount.text = snapshot.childrenCount.toString()
                } else{
                    commentsCount.text = "0"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PostAdapter", "Error getting comments count: ${error.message}")
            }
        })
    }

    private fun updateLikeButton(likeButton: ImageView, isLiked: Boolean) {
        if (isLiked) {
            likeButton.setImageResource(R.drawable.heart_red)
            likeButton.tag = "Liked"
        } else {
            likeButton.setImageResource(R.drawable.heart)
            likeButton.tag = "Like"
        }
    }

    private fun updateLikesCount(postId: String, likesCountTextView: TextView) {
        val likesRef = FirebaseDatabase.getInstance().reference.child("Likes").child(postId)
        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                likesCountTextView.text = snapshot.childrenCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CommentsActivity", "Error updating likes count: ${error.message}")
            }
        })
    }

    //get bookmarks status
    private fun checkBookmarkStatus(postId: String, bookmarkButton: ImageView) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val bookmarksRef = FirebaseDatabase.getInstance().reference.child("Bookmarks").child(firebaseUser?.uid.toString())

        bookmarksRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(postId).exists()) {
                    bookmarkButton.setImageResource(R.drawable.bookmark_blue)
                    bookmarkButton.tag = "Saved"
                } else {
                    bookmarkButton.setImageResource(R.drawable.bookmark_light)
                    bookmarkButton.tag = "Save"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PostAdapter", "Error checking bookmark status: ${error.message}")
            }
        })

    }

    // Helper function to calculate and format relative time
    fun getRelativeTime(postTimestamp: String?): String {
        if (postTimestamp == null) return ""

        val currentTime = System.currentTimeMillis()
        val postTime = postTimestamp.toLongOrNull() ?: return ""
        val timeDiff = currentTime - postTime

        return when {
            timeDiff < 60000 -> "${timeDiff / 1000} sec ago"
            timeDiff < 3600000 -> "${timeDiff / 60000} min ago"
            timeDiff < 86400000 -> "${timeDiff / 3600000} hr ago"
            else -> "${timeDiff / 86400000} days ago"
        }
    }

    //Add Notifications
    private fun addNotification() {
        val notificationsRef = FirebaseDatabase.getInstance().reference.child("Notifications").child(senderId.toString())
        // Get the comment text
        val commentText = binding.addComment.text.toString().trim()

        // Check if the comment text is not empty
        if (commentText.isNotBlank()) {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val notification = NotificationsModel(
                userId = firebaseUser?.uid,
                postId = postId,
                notificationType = "comment",
                notificationMessage = commentText,
                notificationTimestamp = System.currentTimeMillis().toString()
            )

            notificationsRef.push().setValue(notification)
        }
    }
}