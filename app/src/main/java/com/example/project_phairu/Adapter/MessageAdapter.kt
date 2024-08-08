package com.example.project_phairu.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_phairu.Model.MessageModel
import com.example.project_phairu.R
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter (private var context: Context,
                      private var messages: MutableList<MessageModel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_SENDER = 1
    private val VIEW_TYPE_RECEIVER = 2

    class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENDER) {
            val view = LayoutInflater.from(context).inflate(R.layout.message_sender_layout, parent, false)
            SenderViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.message_reciever_layout, parent, false)
            ReceiverViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        if (holder is SenderViewHolder) {
            holder.senderMessage.text = message.message
            holder.senderMessageTime.text = message.messageTimestamp
        } else if (holder is ReceiverViewHolder) {
            holder.receiverMessage.text = message.message
            holder.receiverMessageTime.text = message.messageTimestamp
        }
    }

    override fun getItemCount(): Int {
       return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        return if (message.senderId == currentUserId) {
            VIEW_TYPE_SENDER
        } else {
            VIEW_TYPE_RECEIVER
        }
    }


    inner class SenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialize sender views
        var senderMessageTime: TextView = itemView.findViewById(R.id.senderTextTime)
        var senderMessage: TextView = itemView.findViewById(R.id.senderText)
    }

    inner class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialize receiver views
        var receiverMessage: TextView = itemView.findViewById(R.id.receiverText)
        var receiverMessageTime: TextView = itemView.findViewById(R.id.receiverTextTime)
    }


}