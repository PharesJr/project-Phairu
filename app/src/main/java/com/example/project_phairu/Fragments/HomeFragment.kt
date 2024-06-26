package com.example.project_phairu.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.project_phairu.R
import com.example.project_phairu.databinding.FragmentHomeBinding
import com.google.android.material.navigation.NavigationView

class HomeFragment : Fragment() {

    //binding
    private lateinit var binding: FragmentHomeBinding

    //Drawer
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize drawerLayout and navigationView
        drawerLayout = requireActivity().findViewById(R.id.drawer_layout)
        navigationView = requireActivity().findViewById(R.id.navigation_view)

        // Open the drawer when the sidebar icon is clicked
        binding.sideBar.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        //when an Item in the drawer is clicked
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_logout -> {
                    //initialize a dialog builder
                    val builder = AlertDialog.Builder(requireContext())
                    val view = layoutInflater.inflate(R.layout.dialog_logout, null)

                    builder.setView(view)
                    val dialog = builder.create()

                    view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                        dialog.dismiss()
                    }

                    view.findViewById<Button>(R.id.btnLogout).setOnClickListener {
                        // Logout logic goes here
                        dialog.dismiss()
                    }
                    dialog.show()
                    true
                }
                else -> false
            }
            // Close drawer after item click
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

    }
}