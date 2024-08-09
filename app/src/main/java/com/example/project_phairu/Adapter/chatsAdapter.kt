package com.example.project_phairu.Adapter

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.project_phairu.Model.MessageModel
import com.example.project_phairu.Model.UserModel
import com.example.project_phairu.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class chatsAdapter (private var context: Context, private var chats : MutableList<MessageModel>) : RecyclerView.Adapter<chatsAdapter.ViewHolder>() {
    class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profilePic: CircleImageView = itemView.findViewById(R.id.ProfilePic)
        var profileName: TextView = itemView.findViewById(R.id.ProfileName)
        var latestMessage: TextView = itemView.findViewById(R.id.latestMessage)
        var latestMessageTime: TextView = itemView.findViewById(R.id.latestMessageTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.messages_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return chats.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        // Bind data to views
        val chat = chats[position]

        // get the user Info (profilePic, name)
        userInfo(holder.profilePic, holder.profileName, chat.receiverId)

        // get the last message
        holder.latestMessage.text = chat.message

        val relativeTime = getActualTime(chat.messageTimestamp)

        holder.latestMessageTime.text = relativeTime

        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("userId", chat.receiverId)
            Navigation.findNavController(holder.itemView).navigate(R.id.action_chatFragment_to_chatScreenFragment, bundle)
        }

    }

    private fun userInfo(profilePic: CircleImageView, profileName: TextView, senderId: String?) {
        //get users node
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(senderId.toString())

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(UserModel::class.java)

                    // Capitalize first and last names
                    val capitalizedFirstName = user?.firstname?.capitalize() ?: ""
                    val capitalizedLastName = user?.lastname?.capitalize() ?: ""

                    Picasso.get().load(user?.profilePicture)
                        .placeholder(R.drawable.profilepic_placeholder).into(profilePic)
                    profileName.text = ("$capitalizedFirstName $capitalizedLastName")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("chatsAdapter", "Error getting user info: ${error.message}")
            }
        })
    }

    // Helper function to get the time a message was sent
    fun getActualTime(messageTimestamp: String?): String {
        if (messageTimestamp == null) return ""

        val messageTime =messageTimestamp.toLongOrNull() ?: return ""
        val date = Date(messageTime)
        // Example format: 9:38 PM
        val format = SimpleDateFormat("h:mm a", Locale.getDefault())
        return format.format(date)
    }

}