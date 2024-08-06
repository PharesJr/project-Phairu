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
import com.example.project_phairu.Model.PostsModel
import com.example.project_phairu.databinding.FragmentBookmarksBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class bookmarksFragment : Fragment() {

    //binding
    private lateinit var binding: FragmentBookmarksBinding

    //firebase
    private lateinit var firebaseUser: FirebaseUser

    //profileId
    private var profileId: String? = null

    // Instantiate the navController
    private lateinit var navController: NavController

    //Bookmarks list and postAdapter
    var bookmarkedPostsList: List<PostsModel> = emptyList()
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBookmarksBinding.inflate(inflater, container, false)
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
        val bookmarksRecyclerView = binding.userSavedPostsRecyclerview
        val bookmarksLinearLayoutManager = LinearLayoutManager(requireContext())
        bookmarksLinearLayoutManager.reverseLayout = true
        bookmarksLinearLayoutManager.stackFromEnd = true
        bookmarksRecyclerView.layoutManager = bookmarksLinearLayoutManager
        bookmarksRecyclerView.setHasFixedSize(true)


        // Initialize the postList and postAdapter
        bookmarkedPostsList = mutableListOf()
        postAdapter = PostAdapter(requireContext(), bookmarkedPostsList as MutableList<PostsModel>, "bookmarks")
        bookmarksRecyclerView.adapter = postAdapter

        // get the bookmarked Posts
        myBookmarks()

        // backBtn
        binding.backBtn.setOnClickListener {
          //Navigate to the backStack
            navController.popBackStack()
        }
    }

    private fun myBookmarks() {
        val bookmarksRef = FirebaseDatabase.getInstance().reference.child("Bookmarks").child(firebaseUser.uid)

        bookmarksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (bookmarkedPostsList as MutableList<PostsModel>).clear()

                if (snapshot.exists()) {
                    val postIds = mutableListOf<String>()
                    for (postSnapshot in snapshot.children) {
                        val postId = postSnapshot.key
                        postId?.let { postIds.add(it) }
                    }

                    val bookmarkedPosts = mutableListOf<Pair<PostsModel, Long>>()
                    var postsFetched = 0

                    for (postId in postIds) {
                        val postRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId)
                        postRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(postSnapshot: DataSnapshot) {
                                val post = postSnapshot.getValue(PostsModel::class.java)
                                val bookmarkSnapshot = snapshot.child(postId)
                                val bookmark = bookmarkSnapshot.getValue(BookmarkModel::class.java)
                                val bookmarkTimestamp = bookmark?.timestamp?.toLong() ?: 0L

                                if (post != null) {
                                    bookmarkedPosts.add(Pair(post, bookmarkTimestamp))
                                }

                                postsFetched++
                                if (postsFetched == postIds.size) {
                                    bookmarkedPosts.sortByDescending { it.second }
                                    (bookmarkedPostsList as MutableList<PostsModel>).clear()
                                    (bookmarkedPostsList as MutableList<PostsModel>).addAll(bookmarkedPosts.map { it.first })
                                    Log.d("BookmarksFragment", "Bookmarked posts: $bookmarkedPostsList")
                                    postAdapter.notifyDataSetChanged()

                                    // Show ScrollView and hide ProgressBar
                                    binding.bookmarksScrollview.visibility = View.VISIBLE
                                    binding.bookmarksPageLoader.visibility = View.GONE
                                } else {
                                    // Show ScrollView and hide ProgressBar (even if no bookmarks)
                                    postAdapter.notifyDataSetChanged()
                                    binding.bookmarksScrollview.visibility = View.VISIBLE
                                    binding.bookmarksPageLoader.visibility = View.GONE
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("BookmarksFragment", "Error fetching post data: ${error.message}")
                            }
                        })
                    }
                } else {
                    postAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("BookmarksFragment", "Error fetching bookmarks: ${error.message}")
            }
        })
    }

}