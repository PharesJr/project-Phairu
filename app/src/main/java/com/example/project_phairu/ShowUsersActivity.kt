package com.example.project_phairu

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_phairu.Adapter.UserAdapter
import com.example.project_phairu.Model.UserModel
import com.example.project_phairu.databinding.ActivityShowUsersBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShowUsersActivity : AppCompatActivity() {

    //binding
    private lateinit var binding: ActivityShowUsersBinding

    //Firebase
    private lateinit var firebaseUser: FirebaseUser

    var id : String = ""
    var title : String = ""

    var userAdapter: UserAdapter? = null
    var userList: MutableList<UserModel>? = null
    var idList: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize binding
        binding = ActivityShowUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val intent = intent
        id = intent.getStringExtra("id").toString()
        title = intent.getStringExtra("title").toString()

        val toolbar : Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = title
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        //set up RecyclerVIew
        binding.usersRecyclerView.setHasFixedSize(true)
        binding.usersRecyclerView.layoutManager = LinearLayoutManager(this)
        userList = ArrayList()
        userAdapter = UserAdapter(this, userList as ArrayList<UserModel>, false)
        binding.usersRecyclerView.adapter = userAdapter

        idList = ArrayList()

        when(title)
        {
            "likes" -> getLikes()
            "following" -> getFollowing()
            "followers" -> getFollowers()
//            "views" -> getViews()
        }

    }

    private fun getLikes() {
        val likesRef = FirebaseDatabase.getInstance().reference.child("Likes").child(id)

        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    (idList as ArrayList<String>).clear()

                    for (snap in snapshot.children) {
                        (idList as ArrayList<String>).add(snap.key!!)
                    }

                    showUsers()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PostAdapter", "Error getting likes count: ${error.message}")
            }
        })
    }

    private fun getFollowing() {
        val followingCountRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(id)
            .child("Following")


        followingCountRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    (idList as ArrayList<String>).clear()
                    for (snap in snapshot.children) {
                        (idList as ArrayList<String>).add(snap.key!!)
                    }
                    showUsers()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileFragment", "Error fetching data: ${error.message}")
//                Toast.makeText(context, "Error loading profile data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getFollowers() {
        val followersCountRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(id)
            .child("Followers")


        followersCountRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    (idList as ArrayList<String>).clear()
                    for (snap in snapshot.children) {
                        (idList as ArrayList<String>).add(snap.key!!)
                    }
                    showUsers()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileFragment", "Error fetching data: ${error.message}")
//                Toast.makeText(context, "Error loading profile data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showUsers() {
        //instantiate firebase
        val userRef = FirebaseDatabase.getInstance().getReference().child("Users")
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (userList as ArrayList<UserModel>).clear()

                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(UserModel::class.java)

                    for (id in idList!!) {
                        if (user?.id == id) {
                            (userList as ArrayList<UserModel>).add(user)
                        }
                    }
                }

                userAdapter?.notifyDataSetChanged()

            }


            override fun onCancelled(error: DatabaseError) {
                // Handle the error appropriately
                Log.e("ExploreFragment", "Error retrieving users: ${error.message}")
            }

        })
    }
}