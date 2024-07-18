package com.example.project_phairu.Fragments
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.project_phairu.Model.UserModel
import com.example.project_phairu.R
import com.example.project_phairu.databinding.FragmentProfileBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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

        profileId = arguments?.getString("profileId")

        if (profileId == firebaseUser.uid) {
            binding.editProfileBtn.text = "Edit Profile"
        } else if (profileId != firebaseUser.uid) {
            checkFollowAndFollowingButtonStatus()
        }

        //get followers
        getFollowers()

        //get following
        getFollowing()

        //get user info
        userInfo()

        // Initialize navController
        navController = findNavController()


        // Find the BottomNavigation
        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.nav_view)

        // Set up the BottomNavigationView with the NavController
        bottomNavigationView.setupWithNavController(navController)

        //find the EditProfile button
        binding.editProfileBtn.setOnClickListener {
            //navigate to edit profile activity
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

    }

    private fun checkFollowAndFollowingButtonStatus () {
        val followingRef = firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }

        if (followingRef != null) {
            followingRef.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child(profileId.toString()).exists()) {
                        binding.editProfileBtn.text = "Following"
                    } else {
                        binding.editProfileBtn.text = "Follow"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }

    private fun getFollowers() {
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
                    // Toast message
                }
            })
        } else {
            //hide followers count
            binding.followers.text = "-"
        }
    }

    private fun getFollowing() {
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
                    // Toast message
                }
            })
        } else {
            //hide following count
            binding.following.text = "-"
        }
    }

    private fun userInfo () {
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

                        Picasso.get().load(user?.profilePicture)
                            .placeholder(R.drawable.profile_placeholder).into(binding.profilePic)
                        binding.FnameLname.text = "$capitalizedFirstName $capitalizedLastName"
                        binding.username.text = user?.username
                        binding.textviewBio.text = user?.bio
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileFragment", "Error fetching data: ${error.message}")
                    // Toast message

                }
            })
        } else {
            // Handle the case where profileId is null
            Log.w("ProfileFragment", "Profile ID is null, cannot fetch user info")
        }
    }

}