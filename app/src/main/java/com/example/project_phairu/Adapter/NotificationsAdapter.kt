package com.example.project_phairu.Adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.project_phairu.CommentsActivity
import com.example.project_phairu.Fragments.NotificationsFragmentDirections
import com.example.project_phairu.Model.NotificationsModel
import com.example.project_phairu.Model.UserModel
import com.example.project_phairu.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class NotificationsAdapter (private var context: Context, private var notifications: MutableList<NotificationsModel>)
    : RecyclerView.Adapter<NotificationsAdapter.ViewHolder>() {

        class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
            //Initialize views
            var profileName: TextView = itemView.findViewById(R.id.ProfileName_Notifications)
            var username: TextView = itemView.findViewById(R.id.username_Notifications)
            var Notificationtext: TextView = itemView.findViewById(R.id.Text_Notifications)
            var profilePic: CircleImageView = itemView.findViewById(R.id.notificationProfilePic)
            var timestamp : TextView = itemView.findViewById(R.id.notificationTime_Notifications)

        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.notification_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
      return notifications.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val notification = notifications[position]
        val userId = notification.userId

        //get the user data for the current post(profilePic, name, username)
        userInfo(holder.profilePic, holder.profileName, holder.username, userId)

       if (notification.notificationType == "like") {
           holder.Notificationtext.text = "liked your post"
       } else if (notification.notificationType == "comment") {
           holder.Notificationtext.text = "commented on your post"
       } else if (notification.notificationType == "follow") {
           holder.Notificationtext.text = "started following you"
       } else{
           holder.Notificationtext.text = notification.notificationMessage
       }

        //get the Notification time and date and state the passed time
        val relativeTime = getRelativeTime(notification.notificationTimestamp)
        holder.timestamp.text = relativeTime

        holder.itemView.setOnClickListener {
            if (notification.notificationType == "like") {
                //navigate to the post
                val intent = Intent(context, CommentsActivity::class.java)
                intent.putExtra("postId", notification.postId)
                intent.putExtra("senderId", userId)
                context.startActivity(intent)
            } else if (notification.notificationType == "comment") {
                //navigate to the post
                val intent = Intent(context, CommentsActivity::class.java)
                intent.putExtra("postId", notification.postId)
                intent.putExtra("senderId", userId)
                context.startActivity(intent)
            } else if (notification.notificationType == "follow") {
                // Navigate to the profile page of the user who started following using NavController
                val navController = Navigation.findNavController(holder.itemView)

                val profileId = userId

                if (profileId != null) {
                    val action = NotificationsFragmentDirections.actionNotificationsFragmentToProfileFragment(profileId)
                    navController.navigate(action)
                } else {
                    // Handle the case where user.id is null (e.g., show an error)
                    Log.e("NotificationsAdapter", "User ID is null, cannot navigate to profile")
                }

            }
        }

        holder.profilePic.setOnClickListener{
            val navController = Navigation.findNavController(holder.itemView)
            val profileId = userId
            if (profileId != null) {
                val action = NotificationsFragmentDirections.actionNotificationsFragmentToProfileFragment(profileId)
                navController.navigate(action)
            } else {
                // Handle the case where user.id is null (e.g., show an error)
                Log.e("NotificationsAdapter", "User ID is null, cannot navigate to profile")
            }
        }

        holder.profileName.setOnClickListener{
            val navController = Navigation.findNavController(holder.itemView)
            val profileId = userId
            if (profileId != null) {
                val action = NotificationsFragmentDirections.actionNotificationsFragmentToProfileFragment(profileId)
                navController.navigate(action)
            } else {
                // Handle the case where user.id is null (e.g., show an error)
                Log.e("NotificationsAdapter", "User ID is null, cannot navigate to profile")
            }
        }

        holder.username.setOnClickListener{
            val navController = Navigation.findNavController(holder.itemView)
            val profileId = userId
            if (profileId != null) {
                val action = NotificationsFragmentDirections.actionNotificationsFragmentToProfileFragment(profileId)
                navController.navigate(action)
            } else {
                // Handle the case where user.id is null (e.g., show an error)
                Log.e("NotificationsAdapter", "User ID is null, cannot navigate to profile")
            }
        }

    }

    private fun userInfo(profilePic: CircleImageView, profileName: TextView, profileUsername: TextView, senderId: String?) {
        //get users node
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(senderId.toString())

        userRef.addValueEventListener(object : ValueEventListener{
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
                Log.e("PostAdapter", "Error getting user info: ${error.message}")
            }
        })
    }

    // Helper function to calculate and format relative time
    fun getRelativeTime(postTimestamp: String?): String {
        if (postTimestamp == null) return ""

        val currentTime = System.currentTimeMillis()
        val postTime = postTimestamp.toLongOrNull() ?: return ""
        val timeDiff = currentTime - postTime

        return when {
            timeDiff < 60000 -> ".${timeDiff / 1000} sec ago"
            timeDiff < 3600000 -> ".${timeDiff / 60000} min ago"
            timeDiff < 86400000 -> ".${timeDiff / 3600000} hr ago"
            else -> ".${timeDiff / 86400000} days ago"
        }
    }

}