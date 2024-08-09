package com.example.project_phairu.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_phairu.Adapter.chatsAdapter
import com.example.project_phairu.Model.MessageModel
import com.example.project_phairu.R
import com.example.project_phairu.databinding.FragmentChatBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ChatFragment : Fragment() {

    //binding
    private lateinit var binding: FragmentChatBinding

    //navController
    private lateinit var navController: NavController

    // Adapter
    private lateinit var chatsAdapter: chatsAdapter
    private var chatsList: MutableList<MessageModel> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentChatBinding.inflate(inflater, container, false)
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

        // Initialize the RecyclerView
        binding.conversationsRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        chatsAdapter = chatsAdapter(requireContext(), chatsList)
        binding.conversationsRecyclerview.adapter = chatsAdapter

        // Fetch conversations
        getChats()

        // navigate to a new chat
        binding.newChat.setOnClickListener {
            navController.navigate(R.id.action_chatFragment_to_newChatFragment)
        }

    }


    private fun getChats() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val conversationsRef = FirebaseDatabase.getInstance().reference.child("Conversations")
        conversationsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val tempChatsList = mutableListOf<MessageModel>()
                    for (conversationSnapshot in snapshot.children) {
                        val participantsMap = conversationSnapshot.child("participants").value as? Map<String, Boolean>
                        if (participantsMap != null && participantsMap.containsKey(currentUserId)) {
                            val otherUserId = participantsMap.keys.first { it != currentUserId }
                            val conversationId = conversationSnapshot.key
                            val messagesRef = FirebaseDatabase.getInstance().reference.child("Messages")
                                .orderByChild("conversationId").equalTo(conversationId)
                            messagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(messageSnapshot: DataSnapshot) {
                                    var latestMessage: MessageModel? = null
                                    for (message in messageSnapshot.children) {
                                        val chat = message.getValue(MessageModel::class.java)
                                        if (chat != null && (latestMessage == null || (chat.messageTimestamp?.toLong()
                                                ?: 0L) > (latestMessage.messageTimestamp?.toLong()
                                                ?: 0L))
                                        ) {
                                            latestMessage = chat
                                            latestMessage.receiverId = otherUserId
                                        }
                                    }
                                    if (latestMessage != null) {
                                        tempChatsList.add(latestMessage)
                                    }
                                    // Update the adapter with the new list of chats
                                    chatsList.clear()
                                    chatsList.addAll(tempChatsList)
                                    chatsAdapter.notifyDataSetChanged()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("ChatFragment", "Error fetching messages: ${error.message}")
                                }
                            })
                        }
                    }
                } else {
                    Log.d("ChatFragment", "No conversations found")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatFragment", "Error fetching conversations: ${error.message}")
            }
        })
    }
}
