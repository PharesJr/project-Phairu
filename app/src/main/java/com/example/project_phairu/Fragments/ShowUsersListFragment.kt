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
import com.example.project_phairu.Adapter.UserAdapter
import com.example.project_phairu.Model.UserModel
import com.example.project_phairu.R
import com.example.project_phairu.databinding.FragmentShowUsersListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShowUsersListFragment : Fragment() {

    //binding
    private lateinit var binding: FragmentShowUsersListBinding

    //Firebase
    private lateinit var firebaseUser: FirebaseUser

    //Nav Controller
    private lateinit var navController: NavController

    var id : String? = null
    var title : String? = null

    // Adapter
    var userAdapter: UserAdapter? = null
    var userList: MutableList<UserModel>? = null
    var idList: List<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentShowUsersListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val intent = requireActivity().intent
        id = arguments?.getString("id")
        title = arguments?.getString("title")

        // Initialize the navController
        navController = findNavController()

        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.title = title
        toolbar.setNavigationIcon(R.drawable.back_icon_32)
        toolbar.setNavigationOnClickListener {
            //Navigate back
            navController.popBackStack()
        }

        //set up RecyclerVIew
        binding.usersRecyclerView.setHasFixedSize(true)
        binding.usersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        userList = ArrayList()
        userAdapter = UserAdapter(requireContext(), userList as ArrayList<UserModel>, title.toString(), true)
        binding.usersRecyclerView.adapter = userAdapter

        idList = ArrayList()

        when(title)
        {
            "likes" -> getLikes()
            "following" -> getFollowing()
            "followers" -> getFollowers()
//            "views" -> getViews()
        }

    }

    private fun getLikes() {
        val likesRef = id?.let { FirebaseDatabase.getInstance().reference.child("Likes").child(it) }

        likesRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    (idList as ArrayList<String>).clear()

                    for (snap in snapshot.children) {
                        (idList as ArrayList<String>).add(snap.key!!)
                    }

                    showUsers()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PostAdapter", "Error getting likes count: ${error.message}")
            }
        })
    }

    private fun getFollowing() {
        val followingCountRef = id?.let {
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it)
                .child("Following")
        }


        followingCountRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    (idList as ArrayList<String>).clear()
                    for (snap in snapshot.children) {
                        (idList as ArrayList<String>).add(snap.key!!)
                    }
                    showUsers()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileFragment", "Error fetching data: ${error.message}")
                //                Toast.makeText(context, "Error loading profile data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getFollowers() {
        val followersCountRef = id?.let {
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it)
                .child("Followers")
        }


        followersCountRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    (idList as ArrayList<String>).clear()
                    for (snap in snapshot.children) {
                        (idList as ArrayList<String>).add(snap.key!!)
                    }
                    showUsers()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileFragment", "Error fetching data: ${error.message}")
                //                Toast.makeText(context, "Error loading profile data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showUsers() {
        //instantiate firebase
        val userRef = FirebaseDatabase.getInstance().getReference().child("Users")
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (userList as ArrayList<UserModel>).clear()

                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(UserModel::class.java)

                    for (id in idList!!) {
                        if (user?.id == id) {
                            (userList as ArrayList<UserModel>).add(user)
                        }
                    }
                }

                userAdapter?.notifyDataSetChanged()

            }


            override fun onCancelled(error: DatabaseError) {
                // Handle the error appropriately
                Log.e("ExploreFragment", "Error retrieving users: ${error.message}")
            }

        })
    }
}