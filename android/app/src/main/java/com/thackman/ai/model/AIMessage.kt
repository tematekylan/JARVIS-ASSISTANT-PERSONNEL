package com.thackman.ai.model

data class AIMessage(
    val id: String = System.currentTimeMillis().toString(),
    val sender: MessageSender = MessageSender.USER,
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER,
    AI_ASSISTANT
}
