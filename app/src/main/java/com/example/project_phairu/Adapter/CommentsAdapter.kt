package com.example.project_phairu.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_phairu.Model.CommentsModel
import com.example.project_phairu.Model.UserModel
import com.example.project_phairu.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class CommentsAdapter(private var context: Context, private var comments: MutableList<CommentsModel>) :
    RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {

    // Initialize FirebaseUser (get current user ID)
    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialize views
        var profileName: TextView = itemView.findViewById(R.id.commentProfileName)
        var profileUsername: TextView = itemView.findViewById(R.id.commentProfileUsername)
        var profilePic: CircleImageView = itemView.findViewById(R.id.commentUserProfilePic)
        var postImage: ShapeableImageView = itemView.findViewById(R.id.commentsPostImage)
        var postTime: TextView = itemView.findViewById(R.id.postTime)
        var imageHolder: LinearLayout = itemView.findViewById(R.id.bottomLayer)
        var postCaption: TextView = itemView.findViewById(R.id.commentsText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.comments_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
      return comments.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //get current user
         firebaseUser = FirebaseAuth.getInstance().currentUser

        // Bind data to views
        val comment = comments[position]

        //get the senderId
        val senderId = comment.senderId

        // get the user data for the current post(profilePic, name, username)
        userInfo(holder.profilePic, holder.profileName, holder.profileUsername, senderId)

        // Caption visibility
        if (comment.comment == "") {
            holder.postCaption.visibility = View.GONE
        } else {
            holder.postCaption.visibility = View.VISIBLE
            holder.postCaption.text = comment.comment
        }
    }


    private fun userInfo(profilePic: CircleImageView, profileName: TextView, profileUsername: TextView, postSenderId: String?) {
        //get users node
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(postSenderId.toString())

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(UserModel::class.java)

                    // Capitalize first and last names
                    val capitalizedFirstName = user?.firstname?.capitalize() ?: ""
                    val capitalizedLastName = user?.lastname?.capitalize() ?: ""

                    Picasso.get().load(user?.profilePicture)
                        .placeholder(R.drawable.profile_placeholder).into(profilePic)
                    profileName.text = ("$capitalizedFirstName $capitalizedLastName")
                    profileUsername.text = ("@" + user?.username)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CommentsAdapter", "Error getting user info: ${error.message}")
            }
        })
    }

}