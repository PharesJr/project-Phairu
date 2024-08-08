package com.example.project_phairu.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_phairu.Adapter.PostAdapter
import com.example.project_phairu.DataStore.UserSessionDataStore
import com.example.project_phairu.Model.PostsModel
import com.example.project_phairu.R
import com.example.project_phairu.databinding.FragmentHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    //binding
    private lateinit var binding: FragmentHomeBinding

    //Drawer
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    //userSession
    private lateinit var userSessionDataStore: UserSessionDataStore

    //backPress timer
    private var backPressedTime: Long = 0

    //navController
    private lateinit var navController: NavController

    //postAdapter and PostModel
    private lateinit var postAdapter: PostAdapter
    private lateinit var postList: MutableList<PostsModel>

    //get people following a certain user
    private lateinit var followingUsersList: MutableList<PostsModel>



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        //Instantiate the RecyclerView
        val recyclerView: RecyclerView? = view?.findViewById(R.id.timelineRecyclerView)
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        if (recyclerView != null) {
            recyclerView.layoutManager = linearLayoutManager
        }

        // Initialize navController
        navController = findNavController()

        // Initialize the postList and postAdapter
        postList = mutableListOf()
        postAdapter = PostAdapter(requireContext(), postList, "home")
        recyclerView?.adapter = postAdapter


        // Initialize the followingUsersList
        followingUsersList = mutableListOf()

        checkFollowings()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Initialize binding
        binding = FragmentHomeBinding.bind(view)

        // Initialize navController
        navController = findNavController()


        // Find the BottomNavigation
        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.nav_view)

        // Set up the BottomNavigationView with the NavController
        bottomNavigationView.setupWithNavController(navController)


        //backPressedTime
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {

            // Check if back button is pressed twice within 2 seconds
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                // Exit the app
                requireActivity().finish()
            } else {
                Toast.makeText(requireContext(), "Click again to exit the app", Toast.LENGTH_SHORT).show()
                backPressedTime = System.currentTimeMillis()
            }
        }

        // Initialize drawerLayout and navigationView
        drawerLayout = requireActivity().findViewById(R.id.drawer_layout)
        navigationView = requireActivity().findViewById(R.id.navigation_view)

        //set the menu item "create Event" to gone
        val menu = navigationView.menu

        // Hide the createEvent menu item if userRole != "admin"
        val createEventItem = menu.findItem(R.id.eventsCreationFragment)
        createEventItem.isVisible = false

        // Hide the flaggedPosts menu item if userRole != "moderator"
        val flaggedPostsItem = menu.findItem(R.id.flaggedPostsFragment)
        flaggedPostsItem.isVisible = false

        // Check user role and set menu visibility
        checkUserRole()

        // Initialize userSessionDataStore
        userSessionDataStore = UserSessionDataStore(requireContext())

        // Open the drawer when the sidebar icon is clicked
        binding.sideBar.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        //when an Item in the drawer is clicked
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.exploreFragment -> {
                    // Navigate to Explore page
                    findNavController().navigate(R.id.action_homeFragment_to_exploreFragment)
                    true
                }
                R.id.eventsFragment -> {
                    // Navigate to Events page
                    findNavController().navigate(R.id.action_homeFragment_to_eventsFragment)
                    true
                }
                R.id.eventsCreationFragment -> {
                    // Navigate to Events Creation page
                    findNavController().navigate(R.id.action_homeFragment_to_eventsUploadFragment)
                    true
                } R.id.flaggedPostsFragment -> {
                // Navigate to Flagged Posts page
                findNavController().navigate(R.id.action_homeFragment_to_flaggedPostsFragment)
                true
            }
                else -> false
            }
            // Close drawer after item click
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }


    }

    private fun checkFollowings() {
        val followingRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                .child("Following")

        followingRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(pO: DataSnapshot) {
                if (pO.exists()) {
                    (followingUsersList as ArrayList<String>).clear()
                    for (snapshot in pO.children) {
                        snapshot.key?.let { (followingUsersList as ArrayList<String>).add(it) }
                    }
                    //get Posts from people you follow.
                    retrievePosts()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeFragment", "Error checking followings: ${error.message}")
            }
        })

    }

    private fun retrievePosts () {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        // Show ProgressBar initially
        binding.timelineProgressBar.visibility =View.VISIBLE
        binding.homePageScrollview.visibility = View.GONE

        postsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                postList.clear()
                var postsLoaded= 0

                for (snapshot in dataSnapshot.children) {
                    val post = snapshot.getValue(PostsModel::class.java)

                    for (id in followingUsersList as ArrayList<String>) {
                        if (post?.senderId?.equals(id) == true) {
                            postList.add(post)

                            postsLoaded++

                            if (postsLoaded == postList.size) {
                                postAdapter.notifyDataSetChanged()
                                // Hide ProgressBar and show ScrollView
                                binding.timelineProgressBar.visibility = View.GONE
                                binding.homePageScrollview.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            Log.e("HomeFragment", "Error retrieving posts: ${error.message}")
            }
        })

    }

    private fun checkUserRole() {
        // get userId
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId.toString())

        userRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userType = snapshot.child("type").getValue(String::class.java)
                    if (userType == "admin") {
                        // Show the createEvent menu item
                        val menu = navigationView.menu
                        val createEventItem = menu.findItem(R.id.eventsCreationFragment)
                        createEventItem.isVisible = true
                    } else if (userType == "moderator") {
                        val menu = navigationView.menu
                        val flaggedPostsItem = menu.findItem(R.id.flaggedPostsFragment)
                        flaggedPostsItem.isVisible = true
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeFragment", "Error checking user role: ${error.message}")
            }
        })
    }

}