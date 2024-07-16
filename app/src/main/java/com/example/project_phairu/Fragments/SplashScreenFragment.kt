package com.example.project_phairu.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.project_phairu.DataStore.UserSessionDataStore
import com.example.project_phairu.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SplashScreenFragment : Fragment() {

    //DataStore
    private lateinit var userSessionDataStore: UserSessionDataStore

    //navController
    private lateinit var navController: NavController


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize navController
        navController = findNavController()


        //DataStore
        userSessionDataStore = UserSessionDataStore(requireContext())

        lifecycleScope.launch {

            // Delay for 3 seconds
            delay(3000)

            userSessionDataStore.userIdFlow.collect { userId ->
                if (userId != null) {
                    // User is logged in, proceed to MainActivity
                   findNavController().navigate(R.id.action_splashScreenFragment_to_homeFragment)
                } else {
                    // User is not logged in, proceed to LoginActivity
                    findNavController().navigate(R.id.action_splashScreenFragment_to_loginFragment)
                }
            }
        }
    }
}