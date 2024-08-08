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
import com.example.project_phairu.Model.UserModel
import com.example.project_phairu.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class NewChatAdapter (private var context: Context, private var users: MutableList<UserModel>) : RecyclerView.Adapter<NewChatAdapter.ViewHolder>() {

    class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.ProfileName)
        var username: TextView = itemView.findViewById(R.id.ProfileUsername)
        var profilePic: CircleImageView = itemView.findViewById(R.id.ProfilePic)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.new_chat_user_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        //bind data to views
        val user = users[position]

        // get the userId
        val userId = user.id

        // Get user info
        getUsers(holder.name, holder.username, holder.profilePic, userId.toString())

        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("userId", userId)
            Navigation.findNavController(holder.itemView).navigate(R.id.action_newChatFragment_to_chatScreenFragment, bundle)
        }


    }

    private fun getUsers (name: TextView, username: TextView, profilePic: CircleImageView, userId: String) {

        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)

        usersRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
               if (snapshot.exists()) {

                   val user = snapshot.getValue(UserModel::class.java)

                   // Capitalize first and last names
                   val capitalizedFirstName = user?.firstname?.capitalize() ?: ""
                   val capitalizedLastName = user?.lastname?.capitalize() ?: ""
                   name.text = "$capitalizedFirstName $capitalizedLastName"
                   username.text = "@" + user?.username
                   Picasso.get().load(user?.profilePicture).placeholder(R.drawable.profilepic_placeholder).into(profilePic)
               }
            }

            override fun onCancelled(error: DatabaseError) {
               Log.e("NewChatAdapter", "Error getting user info: ${error.message}")
            }
        })
    }



}