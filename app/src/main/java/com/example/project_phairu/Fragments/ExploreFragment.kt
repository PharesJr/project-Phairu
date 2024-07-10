package com.example.project_phairu.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_phairu.Adapter.UserAdapter
import com.example.project_phairu.MainActivity
import com.example.project_phairu.Model.UserModel
import com.example.project_phairu.R
import com.example.project_phairu.databinding.FragmentExploreBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ExploreFragment : Fragment() {

    //binding
    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    //instantiate adapter objects
    private var recyclerView: RecyclerView? = null
    private var userAdapter: UserAdapter? = null
    private var users: MutableList<UserModel>? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentExploreBinding.inflate(inflater, container, false)

        //recyclerview
        recyclerView = binding.exploreRecyclerview
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)

        users = ArrayList()
        userAdapter = requireContext().let { UserAdapter(it, users!! as ArrayList<UserModel>, true) }
        recyclerView?.adapter = userAdapter

        binding.searchUsers.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    // Hide progress bar
                    binding.progressBar2.visibility = View.GONE

                    // Show all users when search is empty
                    retrieveUsers()
                } else {
                    // Show progress bar
                    binding.progressBar2.visibility = View.VISIBLE

                    //show the recyclerview
                    binding.exploreRecyclerview.visibility = View.VISIBLE

                    //search
                    searchUser(newText.lowercase().trim())
                }
                return true
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //remove the navigation bar
        (activity as? MainActivity)?.hideBottomNavigation()

        // Load initial list of users
        retrieveUsers()

    }

    private fun searchUser(input: String) {
        val query = FirebaseDatabase.getInstance().getReference()
            .child("Users")
            .orderByChild("username")
            .startAt(input)
            .endAt(input + "\uf8ff")


        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                    users?.clear()

                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(UserModel::class.java)
                        if (user != null) {
                            users?.add(user)
                        }
                    }

                    userAdapter?.notifyDataSetChanged()

                // Hide progress bar after results
                binding.progressBar2.visibility = View.GONE


            }


            override fun onCancelled(error: DatabaseError) {
                // Handle the error appropriately, e.g., log it or show a message
                Log.e("ExploreFragment", "Error retrieving users: ${error.message}")

                //Toast to the user
                Toast.makeText(requireContext(), "Error retrieving users", Toast.LENGTH_SHORT).show()

                // Hide progress bar on error
                binding.progressBar2.visibility = View.GONE
            }

        })

    }

    private fun retrieveUsers() {
        //instantiate firebase
        val userRef = FirebaseDatabase.getInstance().getReference().child("Users")
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                users?.clear()

                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(UserModel::class.java)
                    if (user != null) {
                        users?.add(user)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }


}