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
import androidx.recyclerview.widget.RecyclerView
import com.example.project_phairu.Adapter.NotificationsAdapter
import com.example.project_phairu.Model.NotificationsModel
import com.example.project_phairu.R
import com.example.project_phairu.databinding.FragmentNotificationsBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificationsFragment : Fragment() {

    //binding
    private lateinit var binding: FragmentNotificationsBinding

    //instantiate adapter objects
    private var notificationsAdapter: NotificationsAdapter? = null
    private var notificationsList: MutableList<NotificationsModel>? = null

    //nav controller
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)

        // Initialize binding
        binding = FragmentNotificationsBinding.bind(view)

        //recyclerview
        val recyclerView: RecyclerView = binding.notificationsRecyclerview
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        notificationsList = ArrayList()
        notificationsAdapter =
            NotificationsAdapter(requireContext(), notificationsList!! as ArrayList<NotificationsModel>)
        recyclerView.adapter = notificationsAdapter

        // Show ProgressBar initially
        binding.notificationsPageLoader.visibility = View.VISIBLE
        binding.notificationsScrollview.visibility = View.GONE

        //read notifications
        readNotifications()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize navController
        navController = findNavController()


        // Find the BottomNavigation
        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.nav_view)

        // Set up the BottomNavigationView with the NavController
        bottomNavigationView.setupWithNavController(navController)

    }

    //Add Notifications
    private fun readNotifications() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val notificationsRef = FirebaseDatabase.getInstance().reference.child("Notifications").child(firebaseUser?.uid.toString())


        notificationsRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    notificationsList?.clear()

                    for (snap in snapshot.children) {
                        val notification = snap.getValue(NotificationsModel::class.java)

                        if (notification != null) {
                            // Check if NOT current user
                            if (firebaseUser != null) {
                                if (notification.userId != firebaseUser.uid) {
                                    (notificationsList as ArrayList<NotificationsModel>).add(notification)
                                    Log.d("NotificationsFragment", "Notification added: $notification")
                                }
                            }
                        }
                    }

                    notificationsAdapter?.notifyDataSetChanged()

                    binding.notificationsPageLoader.visibility = View.GONE
                    binding.notificationsScrollview.visibility = View.VISIBLE


                } else {
                    // No notifications found
                    binding.notificationsPageLoader.visibility = View.GONE
                    binding.notificationsScrollview.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "No notifications found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("NotificationsFragment", "Error fetching data: ${error.message}")
                binding.notificationsPageLoader.visibility = View.GONE
                binding.notificationsScrollview.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "Error fetching notifications", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
