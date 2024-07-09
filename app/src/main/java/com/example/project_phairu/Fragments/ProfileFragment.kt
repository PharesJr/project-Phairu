package com.example.project_phairu.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.project_phairu.MainActivity
import com.example.project_phairu.R
import com.example.project_phairu.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    //binding
    private lateinit var binding: FragmentProfileBinding

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
        //make the bottom navigation visible
        (activity as? MainActivity)?.showBottomNavigation()

        //find the EditProfile button
        binding.editProfileBtn.setOnClickListener {
            //navigate to edit profile activity
            val editProfileFragment = EditProfileFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, editProfileFragment) // Replace 'R.id.fragment_container' with the ID of your Fragmentcontainer
                .addToBackStack(null) // Optional: Add to back stack for back navigation
                .commit()
        }

    }

}