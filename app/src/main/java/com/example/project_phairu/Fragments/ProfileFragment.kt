package com.example.project_phairu.Fragments
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_phairu.Adapter.PostAdapter
import com.example.project_phairu.Model.PostsModel
import com.example.project_phairu.Model.UserModel
import com.example.project_phairu.R
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

class ProfileFragment : Fragment() {

    //binding
    private lateinit var binding: FragmentProfileBinding

    //navController
    private lateinit var navController: NavController

    //firebase
    private lateinit var firebaseUser: FirebaseUser

    //profileID
    private var profileId: String? = null

    //users Posts and postAdapter
    var postsList: List<PostsModel> = emptyList()
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //firebase User
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        //profileId from the exploreFragment
        profileId = arguments?.getString("profileId")

        //Determine if it's the current user's profile
        val isCurrentUser = profileId == null || profileId == firebaseUser.uid

        if (isCurrentUser) {
            //profileId is set for current user
            profileId = firebaseUser.uid
            binding.editProfileBtn.text = "Edit Profile"
        } else if (profileId != firebaseUser.uid) {
            checkFollowAndFollowingButtonStatus()
        }

        //Instantiate the Recyclerview
        val recyclerView = binding.userPostsRecyclerview
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)

        // Initialize the postList and postAdapter
        postsList = mutableListOf()
        postAdapter = requireContext().let { PostAdapter(it, postsList as MutableList<PostsModel>) }!!
        recyclerView?.adapter = postAdapter


        var dataFetchCounter = 0

        // Number of data fetches (userInfo, followers, following)
        val totalDataFetches = 3

        val onDataFetchComplete = {
            dataFetchCounter++
            if (dataFetchCounter == totalDataFetches) {

                // Hide progress bar
                binding.profilePageLoader.visibility = View.GONE

                //Show user profile
                binding.profileScrollView.visibility = View.VISIBLE
            }
        }

        //get user info
        userInfo(onDataFetchComplete)

        // Fetch followers
        getFollowers(onDataFetchComplete)

        // Fetch following
        getFollowing(onDataFetchComplete)




        // Initialize navController
        navController = findNavController()


        // Find the BottomNavigation
        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.nav_view)

        // Set up the BottomNavigationView with the NavController
        bottomNavigationView.setupWithNavController(navController)

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

        //show users posts
        myPosts()


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
                // Handle errors
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
                            .placeholder(R.drawable.profile_placeholder)
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

}