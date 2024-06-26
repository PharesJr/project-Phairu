package com.example.project_phairu.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.project_phairu.R
import com.example.project_phairu.databinding.FragmentEditProfileBinding

class EditProfileFragment : Fragment() {

    //binding
    private lateinit var binding: FragmentEditProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //find the back button
        binding.backIcon.setOnClickListener {
            //navigate back to profile fragment
            requireActivity().onBackPressed()
        }
    }
}