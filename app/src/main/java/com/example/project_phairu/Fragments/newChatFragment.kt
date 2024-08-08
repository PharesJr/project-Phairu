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
import androidx.recyclerview.widget.RecyclerView
import com.example.project_phairu.Adapter.EventsAdapter
import com.example.project_phairu.Adapter.NewChatAdapter
import com.example.project_phairu.Model.UserModel
import com.example.project_phairu.R
import com.example.project_phairu.databinding.FragmentNewChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class newChatFragment : Fragment() {

    //binding
    private lateinit var binding: FragmentNewChatBinding

    //Adapter and List
    private var newChatAdapter: NewChatAdapter? = null
    private var userList: MutableList<UserModel>? = null

    //Firebase
    private lateinit var firebaseUser: FirebaseUser

    //navController
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentNewChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val recyclerView: RecyclerView? = binding.root.findViewById(R.id.newChatRecyclerView)
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        if (recyclerView != null) {
            recyclerView.layoutManager = linearLayoutManager
        }

        // Initialize navController
        navController = findNavController()

        // Initialize the eventsList and eventsAdapter
        userList = mutableListOf()
        newChatAdapter = NewChatAdapter(requireContext(), userList!!)
        recyclerView?.adapter = newChatAdapter

        //get followers
        retrieveUsers()

        //find the backIcon
        binding.backBtn.setOnClickListener {
            //navigate back
            findNavController().popBackStack()
        }
    }

    private fun retrieveUsers() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users")

        usersRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                userList?.clear()

                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(UserModel::class.java)

                    if (user != null) {
                        userList?.add(user)

                        Log.d("NewChatFragment", "User List: $userList")
                        // Notify the adapter about data changes
                        newChatAdapter?.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
               Log.e("NewChatFragment", "Error retrieving users: ${error.message}")
            }
        })
    }
}