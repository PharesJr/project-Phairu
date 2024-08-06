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
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_phairu.Adapter.UserAdapter
import com.example.project_phairu.Model.UserModel
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

    //navController
    private lateinit var navController: NavController


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
        userAdapter = requireContext().let { UserAdapter(it, users!! as ArrayList<UserModel>, "search", true) }
        recyclerView?.adapter = userAdapter

        binding.searchUsers.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int){
                // Not used in this case
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val newText = s?.toString()
                if (newText.isNullOrEmpty()) {
                    // Hide progress bar
                    binding.progressBar2.visibility = View.GONE

//                    // Show all users when search is empty
//                    retrieveUsers()
                } else {

                    //show results textview
                    binding.resultsTxt.visibility = View.VISIBLE

                    // Show progress bar
                    binding.progressBar2.visibility = View.VISIBLE

                    //show the recyclerview
                    binding.exploreRecyclerview.visibility = View.VISIBLE

                    //search
                    searchUser(newText.lowercase().trim())
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Not used in this case
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize navController
        navController = findNavController()


        //find the backIcon
        binding.backBtn.setOnClickListener {
            //navigate back
            findNavController().popBackStack()
        }

        // Load initial list of users
        retrieveUsers()

    }

    private fun searchUser(input: String) {

        // Counter for completed queries
        var queriesCompleted = 0

        // Split input into words
        val searchTerms = input.toLowerCase().split(" ")
        val filteredUsers = mutableListOf<UserModel>()

        val query = FirebaseDatabase.getInstance().getReference()
            .child("Users")

        for (term in searchTerms) {
            query.orderByChild("firstname")
                .startAt(term)
                .endAt(term + "\uf8ff")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d("ExploreFragment - Firstname", "Search results count: ${snapshot.childrenCount}")
                        for (userSnapshot in snapshot.children) {
                            val user = userSnapshot.getValue(UserModel::class.java)

                            // Avoid duplicates
                            if (user != null && !filteredUsers.contains(user)) {
                                filteredUsers.add(user)
                            }}

                        queriesCompleted++
                        updateAdapterIfAllQueriesCompleted(queriesCompleted, filteredUsers)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ExploreFragment", "Error retrieving users: ${error.message}")
                        Toast.makeText(requireContext(), "Error retrieving users", Toast.LENGTH_SHORT).show()
                        binding.progressBar2.visibility = View.GONE
                    }
                })

            query.orderByChild("lastname")
                .startAt(term)
                .endAt(term + "\uf8ff")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d("ExploreFragment - Lastname", "Search results count: ${snapshot.childrenCount}")
                        for (userSnapshot in snapshot.children) {
                            val user = userSnapshot.getValue(UserModel::class.java)

                            // Avoid duplicates
                            if (user != null && !filteredUsers.contains(user)) {
                                filteredUsers.add(user)
                            }}
                        queriesCompleted++
                        updateAdapterIfAllQueriesCompleted(queriesCompleted, filteredUsers)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ExploreFragment", "Error retrieving users: ${error.message}")
                        Toast.makeText(requireContext(), "Error retrieving users", Toast.LENGTH_SHORT).show()
                        binding.progressBar2.visibility = View.GONE
                    }
                })

            query.orderByChild("username")
                .startAt(term)
                .endAt(term + "\uf8ff")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d("ExploreFragment - Username", "Search results count: ${snapshot.childrenCount}")
                        for (userSnapshot in snapshot.children) {
                            val user = userSnapshot.getValue(UserModel::class.java)

                            // Avoid duplicates
                            if (user != null && !filteredUsers.contains(user)) {
                                filteredUsers.add(user)
                            }}
                        queriesCompleted++
                        updateAdapterIfAllQueriesCompleted(queriesCompleted, filteredUsers)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ExploreFragment", "Error retrieving users: ${error.message}")
                        Toast.makeText(requireContext(), "Error retrieving users", Toast.LENGTH_SHORT).show()
                        binding.progressBar2.visibility = View.GONE
                    }
                })
        }

    }

    private fun updateAdapterIfAllQueriesCompleted(queriesCompleted: Int, filteredUsers: MutableList<UserModel>) {
        if (queriesCompleted == 3) { // 3 queries in total
            userAdapter?.updateUsers(filteredUsers)
            binding.progressBar2.visibility = View.GONE
        }
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