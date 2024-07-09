package com.example.project_phairu.Model

data class UserModel(
    val id: String? = null,
    val firstname: String? = null,
    val lastname: String? = null,
    val username: String? = null,
    val email: String? = null,
    val bio: String? = "Just joined, excited to see the PCEA App!",
    val profilePicture: String? = "https://firebasestorage.googleapis.com/v0/b/project-phairu.appspot.com/o/default%20images%2Fprofile_placeholder.png?alt=media&token=8a02e84c-3910-43a2-b0a9-950a0248dfca"
)
