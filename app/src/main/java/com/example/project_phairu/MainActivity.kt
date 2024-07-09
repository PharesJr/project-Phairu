package com.example.project_phairu
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.project_phairu.Fragments.ChatFragment
import com.example.project_phairu.Fragments.CommunityFragment
import com.example.project_phairu.Fragments.EventsFragment
import com.example.project_phairu.Fragments.HomeFragment
import com.example.project_phairu.Fragments.NotificationsFragment
import com.example.project_phairu.Fragments.ProfileFragment
import com.example.project_phairu.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ismaeldivita.chipnavigation.ChipNavigationBar


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    internal var selectedFragment: androidx.fragment.app.Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Find the BottomNavigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)

        // Set a click listener on the BottomNavigation
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_home -> {
                    selectedFragment = HomeFragment()

                }
                R.id.action_chat -> {
                    selectedFragment = ChatFragment()
                }
                R.id.action_events -> {
                    selectedFragment = EventsFragment()

                }
                R.id.action_notifications -> {
                    selectedFragment = NotificationsFragment()
                }
                R.id.action_profile -> {
                    selectedFragment = ProfileFragment()

                }

                else -> false
            }

            // Perform fragment transaction if selectedFragment is not null
            if (selectedFragment != null) {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    selectedFragment!!
                ).commit()
            }
            true // Indicate that the click event has been handled
        }

        // Set the default fragment to HomeFragment
        supportFragmentManager.beginTransaction().replace(
            R.id.fragment_container, HomeFragment()
        ).commit()

    }

    fun showBottomNavigation() {
        binding.navView.visibility = View.VISIBLE
    }

    fun hideBottomNavigation() {
        binding.navView.visibility = View.GONE
    }

}