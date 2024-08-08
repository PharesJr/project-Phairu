package com.example.project_phairu.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_phairu.Model.FlaggedContentModel
import com.example.project_phairu.Model.PostsModel
import com.example.project_phairu.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FlaggedPostsAdapter (private val context: Context, private val flaggedContent: MutableList<FlaggedContentModel>) :
    RecyclerView.Adapter<FlaggedPostsAdapter.ViewHolder>() {

    class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialize views
        var flaggedReference: TextView = itemView.findViewById(R.id.flaggedRef)
        var time: TextView = itemView.findViewById(R.id.flaggedTime)
        var reason: TextView = itemView.findViewById(R.id.flaggedReason)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.flagged_content_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return flaggedContent.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        // Bind data to views
        val flaggedPost = flaggedContent[position]

        //get the postId
        val postId = flaggedPost.postId.toString()

        //get the Post time and date and state the passed time
        val relativeTime = getRelativeTime(flaggedPost.flaggedTimestamp)
        holder.time.text = relativeTime

        // Caption visibility
        if (flaggedPost.flaggedReason == "") {
            holder.reason.visibility = View.GONE
        } else {
            holder.reason.visibility = View.VISIBLE
            holder.reason.text = flaggedPost.flaggedReason
        }

        //get the flagged Reference
        holder.flaggedReference.text = flaggedPost.flaggedTimestamp

    }

    // Helper function to calculate and format relative time
    fun getRelativeTime(flaggedPostTimestamp: String?): String {
        if (flaggedPostTimestamp == null) return ""

        val currentTime = System.currentTimeMillis()
        val commentTime = flaggedPostTimestamp.toLongOrNull() ?: return ""
        val timeDiff = currentTime - commentTime

        return when {
            timeDiff < 60000 -> "${timeDiff / 1000} sec ago"
            timeDiff < 3600000 -> "${timeDiff / 60000} min ago"
            timeDiff < 86400000 -> "${timeDiff / 3600000} hr ago"
            else -> "${timeDiff / 86400000} days ago"
        }
    }
}