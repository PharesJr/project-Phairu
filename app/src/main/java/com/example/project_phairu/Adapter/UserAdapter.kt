package com.example.project_phairu.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.example.project_phairu.Model.UserModel
import com.example.project_phairu.R
import com.example.project_phairu.databinding.FragmentExploreBinding
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Locale

class UserAdapter (private var context: Context,
                   private var users: List<UserModel>,
    private var fragment: Boolean = false) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {


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

    }

    override fun getItemCount(): Int {
        return users.size
    }

}