package com.example.project_phairu.Fragments
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.project_phairu.R
import com.example.project_phairu.databinding.FragmentProfileBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileFragment : Fragment() {

    //binding
    private lateinit var binding: FragmentProfileBinding

    //navController
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize navController
        navController = findNavController()


        // Find the BottomNavigation
        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.nav_view)

        // Set up the BottomNavigationView with the NavController
        bottomNavigationView.setupWithNavController(navController)

        //find the EditProfile button
        binding.editProfileBtn.setOnClickListener {
            //navigate to edit profile activity
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

    }

}