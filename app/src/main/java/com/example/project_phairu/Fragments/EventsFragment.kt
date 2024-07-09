package com.example.project_phairu.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.project_phairu.MainActivity
import com.example.project_phairu.R
import com.example.project_phairu.databinding.FragmentChatBinding
import com.example.project_phairu.databinding.FragmentEventsBinding

class EventsFragment : Fragment() {

    //binding
    private lateinit var binding: FragmentEventsBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //make the bottom navigation visible
        (activity as? MainActivity)?.showBottomNavigation()
    }
    }
