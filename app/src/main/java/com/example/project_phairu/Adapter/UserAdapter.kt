package com.example.project_phairu.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.project_phairu.Fragments.ExploreFragmentDirections
import com.example.project_phairu.Model.UserModel
import com.example.project_phairu.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Locale

class UserAdapter (private var context: Context,
                   private var users: MutableList<UserModel>,
    private var fragment: Boolean = false) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    // Initialize FirebaseUser (get specific user ID)
    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val view = LayoutInflater.from(context).inflate(R.layout.user_item_layout, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialize views
        var profileName: TextView = itemView.findViewById(R.id.ProfileName)
        var profileUsername: TextView = itemView.findViewById(R.id.ProfileUsername)
        var profilePic: CircleImageView = itemView.findViewById(R.id.ProfilePic)
        var followBtn: AppCompatButton = itemView.findViewById(R.id.followBtn)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        // Bind data to views
        val user = users[position]

        // Capitalize first letter of first and last names
        val capitalizedFirstName = user.firstname?.capitalize(Locale.ROOT)
        val capitalizedLastName = user.lastname?.capitalize(Locale.ROOT)
        holder.profileName.text = "$capitalizedFirstName $capitalizedLastName"
        holder.profileUsername.text = "@" + user.username
        Picasso.get().load(user.profilePicture).placeholder(R.drawable.profile_placeholder).into(holder.profilePic)

        //check if the current user is already following the searched user
        checkFollowingStatus(user.id, holder.followBtn)

        //OnClick Listener when a user Clicks on a profile when searching
        holder.itemView.setOnClickListener {
            val navController = Navigation.findNavController(holder.itemView)
            val profileId = user.id
            if (profileId != null) {
                val action = ExploreFragmentDirections.actionExploreFragmentToProfileFragment(
                    profileId
                )
                navController.navigate(action)
            } else {
                // Handle the case where user.id is null (e.g., show an error)
                Log.e("UserAdapter", "User ID is null, cannot navigate to profile")
            }
        }

        //follow button
        holder.followBtn.setOnClickListener {
            if(holder.followBtn.text.toString() == "Follow"){

                firebaseUser?.uid.let { it1 ->
                    // Check if user.id is not null
                    user.id?.let { userId ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(user.id)
                            .setValue(true).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.id)
                                        .child("Followers").child(it1.toString())
                                        .setValue(true).addOnCompleteListener { task ->
                                            if (task.isSuccessful) {

                                            }

                                        }
                                }

                            }
                    }
                }
            } else {
                firebaseUser?.uid.let { it1 ->
                    // Check if user.id is not null
                    user.id?.let { userId ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(user.id)
                            .removeValue().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.id)
                                        .child("Followers").child(it1.toString())
                                        .removeValue().addOnCompleteListener { task ->
                                            if (task.isSuccessful) {

                                            }

                                        }
                                }

                            }
                    }
                }
            }
        }
    }



    override fun getItemCount(): Int {
        return users.size
    }

    fun updateUsers(newUsers: List<UserModel>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }

    private fun checkFollowingStatus(id: String?, followBtn: AppCompatButton) {

        val followingRef = firebaseUser?.uid.let { it1 ->
                FirebaseDatabase.getInstance().reference
                    .child("Follow").child(it1.toString())
                    .child("Following")
        }

        followingRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(id.toString()).exists()) {
                    followBtn.text = "Following"
                } else {
                    followBtn.text = "Follow"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

}