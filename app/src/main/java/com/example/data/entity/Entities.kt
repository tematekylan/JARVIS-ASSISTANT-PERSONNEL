package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val summary: String = ""
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: Long,
    val role: String, // "user" or "assistant" or "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val toolName: String? = null,
    val toolInput: String? = null,
    val toolOutput: String? = null,
    val imageUri: String? = null,
    val isError: Boolean = false
)

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val key: String, // e.g. "Language Preference", "User Name", "Project Goal"
    val content: String, // e.g. "User prefers Kotlin & Python"
    val category: String = "General", // "Preferences", "Work", "Identity", "System"
    val timestamp: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val tags: String = "general",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tool_logs")
data class ToolLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val toolName: String,
    val inputParams: String,
    val outputResult: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMs: Long = 0,
    val status: String = "SUCCESS" // "SUCCESS", "ERROR"
)

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String = "",
    val phone: String = "",
    val displayName: String = "",
    val passwordHash: String = "",
    val authProvider: String = "email", // "google", "email", "phone"
    val clearanceLevel: String = "LEVEL 5 (COMMANDER)",
    val avatarUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "incident_reports")
data class IncidentReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val incidentCode: String = "INC-" + System.currentTimeMillis().toString().takeLast(6),
    val errorCode: String = "ERR_GENERIC_EXCEPTION",
    val errorMessage: String = "",
    val stackTrace: String = "",
    val contextInfo: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val emailRecipient: String = "temateteddy@gmail.com",
    val emailSent: Boolean = false,
    val aiCouncilStatus: String = "CONVENED", // "CONVENED", "ANALYZING", "RESOLVED"
    val aiCouncilDeliberation: String = "",
    val aiCouncilHotfixCode: String = ""
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val userName: String = "Sir",
    val userEmail: String = "",
    val userPhone: String = "",
    val authProvider: String = "guest", // "google", "email", "phone", "guest"
    val isLoggedIn: Boolean = false,
    val securityClearanceLevel: String = "LEVEL 5 (COMMANDER)",
    val assistantName: String = "T-HACK AI",
    val voiceLanguage: String = "fr", // "fr", "en", "auto"
    val ttsEnabled: Boolean = true,
    val autoSpeakResponses: Boolean = false,
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val aiModel: String = "gemini-3.5-flash",
    val temperature: Float = 0.7f,
    val memoryEnabled: Boolean = true,
    val isDemoMode: Boolean = false,
    val personalityTone: String = "Calm & Professional",
    val activeAiProvider: String = "gemini", // "gemini", "openai", "claude", "groq", "deepseek"
    val customGeminiApiKey: String = "",
    val customOpenAiApiKey: String = "",
    val customClaudeApiKey: String = "",
    val customGroqApiKey: String = "",
    val customDeepSeekApiKey: String = "",
    val voicePersonaName: String = "",
    val voicePersonaDescription: String = "",
    val voicePersonaPitch: Float = 1.0f,
    val voicePersonaRate: Float = 1.0f,
    val voicePersonaPromptStyle: String = "",
    val isVoicePersonaActive: Boolean = false,
    val developerAlertEmail: String = "temateteddy@gmail.com",
    val autoSendErrorAlerts: Boolean = true
)
