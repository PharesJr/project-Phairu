package com.example.project_phairu.Adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.project_phairu.Fragments.ExploreFragmentDirections
import com.example.project_phairu.Fragments.HomeFragment
import com.example.project_phairu.Model.BookmarkModel
import com.example.project_phairu.Model.NotificationsModel
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
                   private var posts: MutableList<PostsModel>,
                   private val source: String) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialize views
        var profileName: TextView = itemView.findViewById(R.id.postProfileName)
        var profileUsername: TextView = itemView.findViewById(R.id.postProfileUsername)
        var profilePic: CircleImageView = itemView.findViewById(R.id.postProfilePic)
        var postImage: ShapeableImageView = itemView.findViewById(R.id.postImage)
        var imageHolder: LinearLayout = itemView.findViewById(R.id.bottomLayer)
        var postCaption: TextView = itemView.findViewById(R.id.postCaption)
        var postTime: TextView = itemView.findViewById(R.id.postTime)
        var likesCount: TextView = itemView.findViewById(R.id.likesCount)
        var commentsCount: TextView = itemView.findViewById(R.id.commentsCount)
        var likeBtn: ImageView = itemView.findViewById(R.id.post_image_like_btn)
        var commentBtn: ImageView = itemView.findViewById(R.id.post_image_comment_btn)
        var shareBtn: ImageView = itemView.findViewById(R.id.post_share_comment_btn)
        var postmenu: RelativeLayout = itemView.findViewById(R.id.post_menu)
        var bookmarkLayout: LinearLayout = itemView.findViewById(R.id.bookmarkLayout)
        var bookmarkIcon: ImageView = itemView.findViewById(R.id.bookmarkIcon)
        var isLiked: Boolean = false



    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.posts_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
    return posts.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        // get current userId
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        // Bind data to views
        val post = posts[position]

        //get the postId
        val postId = post.postId.toString()

        // get the user data for the current post(profilePic, name, username)
        userInfo(holder.profilePic, holder.profileName, holder.profileUsername, post.senderId)

        // Check bookmark status
        checkBookmarkStatus(postId, holder.bookmarkIcon)


        // Image visibility
        if (post.postPicture != null) {
            holder.imageHolder.visibility = View.VISIBLE
            Picasso.get().load(post.postPicture).into(holder.postImage)
        } else {
            holder.imageHolder.visibility = View.GONE
        }

        // Caption visibility
        if (post.postDescription == "") {
            holder.postCaption.visibility = View.GONE
        } else {
            holder.postCaption.visibility = View.VISIBLE
            holder.postCaption.text = post.postDescription
        }

        // userProfile pic navigation
        holder.profilePic.setOnClickListener {
            val profileId = post.senderId
            // Get current user ID
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

            if (profileId != null) {
                val navController = Navigation.findNavController(holder.itemView)
                val bundle = Bundle().apply { putString("profileId", profileId) }

                if (source == "home") {
                    if (profileId != currentUserId) {
                        navController.navigate(R.id.action_homeFragment_to_profileFragment, bundle)
                    }else {
                        Toast.makeText(context, "This is your own profile", Toast.LENGTH_SHORT).show()
                    }
                } else if (source == "profile") {
                    if (profileId != currentUserId) {
                        navController.navigate(R.id.action_profileFragment_to_self, bundle)
                    }else {
                        Toast.makeText(context, "This is your own profile", Toast.LENGTH_SHORT).show()
                    }
                } else if (source == "bookmarks") {
                    if (profileId != currentUserId) {
                        navController.navigate(R.id.action_bookmarksFragment_to_profileFragment, bundle)
                    }else {
                        Toast.makeText(context, "This is your own profile", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Like button functionality
        holder.likeBtn.setOnClickListener {
            val likesRef = FirebaseDatabase.getInstance().reference.child("Likes").child(postId)
            if (holder.isLiked) {
                likesRef.child(firebaseUser?.uid.toString()).removeValue()
                    .addOnSuccessListener {
                        holder.isLiked = false
                        updateLikeButton(holder.likeBtn, false)
                        updateLikesCount(postId, holder.likesCount)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error unliking post: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {if (firebaseUser != null) {
                likesRef.child(firebaseUser.uid).setValue(true)
                    .addOnSuccessListener {
                        holder.isLiked = true
                        updateLikeButton(holder.likeBtn, true)
                        updateLikesCount(postId, holder.likesCount)
                        addNotification(post.senderId, postId)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error liking post: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            }
        }

        // post menu button functionality
        holder.postmenu.setOnClickListener {
            val popupMenu = PopupMenu(context, holder.postmenu)
            popupMenu.inflate(R.menu.post_options_menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.flag -> {
                        // Handle flag action
                        true
                    }
                    R.id.deletePost -> {
                        // Handle delete action
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        //get the Post time and date and state the passed time
        val relativeTime = getRelativeTime(post.postId)
        holder.postTime.text = relativeTime

        // Check if liked.... and set likes count
        isLiked(postId, holder.likeBtn)
        likesCount(postId, holder.likesCount)

        //get Comments count
        commentsCount(postId, holder.commentsCount)

        // Comments button functionality
        holder.commentBtn.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("postId", postId)
            bundle.putString("senderId", post.senderId)
            Navigation.findNavController(holder.itemView).navigate(R.id.action_homeFragment_to_commentsFragment, bundle)
        }

        //OnClick Listener when a user Clicks on a post
        holder.itemView.setOnClickListener {
            if (source == "home") {
                val bundle = Bundle()
                bundle.putString("postId", postId)
                bundle.putString("senderId", post.senderId)
                Navigation.findNavController(holder.itemView).navigate(R.id.action_homeFragment_to_commentsFragment, bundle)
            } else if (source == "profile") {
                val bundle = Bundle()
                bundle.putString("postId", postId)
                bundle.putString("senderId", post.senderId)
                Navigation.findNavController(holder.itemView).navigate(R.id.action_profileFragment_to_commentsFragment, bundle)
            } else if (source == "bookmarks") {
                val bundle = Bundle()
                bundle.putString("postId", postId)
                bundle.putString("senderId", post.senderId)
                Navigation.findNavController(holder.itemView).navigate(R.id.action_bookmarksFragment_to_commentsFragment, bundle)
            }
        }

        // Bookmark button functionality
        holder.bookmarkLayout.setOnClickListener {
           if (holder.bookmarkIcon.tag == "Save") {

               val bookmark = BookmarkModel(System.currentTimeMillis().toString(), true)

               FirebaseDatabase.getInstance().reference.child("Bookmarks").child(firebaseUser?.uid.toString()).child(postId).setValue(bookmark)
               Toast.makeText(context, "Post saved.", Toast.LENGTH_SHORT).show()
           } else{
               FirebaseDatabase.getInstance().reference.child("Bookmarks").child(firebaseUser?.uid.toString()).child(postId).removeValue()
               Toast.makeText(context, "Post removed from bookmarks.", Toast.LENGTH_SHORT).show()
           }

        }

    }

    private fun isLiked(postId: String, likeButton: ImageView) {
        //get current user
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val likesRef = FirebaseDatabase.getInstance().reference.child("Likes").child(postId)

        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(firebaseUser?.uid.toString()).exists()) {
                    likeButton.setImageResource(R.drawable.heart_red)
                    likeButton.tag = "Liked"
                } else {
                    likeButton.setImageResource(R.drawable.heart)
                    likeButton.tag = "Like"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PostAdapter", "Error checking if post is liked: ${error.message}")
            }
        })
    }

    private fun likesCount(postId: String, likesCount: TextView) {
        val likesRef = FirebaseDatabase.getInstance().reference.child("Likes").child(postId)

        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                   likesCount.text = snapshot.childrenCount.toString()
                } else{
                    likesCount.text = "0"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PostAdapter", "Error getting likes count: ${error.message}")
            }
        })
    }

    private fun commentsCount(postId: String, commentsCount: TextView) {
        val commentsRef = FirebaseDatabase.getInstance().reference.child("Comments").child(postId)

        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    commentsCount.text = snapshot.childrenCount.toString()
                } else{
                    commentsCount.text = "0"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PostAdapter", "Error getting comments count: ${error.message}")
            }
        })
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
                        .placeholder(R.drawable.profilepic_placeholder).into(profilePic)
                   profileName.text = ("$capitalizedFirstName $capitalizedLastName")
                   profileUsername.text = ("@" + user?.username)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PostAdapter", "Error getting user info: ${error.message}")
            }
        })
    }

    private fun updateLikeButton(likeButton: ImageView, isLiked: Boolean) {
        if (isLiked) {
            likeButton.setImageResource(R.drawable.heart_red)
            likeButton.tag = "Liked"
        } else {
            likeButton.setImageResource(R.drawable.heart)
            likeButton.tag = "Like"
        }
    }

    private fun updateLikesCount(postId: String, likesCountTextView: TextView) {
        val likesRef = FirebaseDatabase.getInstance().reference.child("Likes").child(postId)
        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                likesCountTextView.text = snapshot.childrenCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PostAdapter", "Error updating likes count: ${error.message}")
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
            timeDiff < 60000 -> "${timeDiff / 1000} sec ago"
            timeDiff < 3600000 -> "${timeDiff / 60000} min ago"
            timeDiff < 86400000 -> "${timeDiff / 3600000} hr ago"
            else -> "${timeDiff / 86400000} days ago"
        }
    }

    //get bookmarks status
    private fun checkBookmarkStatus(postId: String, bookmarkButton: ImageView) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val bookmarksRef = FirebaseDatabase.getInstance().reference.child("Bookmarks").child(firebaseUser?.uid.toString())

        bookmarksRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
               if (snapshot.child(postId).exists()) {
                   bookmarkButton.setImageResource(R.drawable.bookmark_blue)
                   bookmarkButton.tag = "Saved"
               } else {
                   bookmarkButton.setImageResource(R.drawable.bookmark_light)
                   bookmarkButton.tag = "Save"
               }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PostAdapter", "Error checking bookmark status: ${error.message}")
            }
        })

    }

    //Add Notifications
    private fun addNotification(userId: String?, postId: String?) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val notificationsRef = FirebaseDatabase.getInstance().reference.child("Notifications").child(userId.toString())

        val notification = NotificationsModel(
            userId = firebaseUser!!.uid,
            postId = postId,
            notificationType = "like",
            notificationMessage = "liked your post",
            notificationTimestamp = System.currentTimeMillis().toString()
        )

        notificationsRef.push().setValue(notification)
    }

}