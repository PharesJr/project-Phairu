package com.example.project_phairu.Model

data class MessageModel(
    val messageId: String? = null,
    val conversationId: String? = null,
    val senderId: String? = null,
    var receiverId: String? = null,
    val message: String? = null,
    val messageTimestamp: String? = null
)
