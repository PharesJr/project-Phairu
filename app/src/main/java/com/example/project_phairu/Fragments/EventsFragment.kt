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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_phairu.Adapter.EventsAdapter
import com.example.project_phairu.Model.EventsModel
import com.example.project_phairu.R
import com.example.project_phairu.databinding.FragmentEventsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventsFragment : Fragment() {

    //binding
    private lateinit var binding: FragmentEventsBinding

    //navController
    private lateinit var navController: NavController

    //eventsAdapter and EventsModel
    private lateinit var eventsAdapter: EventsAdapter
    private lateinit var eventsList: MutableList<EventsModel>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView? = binding.root.findViewById(R.id.events_recyclerview)
        val linearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        linearLayoutManager.reverseLayout = false
        linearLayoutManager.stackFromEnd = false
        if (recyclerView != null) {
            recyclerView.layoutManager = linearLayoutManager
        }

        // Initialize navController
        navController = findNavController()

        // Initialize the eventsList and eventsAdapter
        eventsList = mutableListOf()
        eventsAdapter = EventsAdapter(requireContext(), eventsList)
        recyclerView?.adapter = eventsAdapter

        //retrieve events
        retrieveEvents()

        //find the backIcon
        binding.backBtn.setOnClickListener {
            //navigate back
            findNavController().popBackStack()
        }

    }

    private fun retrieveEvents() {
        val eventsRef = FirebaseDatabase.getInstance().reference.child("Events")

        eventsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                eventsList.clear()
                for (eventSnapshot in snapshot.children) {
                    val event = eventSnapshot.getValue(EventsModel::class.java)
                    event?.let { eventsList.add(it) }

                    // Sort events by date
                    eventsList.sortBy { parseDate(it.eventDate!!) }


                    Log.d("EventsFragment", "Events List: $eventsList")
                    // Notify the adapter about data changes
                    eventsAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("EventsFragment", "Error retrieving events: ${error.message}")
                Toast.makeText(requireContext(), "Error retrieving events: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun parseDate(dateString: String): Date? {
        return try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dateFormat.parse(dateString)
        } catch (e: Exception) {
            Log.e("EventsFragment", "Error parsing date: ${e.message}")
            null
        }
    }
}
