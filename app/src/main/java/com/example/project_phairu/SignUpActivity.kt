package com.example.project_phairu

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.project_phairu.Model.UserModel
import com.example.project_phairu.databinding.ActivitySignUpBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SignUpActivity : AppCompatActivity() {

    //binding
    private lateinit var binding: ActivitySignUpBinding

    //Firebase Declaration and Reference
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //binding
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //firebase
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.getReference("Users")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // SignUp Button and the call function for user sign up

        binding.createAccountButton.setOnClickListener{
            val firstname = binding.Fname.text.toString()
            val lastname =  binding.Lname.text.toString()
            val username =  binding.Username.text.toString()
            val email =  binding.email.text.toString()
            val password =  binding.password.text.toString()
            val confirmPassword =  binding.confirmPassword.text.toString()

            if(firstname.isNotEmpty() && lastname.isNotEmpty() && username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()){

                if(!(password == confirmPassword)) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                } else if (password.length < 6){
                    Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
                } else if (!email.contains("@") || !email.contains(".")){
                    Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()
                }else if (password.contains(" ")){
                    Toast.makeText(this, "Password cannot contain spaces", Toast.LENGTH_SHORT).show()
                } else if(email.contains(" ")){
                    Toast.makeText(this, "Email cannot contain spaces", Toast.LENGTH_SHORT).show()
                } else if(username.contains(" ")){
                    Toast.makeText(this, "Username cannot contain spaces", Toast.LENGTH_SHORT).show()
                } else{
                    signUpUser(firstname, lastname, username, email, password)
                    Toast.makeText(this, "Sign Up Success", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish() // Close the current activity
                }


            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Set a click listener on the "Back" TextView
        binding.btnBack.setOnClickListener {
            // Start the LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Close the current activity
        }

    }

    //signup user function

    private fun signUpUser(firstname: String, lastname: String,username: String, email: String, password: String) {
        databaseReference.orderByChild( "username").equalTo(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(!dataSnapshot.exists()){
                    val id = databaseReference.push().key
                    val userData = UserModel(id, firstname, lastname, username, email, password)
                    databaseReference.child(id!!).setValue(userData)
                    Toast.makeText(this@SignUpActivity, "Sign Up Success", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
                    finish() // Close the current activity
                }else{
                    Toast.makeText(this@SignUpActivity, "User already exists, Try a different username", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(DatabaseError: DatabaseError) {
                Toast.makeText(this@SignUpActivity, "Error: ${DatabaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}