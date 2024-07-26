package com.example.project_phairu.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_phairu.Model.PostsModel
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
import java.util.Locale

class PostAdapter (private var context: Context,
                   private var posts: MutableList<PostsModel>) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    // Initialize FirebaseUser (get specific user ID)
    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser


    class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialize views
        var profileName: TextView = itemView.findViewById(R.id.postProfileName)
        var profileUsername: TextView = itemView.findViewById(R.id.postProfileUsername)
        var profilePic: CircleImageView = itemView.findViewById(R.id.postProfilePic)
        var postImage: ShapeableImageView = itemView.findViewById(R.id.postImage)
        var imageHolder: LinearLayout = itemView.findViewById(R.id.bottomLayer)
        var postCaption: TextView = itemView.findViewById(R.id.postCaption)
        var likesCount: TextView = itemView.findViewById(R.id.likesCount)
        var commentsCount: TextView = itemView.findViewById(R.id.commentsCount)
        var likeBtn: ImageView = itemView.findViewById(R.id.post_image_like_btn)
        var commentBtn: ImageView = itemView.findViewById(R.id.post_image_comment_btn)
        var shareBtn: ImageView = itemView.findViewById(R.id.post_share_comment_btn)
        var saveBtn: ImageView = itemView.findViewById(R.id.post_save_comment_btn)



    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.posts_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
    return posts.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

     //instantiate the Firebase User
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        //bind data to views
        val post = posts[position]

        // Check if the post has an image
        if (post.postPicture != null) {
            holder.imageHolder.visibility = View.VISIBLE
            Picasso.get().load(post.postPicture).into(holder.postImage)
        } else {
            holder.postImage.visibility = View.GONE
        }

        //bind the data

        // get the user data
        userInfo(holder.profilePic, holder.profileName, holder.profileUsername, post.senderId)

        Picasso.get().load(post.postPicture).into(holder.itemView.findViewById<ShapeableImageView>(R.id.postImage))

        holder.postCaption.text = post.postDescription


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
                TODO("Not yet implemented")
            }
        })
    }
}