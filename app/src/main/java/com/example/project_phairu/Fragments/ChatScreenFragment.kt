package com.example.project_phairu.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_phairu.Adapter.CommentsAdapter
import com.example.project_phairu.Adapter.MessageAdapter
import com.example.project_phairu.Model.CommentsModel
import com.example.project_phairu.Model.MessageModel
import com.example.project_phairu.Model.UserModel
import com.example.project_phairu.R
import com.example.project_phairu.databinding.FragmentChatScreenBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class ChatScreenFragment : Fragment() {

    //binding
    private lateinit var binding: FragmentChatScreenBinding

    //navController
    private lateinit var navController: NavController

    //userId
    private var userId: String? = null

    //users Messages and messageAdapter
    private lateinit var messageAdapter: MessageAdapter
    private var messagesList: MutableList<MessageModel>? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentChatScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //get userId
        userId = arguments?.getString("userId")
        Log.d("ChatScreenFragment", "Received userId: $userId")

        //Instantiate the Recyclerview
        val recyclerView = binding.userChatsRecyclerView
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)

        // Initialize the commentsList and CommentsAdapter
        messagesList = mutableListOf()
        messageAdapter = MessageAdapter(requireContext(), messagesList as MutableList<MessageModel>)
        recyclerView.adapter = messageAdapter

        //get user info
        getUserInfo(userId)

        //init navController
        navController = findNavController()


        //send message
        binding.sendMessage.setOnClickListener{
            val messageText = binding.messageText.text.toString()
            if (messageText.isNotBlank()) {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
                getOrCreateConversationId(currentUserId, userId!!, messageText)
                binding.messageText.text.clear()
                Log.d("ChatScreenFragment", "Message sent: $messageText")
            }
        }

        //navigate back to ChatFragment
        binding.backBtn.setOnClickListener {
            navController.navigate(R.id.action_chatScreenFragment_to_chatFragment)
        }

    }

    private fun getUserInfo(userId: String?) {
        val chatReceiverRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId.toString())

        chatReceiverRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(UserModel::class.java)

                    // Capitalize first and last names
                    val capitalizedFirstName = user?.firstname?.capitalize() ?: ""
                    val capitalizedLastName = user?.lastname?.capitalize() ?: ""
                    binding.userProfileName.text = "$capitalizedFirstName $capitalizedLastName"

                    // Get current user Profile Picture
                    Picasso.get().load(user?.profilePicture)
                        .placeholder(R.drawable.profile_placeholder).into(binding.ProfilePic)
                }
            }

            override fun onCancelled(error: DatabaseError) {
               Log.e("ChatScreenFragment", "Error fetching data: ${error.message}")
            }
        })
    }

    private fun sendMessage(chatMessage: String, conversationId: String) {

        // Get current userID
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // 2. Create a new message
        val messageId = FirebaseDatabase.getInstance().reference.child("Messages").push().key
        val message = MessageModel(
            messageId = messageId!!,
            conversationId = conversationId,
            senderId = currentUserId,
            receiverId = userId,
            message = chatMessage,
            messageDate = System.currentTimeMillis().toString(),
        )

        // 3. Send the message
        FirebaseDatabase.getInstance().reference.child("Messages").child(messageId).setValue(message)
    }

    private fun getOrCreateConversationId(userId1: String, userId2: String, messageText: String) {
        val database = FirebaseDatabase.getInstance()

        // Query conversations where either user is a participant
        val query = database.reference.child("Conversations")
            .orderByChild("participants/$userId1").equalTo(true) // Check if userId1 is a participant

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var conversationId: String? = null
                for (conversationSnapshot in snapshot.children) {
                    val participantsMap = conversationSnapshot.child("participants").value as? Map<String, Boolean>
                    if (participantsMap != null && participantsMap.containsKey(userId2)) {
                        // Found the conversation
                        conversationId = conversationSnapshot.key
                        break
                    }
                }

                if (conversationId != null) {
                    // Conversation exists, use the conversationId
                    sendMessage(messageText, conversationId)
                    getMessages(conversationId)
                } else {
                    // Conversation doesn't exist, create a new one
                    val newConversationId = database.reference.child("Conversations").push().key
                    val conversationData = mapOf("participants" to mapOf(userId1 to true, userId2 to true))
                    database.reference.child("Conversations").child(newConversationId!!).setValue(conversationData)
                        .addOnSuccessListener {
                            // Use the newConversationId to send the message
                            sendMessage(messageText, newConversationId)
                            getMessages(newConversationId)

                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Log.e("ChatScreenFragment", "Error getting or creating conversation: ${error.message}")
            }
        })
    }

    private fun getMessages(conversationId: String) {
        val messagesRef = FirebaseDatabase.getInstance().reference.child("Messages").orderByChild("conversationId").equalTo(conversationId)

        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    (messagesList as MutableList<MessageModel>).clear()
                    for (messageSnapshot in snapshot.children) {
                        val message = messageSnapshot.getValue(MessageModel::class.java)
                        if (message != null) {
                            (messagesList as MutableList<MessageModel>).add(message)
                            Log.d("ChatScreenFragment", "Message received: ${message.message}")
                        }
                        messageAdapter.notifyDataSetChanged()
                    }
                } else {
                    Log.d("ChatScreenFragment", "No messages found")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatScreenFragment", "Error fetching messages: ${error.message}")
            }
        })
    }
}