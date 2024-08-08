package com.example.project_phairu.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.widget.AppCompatButton
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.project_phairu.Adapter.EventsAdapter
import com.example.project_phairu.Model.EventsModel
import com.example.project_phairu.R
import com.example.project_phairu.databinding.FragmentEventDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventDetailsFragment : Fragment() {

    // Binding
    private lateinit var binding: FragmentEventDetailsBinding

    //navController
    private lateinit var navController: NavController

    //postId
    private var eventId: String? = null

    //firebaseUser
    private lateinit var firebaseUser: FirebaseUser

    //Events and eventsAdapter
    var eventsList: MutableList<EventsModel>? = null
    private lateinit var eventsAdapter: EventsAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentEventDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //eventId
        eventId = arguments?.getString("eventId")

        Log.d("CommentsActivity", "Received eventId: $eventId")

        // Initialize navController
        navController = findNavController()

        if (eventId == null) {
            Log.d("EventDetailsFragment", "Null check failed: eventId is null")
            Toast.makeText(requireContext(), "Error: Event is missing", Toast.LENGTH_SHORT).show()
            return
        }

        // Get event info
        getEventInfo()

        // Check RSVP status
        isRSVPed(eventId.toString(), binding.planToAttend)
        // Get RSVP count
        getRSVPCount(eventId.toString(), binding.attendiesCount)

        //Initialize firebaseUser
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        // attend Button
        binding.planToAttend.setOnClickListener {
            val rsvpRef = FirebaseDatabase.getInstance().reference.child("RSVP").child(eventId.toString())

            if (binding.planToAttend.tag == "RSVP") {
                rsvpRef.child(firebaseUser.uid).setValue(true)
                    .addOnSuccessListener {
                        binding.planToAttend.setBackgroundResource(R.drawable.buttonbackground3)
                        binding.planToAttend.text = "Planning to attend."
                        binding.planToAttend.tag = "RSVPed"
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error RSVPing to event: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                rsvpRef.child(firebaseUser.uid).removeValue()
                    .addOnSuccessListener {
                        binding.planToAttend.setBackgroundResource(R.drawable.buttonbackground2)
                        binding.planToAttend.text = "Plan to attend?"
                        binding.planToAttend.tag = "RSVP"
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error removing RSVP from event: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
          }


       //find the backIcon
        binding.backBtn.setOnClickListener {
            //navigate back
            findNavController().popBackStack()
        }

    }

    private fun getEventInfo() {

        val getEventRef = FirebaseDatabase.getInstance().reference.child("Events").child(eventId.toString())

        getEventRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val event = snapshot.getValue(EventsModel::class.java)

                    if (event != null) {
                        binding.eventTitle.text = event.eventName
                        binding.eventLocation.text = event.eventLocation
                        binding.eventStartTime.text = event.eventStartTime
                        binding.eventEndTime.text = event.eventEndTime
                        binding.eventDescription.text = event.eventDescription


                        // Extract the day and month from the event date
                        val dateString = event.eventDate
                        if (dateString != null) {
                            val parts = dateString.split("/")
                            if (parts.size == 3) {
                                val day = parts[0]
                                val month = parts[1].toIntOrNull() // Convert month to Int
                                binding.eventDay.text = day
                                binding.eventMonth.text = if (month != null) {
                                    SimpleDateFormat("MMM", Locale.getDefault()).format(Date(0, month - 1, 0))
                                } else {
                                    ""
                                }
                            }
                        }

                        Picasso.get().load(event.eventPicture).placeholder(R.drawable.image_holder).into(binding.eventsPoster)


                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
               Log.e("EventDetailsFragment", "Error getting event info: ${error.message}")
                Toast.makeText(requireContext(), "Error getting event info: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun isRSVPed(eventId: String, rsvpButton: AppCompatButton) {
        //get current user
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val rsvpRef = FirebaseDatabase.getInstance().reference.child("RSVP").child(eventId)

        rsvpRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(firebaseUser?.uid.toString()).exists()) {
                    rsvpButton.setBackgroundResource(R.drawable.buttonbackground3)
                    rsvpButton.text = "Planning to attend."
                    rsvpButton.tag = "RSVPed"
                } else {
                    rsvpButton.setBackgroundResource(R.drawable.buttonbackground2)
                    rsvpButton.text = "Plan to attend?"
                    rsvpButton.tag = "RSVP"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("EventDetailsFragment", "Error checking if event plans: ${error.message}")
            }
        })
    }

    private fun getRSVPCount(eventId: String, rsvpCountTextView: TextView) {

        val rsvpRef = FirebaseDatabase.getInstance().reference.child("RSVP").child(eventId)

        rsvpRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    rsvpCountTextView.text = snapshot.childrenCount.toString()
                } else {
                    rsvpCountTextView.text = "0"
                }
            }

            override fun onCancelled(error: DatabaseError) {
               Log.e("EventDetailsFragment", "Error getting RSVP count: ${error.message}")
            }
        })

    }

}