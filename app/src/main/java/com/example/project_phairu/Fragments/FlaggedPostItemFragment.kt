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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_phairu.Adapter.CommentsAdapter
import com.example.project_phairu.Adapter.FlaggedPostsAdapter
import com.example.project_phairu.Model.CommentsModel
import com.example.project_phairu.Model.FlaggedContentModel
import com.example.project_phairu.Model.PostsModel
import com.example.project_phairu.Model.UserModel
import com.example.project_phairu.R
import com.example.project_phairu.databinding.FragmentFlaggedPostItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class FlaggedPostItemFragment : Fragment() {

    //binding
    private lateinit var binding: FragmentFlaggedPostItemBinding

    //postId
    private var postId: String? = null

    //Nav Controller
    private lateinit var navController: NavController

    // flagged posts and postsAdapter
    var flaggedPostsList: MutableList<FlaggedContentModel>? = null
    private lateinit var flaggedPostsAdapter: FlaggedPostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFlaggedPostItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //postId
        postId = arguments?.getString("postId")

        //Initialize navController
        navController = findNavController()
        
        Log.d("FlaggedPostItemFragment", "Received postId: $postId")
        
        if (postId == null) {
            Log.d("FlaggedPostItemFragment", "Null check failed: postId is null")
            Toast.makeText(requireContext(), "Error: Post is missing", Toast.LENGTH_SHORT).show()
            return
        }

        //Instantiate the Recyclerview
        val recyclerView = binding.flaggedContentRecyclerView
        val linearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)

        // Initialize the commentsList and CommentsAdapter
        flaggedPostsList = mutableListOf()
        flaggedPostsAdapter = FlaggedPostsAdapter(requireContext(), flaggedPostsList as MutableList<FlaggedContentModel>)
        recyclerView.adapter = flaggedPostsAdapter

        //get the post info
        getPostInfo()

        //read flagged reasons
        readFlaggedReasons()


        //back Icon
        binding.cancelComment.setOnClickListener {
            //Navigate to back stack
            navController.popBackStack()
        }
        
    }

    private fun readFlaggedReasons() {
        val flaggedPostsRef = FirebaseDatabase.getInstance().reference.child("FlaggedContent").child(postId.toString())

        flaggedPostsRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    (flaggedPostsList as MutableList<FlaggedContentModel>).clear()
                    for (flaggedPostSnapshot in snapshot.children) {
                        val flaggedPost = flaggedPostSnapshot.getValue(FlaggedContentModel::class.java)
                        if (flaggedPost != null) {
                            (flaggedPostsList as MutableList<FlaggedContentModel>).add(flaggedPost)
                        }
                    }
                    flaggedPostsAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(requireContext(), "No flagged posts found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FlaggedPostItemFragment", "Error fetching data: ${error.message}")
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
                Log.e("FlaggedPostItemFragment", "Error fetching data: ${error.message}")
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
                Log.e("FlaggedPostItemFragment", "Error fetching data: ${error.message}")
                Toast.makeText(requireContext(), "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
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
}