package com.example.project_phairu.Model

data class PostsModel(
    val postId: String? = null, // postId == Post Timestamp in milliseconds
    val senderId: String? = null, // senderId == User ID
    val postDescription: String? = null,
    val postPicture: String? = null,
    val postTimestamp: String? = null,
    val postDate: String? = null
)
