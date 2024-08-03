package com.example.project_phairu

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_phairu.Adapter.PostAdapter
import com.example.project_phairu.Model.BookmarkModel
import com.example.project_phairu.Model.PostsModel
import com.example.project_phairu.databinding.ActivityBookmarksBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BookmarksActivity : AppCompatActivity() {

    //binding
    private lateinit var binding: ActivityBookmarksBinding

    //firebase
    private lateinit var firebaseUser: FirebaseUser

    //Bookmarks list and postAdapter
    var bookmarkedPostsList: List<PostsModel> = emptyList()
    private lateinit var postAdapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize binding
        binding = ActivityBookmarksBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Initialize FirebaseUser
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        //Instantiate the user saved Posts Recyclerview
        val bookmarksRecyclerView = binding.userSavedPostsRecyclerview
        val bookmarksLinearLayoutManager = LinearLayoutManager(this)
        bookmarksLinearLayoutManager.reverseLayout = true
        bookmarksLinearLayoutManager.stackFromEnd = true
        bookmarksRecyclerView.layoutManager = bookmarksLinearLayoutManager
        bookmarksRecyclerView.setHasFixedSize(true)

        // Initialize the postList and postAdapter
        bookmarkedPostsList = mutableListOf()
        postAdapter = PostAdapter(this, bookmarkedPostsList as MutableList<PostsModel>)
        bookmarksRecyclerView.adapter = postAdapter

        // get the bookmarked Posts
        myBookmarks()

        // backBtn
        binding.backBtn.setOnClickListener {
            finish()
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
                                    Log.d("BookmarksActivity", "Bookmarked posts: $bookmarkedPostsList")
                                    postAdapter.notifyDataSetChanged()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("BookmarksActivity", "Error fetching post data: ${error.message}")
                            }
                        })
                    }
                } else {
                    postAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("BookmarksActivity", "Error fetching bookmarks: ${error.message}")
            }
        })
    }
}