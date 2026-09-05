package com.thackman.ai.model

data class UserSettings(
    val userName: String = "Teddy",
    val userEmail: String = "teddykylan@gmail.com",
    val userRole: String = "COMMANDANT",
    val voiceEnabled: Boolean = true,
    val autoPilotEnabled: Boolean = false,
    val securityLevel: String = "MAXIMUM",
    val theme: String = "CYBER_DARK"
)
