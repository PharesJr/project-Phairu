package com.example.project_phairu.Fragments
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_phairu.Adapter.PostAdapter
import com.example.project_phairu.BookmarksActivity
import com.example.project_phairu.DataStore.UserSessionDataStore
import com.example.project_phairu.Model.NotificationsModel
import com.example.project_phairu.Model.PostsModel
import com.example.project_phairu.Model.UserModel
import com.example.project_phairu.R
import com.example.project_phairu.ShowUsersActivity
import com.example.project_phairu.databinding.FragmentProfileBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    //binding
    private lateinit var binding: FragmentProfileBinding

    //navController
    private lateinit var navController: NavController

    //firebase
    private lateinit var firebaseUser: FirebaseUser

    //profileID
    private var profileId: String? = null

    //users Posts, Bookmarks and postAdapter
    var postsList: List<PostsModel> = emptyList()
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Show progress bar initially
        binding.profilePageLoader.visibility = View.VISIBLE
        binding.profileScrollView.visibility = View.GONE


        //firebase User
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        //profileId from the exploreFragment
        profileId = arguments?.getString("profileId")

        //Determine if it's the current user's profile
        val isCurrentUser = profileId == null || profileId == firebaseUser.uid

        // Set visibility of Post Menu button
        binding.profileMenu.visibility = if (isCurrentUser) View.VISIBLE else View.GONE

        if (isCurrentUser) {
            //profileId is set for current user
            profileId = firebaseUser.uid
            binding.editProfileBtn.text = "Edit Profile"
        } else if (profileId != firebaseUser.uid) {
            checkFollowAndFollowingButtonStatus()
        }

        //Instantiate the user own Posts Recyclerview
        val recyclerView = binding.userPostsRecyclerview
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)

        navController = findNavController()

        // Initialize the postList and postAdapter
        postsList = mutableListOf()
        postAdapter = PostAdapter(requireContext(), postsList as MutableList<PostsModel>, "profile")
        recyclerView.adapter = postAdapter


        var dataFetchCounter = 0

        // Number of data fetches (userInfo, followers, following)
        val totalDataFetches = 3

        val onDataFetchComplete = {
            dataFetchCounter++
            if (dataFetchCounter == totalDataFetches) {
                // Hide progress bar and show ScrollView
                binding.profilePageLoader.visibility = View.GONE
                binding.profileScrollView.visibility = View.VISIBLE
            }
        }

        //get user info
        userInfo(onDataFetchComplete)

        // Fetch followers
        getFollowers(onDataFetchComplete)

        // Fetch following
        getFollowing(onDataFetchComplete)


        //show users posts
        myPosts()


        // Find the BottomNavigation
        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.nav_view)

        // Set up the BottomNavigationView with the NavController
        bottomNavigationView.setupWithNavController(navController)

        //find the following Layout
        binding.followingLayout.setOnClickListener{
            val intent = Intent(requireContext(), ShowUsersActivity::class.java)
            intent.putExtra("id", profileId)
            intent.putExtra("title", "following")
            Log.d("ProfileFragment", "Starting ShowUsersActivity with title: following and id: $profileId")
            startActivity(intent)
        }

        //find the followers Layout
        binding.followersLayout.setOnClickListener{
            val intent = Intent(requireContext(), ShowUsersActivity::class.java)
            intent.putExtra("id", profileId)
            intent.putExtra("title", "followers")
            startActivity(intent)
        }

        //find the EditProfile button
        binding.editProfileBtn.setOnClickListener {

            val getButtonText = binding.editProfileBtn.text.toString()

            when{
                // if the button text is "Edit Profile" ----> navigate to EditProfileFragment
                getButtonText == "Edit Profile" -> navController.navigate(R.id.action_profileFragment_to_editProfileFragment)

                //if the button text is "Follow" ----> add a new follower
                getButtonText == "Follow" -> {

                    //Database/Follow/currentUserId/Following/profileId(other User)
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(firebaseUser.uid)
                        .child("Following").child(profileId.toString()).setValue(true)

                    //Database/Follow/profileId(other User)/Followers/currentUserId
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(profileId.toString())
                        .child("Followers").child(firebaseUser.uid).setValue(true)

                    addNotification()
                }

                //if the button text is "Following" ----> remove the user from your following
                getButtonText == "Following" -> {

                    //Database/Follow/currentUserId/Following/profileId(other User)
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(firebaseUser.uid)
                        .child("Following").child(profileId.toString()).removeValue()


                    //Database/Follow/profileId(other User)/Followers/currentUserId
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(profileId.toString())
                        .child("Followers").child(firebaseUser.uid).removeValue()
                }
            }


        }

        // Post Menu button
        binding.profileMenu.setOnClickListener {
            val popupMenu = PopupMenu(context, binding.profileMenu)
            popupMenu.inflate(R.menu.profile_menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.bookmarks -> {
                        val intent = Intent(requireContext(), BookmarksActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    R.id.logout -> {

                        //initialize a dialog builder
                        val builder = AlertDialog.Builder(requireContext())
                        val view = layoutInflater.inflate(R.layout.dialog_logout, null)

                        builder.setView(view)
                        val dialog = builder.create()

                        view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                            dialog.dismiss()
                        }

                        view.findViewById<Button>(R.id.btnLogout).setOnClickListener {
                            lifecycleScope.launch {
                                // Sign out from Firebase
                                FirebaseAuth.getInstance().signOut()

                                // Clear user session data
                                val userSessionDataStore = UserSessionDataStore(requireContext())
                                userSessionDataStore.clearUserSession()

                                // Show Toast message
                                Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()


                                //navigate back to login page
                                navController.navigate(R.id.action_profileFragment_to_loginFragment)

                                dialog.dismiss()
                            }
                        }
                        dialog.show()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

    }

    private fun checkFollowAndFollowingButtonStatus () {
        val followingRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(firebaseUser.uid)
            .child("Following")


        followingRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(profileId.toString()).exists()) {
                    binding.editProfileBtn.text = "Following"
                } else {
                    binding.editProfileBtn.text = "Follow"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileFragment", "Error fetching data: ${error.message}")
                Toast.makeText(context, "Error loading profile data", Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun getFollowers(onDataFetchComplete: () -> Unit)  {
        if (profileId != null) {
            val followersCountRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId.toString())
                .child("Followers")


            followersCountRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        binding.followers.text = snapshot.childrenCount.toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileFragment", "Error fetching data: ${error.message}")
                    Toast.makeText(context, "Error loading profile data", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            //hide followers count
            binding.followers.text = "-"
        }

        // Notify when followers count is fetched
        onDataFetchComplete()
    }

    private fun getFollowing(onDataFetchComplete: () -> Unit)  {
        if (profileId != null) {
            val followingCountRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId.toString())
                .child("Following")


            followingCountRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        binding.following.text = snapshot.childrenCount.toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileFragment", "Error fetching data: ${error.message}")
                    Toast.makeText(context, "Error loading profile data", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            //hide following count
            binding.following.text = "-"
        }

        // Notify when following count is fetched
        onDataFetchComplete()
    }

    private fun myPosts() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
            .orderByChild("senderId").equalTo(profileId)

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (postsList as MutableList<PostsModel>).clear()
                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(PostsModel::class.java)
                    if (post != null) {
                        (postsList as MutableList<PostsModel>).add(post)
                    }
                }
                postAdapter.notifyDataSetChanged()
            }override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileFragment", "Error fetching data: ${error.message}")
                Toast.makeText(context, "Error loading profile data", Toast.LENGTH_SHORT).show()
            }
        })
    }



    private fun userInfo (onUserInfoFetched: () -> Unit)  {

        // Flag to track image loading
        var isImageLoaded = false

        if (profileId != null) {
            val userRef =
                FirebaseDatabase.getInstance().getReference().child("Users").child(profileId.toString())

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {
                        val user = snapshot.getValue(UserModel::class.java)

                        // Capitalize first and last names
                        val capitalizedFirstName = user?.firstname?.capitalize() ?: ""
                        val capitalizedLastName = user?.lastname?.capitalize() ?: ""

                        binding.FnameLname.text = "$capitalizedFirstName $capitalizedLastName"
                        binding.username.text = "@" +user?.username
                        binding.textviewBio.text = user?.bio

                        Picasso.get().load(user?.profilePicture)
                            .placeholder(R.drawable.profilepic_placeholder)
                            .into(binding.profilePic, object : com.squareup.picasso.Callback {
                                override fun onSuccess() {
                                    isImageLoaded = true
                                    if (isImageLoaded) { // Check if image is loaded
                                        onUserInfoFetched()
                                    }
                                }
                                override fun onError(e: Exception?) {
                                    Log.e("ProfileFragment", "Error loading profile picture: ${e?.message}")


                                    // Even if error, notify to avoid blocking UI
                                    onUserInfoFetched()
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileFragment", "Error fetching data: ${error.message}")
                    Toast.makeText(context, "Error loading profile data", Toast.LENGTH_SHORT).show()

                }
            })
        } else {
            // Handle the case where profileId is null
            Log.w("ProfileFragment", "Profile ID is null, cannot fetch user info")

            // Notify even if profileId is null
            onUserInfoFetched()
        }
    }

    //Add Notifications
    private fun addNotification() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val notificationsRef = FirebaseDatabase.getInstance().reference.child("Notifications").child(profileId.toString())

        val notification = NotificationsModel(
            userId = firebaseUser!!.uid,
            postId = null,
            notificationType = "follow",
            notificationMessage = "started following you"
        )

        notificationsRef.push().setValue(notification)
    }

}