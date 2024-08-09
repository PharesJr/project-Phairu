package com.example.project_phairu.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_phairu.Adapter.PostAdapter
import com.example.project_phairu.Model.BookmarkModel
import com.example.project_phairu.Model.FlaggedContentModel
import com.example.project_phairu.Model.PostsModel
import com.example.project_phairu.R
import com.example.project_phairu.databinding.FragmentFlaggedPostsBinding
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FlaggedPostsFragment : Fragment() {

    //binding
    private lateinit var binding: FragmentFlaggedPostsBinding

    //firebase
    private lateinit var firebaseUser: FirebaseUser

    //profileId
    private var profileId: String? = null

    // Instantiate the navController
    private lateinit var navController: NavController

    //Bookmarks list and postAdapter
    var flaggedPostsList: List<PostsModel> = emptyList()
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFlaggedPostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize FirebaseUser
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        //profileId
        profileId = arguments?.getString("profileId")

        // Initialize the navController
        navController = findNavController()


        //Instantiate the user saved Posts Recyclerview
        val bookmarksRecyclerView = binding.flaggedPostsRecyclerview
        val bookmarksLinearLayoutManager = LinearLayoutManager(requireContext())
        bookmarksLinearLayoutManager.reverseLayout = true
        bookmarksLinearLayoutManager.stackFromEnd = true
        bookmarksRecyclerView.layoutManager = bookmarksLinearLayoutManager
        bookmarksRecyclerView.setHasFixedSize(true)

        // Initialize the flaggedList and postAdapter
        flaggedPostsList = mutableListOf()
        postAdapter = PostAdapter(requireContext(), flaggedPostsList as MutableList<PostsModel>, "flagged")
        bookmarksRecyclerView.adapter = postAdapter

        // get the flagged Posts
        flaggedPosts()

        // backBtn
        binding.backBtn.setOnClickListener {
            //Navigate to the backStack
            navController.popBackStack()
        }

    }

    private fun flaggedPosts() {
        val flagRef = FirebaseDatabase.getInstance().reference.child("FlaggedContent")

        flagRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (flaggedPostsList as MutableList<PostsModel>).clear()
                if (snapshot.exists()) {
                    val postIds = mutableListOf<String>()
                    for (postSnapshot in snapshot.children) {
                        val postId = postSnapshot.key
                        postId?.let { postIds.add(it) }
                    }

                    val flaggedPosts = mutableListOf<Pair<PostsModel, Long>>()

                    val postFetchTasks = mutableListOf<Task<DataSnapshot>>()

                    for (postId in postIds) {
                        val postRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId)
                        val flaggedSnapshot = snapshot.child(postId)
                        val flaggedPost = flaggedSnapshot.getValue(FlaggedContentModel::class.java)
                        val flaggedTimestamp = flaggedPost?.flaggedTimestamp?.toLong() ?: 0L

                        val postFetchTask = postRef.get().addOnSuccessListener { postSnapshot ->
                            val post = postSnapshot.getValue(PostsModel::class.java)
                            if (post != null) {
                                flaggedPosts.add(Pair(post, flaggedTimestamp))
                            }
                        }
                        postFetchTasks.add(postFetchTask)
                    }

                    Tasks.whenAllComplete(postFetchTasks).addOnCompleteListener {
                        flaggedPosts.sortByDescending { it.second }
                        (flaggedPostsList as MutableList<PostsModel>).clear()
                        (flaggedPostsList as MutableList<PostsModel>).addAll(flaggedPosts.map { it.first })
                        Log.d("FlaggedPostsFragment", "Flagged posts: $flaggedPostsList")
                        postAdapter.notifyDataSetChanged()
                    }
                } else {
                    postAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FlaggedPostsFragment", "Error fetching flagged posts: ${error.message}")
            }
        })
    }
}