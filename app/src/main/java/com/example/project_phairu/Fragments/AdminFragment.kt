package com.example.project_phairu.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.project_phairu.R
import com.example.project_phairu.databinding.FragmentAdminBinding

class AdminFragment : Fragment() {

    // Binding
    private lateinit var binding: FragmentAdminBinding

    //backPress timer
    private var backPressedTime: Long = 0

    //navController
    private lateinit var navController: NavController


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize navController
        navController = findNavController()

        //backPressedTime
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {

            // Check if back button is pressed twice within 2 seconds
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                // Exit the app
                requireActivity().finish()
            } else {
                Toast.makeText(requireContext(), "Click again to exit the app", Toast.LENGTH_SHORT).show()
                backPressedTime = System.currentTimeMillis()
            }
        }

        // click the text
        binding.navigateToEvents.setOnClickListener {
            //navigate to createEventFragment
            navController.navigate(R.id.action_adminFragment_to_eventsUploadFragment)
        }
    }
}