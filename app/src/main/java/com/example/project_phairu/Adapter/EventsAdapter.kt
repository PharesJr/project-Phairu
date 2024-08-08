package com.example.project_phairu.Adapter

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.project_phairu.Model.EventsModel
import com.example.project_phairu.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventsAdapter(private var context: Context, private var events : MutableList<EventsModel>) : RecyclerView.Adapter<EventsAdapter.ViewHolder>() {

    class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        var date: TextView = itemView.findViewById(R.id.eventDay)
        var month: TextView = itemView.findViewById(R.id.eventMonth)
        var poster: ShapeableImageView = itemView.findViewById(R.id.eventsPoster)
        var title: TextView = itemView.findViewById(R.id.eventTitle)
        var location: TextView = itemView.findViewById(R.id.eventLocation)
        var startTime: TextView = itemView.findViewById(R.id.eventStartTime)
        var endTime: TextView = itemView.findViewById(R.id.eventEndTime)
        var viewButton: AppCompatButton = itemView.findViewById(R.id.viewEventDetailsBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.event_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Bind data to views
        val event = events[position]

        // get the eventId
        val eventId = event.eventId.toString()

        // Get event info
        getEventInfo(holder.date, holder.month, holder.poster, holder.title, holder.location, holder.startTime, holder.endTime, eventId)

        holder.viewButton.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("eventId", eventId)
            //Navigate to event details fragment
            val navController = holder.itemView.findNavController()
            navController.navigate(R.id.action_eventsFragment_to_eventDetailsFragment, bundle)
        }
    }

    private fun getEventInfo(eventDate: TextView, eventMonth: TextView, eventPoster: ShapeableImageView, eventTitle: TextView, eventLocation: TextView, eventStartTime: TextView, eventEndTime: TextView, eventId: String) {

        val eventRef = FirebaseDatabase.getInstance().reference.child("Events").child(eventId)

        eventRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val event = snapshot.getValue(EventsModel::class.java)

                    // Extract the day and month from the event date
                    val dateString = event?.eventDate
                    if (dateString != null) {
                        val parts = dateString.split("/")
                        if (parts.size == 3) {
                            val day = parts[0]
                            val month = parts[1].toIntOrNull() // Convert month to Int
                            eventDate.text = day
                            eventMonth.text = if (month != null) {
                                SimpleDateFormat("MMM", Locale.getDefault()).format(Date(0, month - 1, 0))
                            } else {
                                ""
                            }
                        }
                    }
                    eventTitle.text = event?.eventName?.capitalize() ?: ""
                    eventLocation.text = event?.eventLocation?.capitalize() ?: ""
                    eventStartTime.text = event?.eventStartTime
                    eventEndTime.text = event?.eventEndTime

                    Picasso.get().load(event?.eventPicture).placeholder(R.drawable.image_holder).into(eventPoster)
                }
            }

            override fun onCancelled(error: DatabaseError) {
              Log.e("EventsAdapter", "Error getting event info: ${error.message}")
            }
        })
    }
}