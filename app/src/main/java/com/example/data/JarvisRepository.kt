package com.example.data

import com.example.data.entity.ConversationEntity
import com.example.data.entity.MemoryEntity
import com.example.data.entity.MessageEntity
import com.example.data.entity.NoteEntity
import com.example.data.entity.ToolLogEntity
import com.example.data.entity.UserAccountEntity
import com.example.data.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

class JarvisRepository(private val db: AppDatabase) {

    // Conversations
    val allConversations: Flow<List<ConversationEntity>> = db.conversationDao().getAllConversations()

    suspend fun getConversation(id: Long): ConversationEntity? = db.conversationDao().getConversationById(id)

    suspend fun createConversation(title: String): Long {
        val conv = ConversationEntity(title = title)
        return db.conversationDao().insertConversation(conv)
    }

    suspend fun updateConversation(conversation: ConversationEntity) {
        db.conversationDao().updateConversation(conversation)
    }

    suspend fun deleteConversation(id: Long) {
        db.messageDao().deleteMessagesForConversation(id)
        db.conversationDao().deleteConversationById(id)
    }

    suspend fun clearAllConversations() {
        db.messageDao().deleteAllMessages()
        db.conversationDao().deleteAllConversations()
    }

    // Messages
    fun getMessages(conversationId: Long): Flow<List<MessageEntity>> =
        db.messageDao().getMessagesForConversation(conversationId)

    suspend fun getMessagesList(conversationId: Long): List<MessageEntity> =
        db.messageDao().getMessagesListForConversation(conversationId)

    val totalMessageCount: Flow<Int> = db.messageDao().getTotalMessageCount()

    suspend fun addMessage(message: MessageEntity): Long {
        val id = db.messageDao().insertMessage(message)
        // Update conversation timestamp
        val conv = db.conversationDao().getConversationById(message.conversationId)
        if (conv != null) {
            db.conversationDao().updateConversation(conv.copy(updatedAt = System.currentTimeMillis()))
        }
        return id
    }

    // Memories
    val allMemories: Flow<List<MemoryEntity>> = db.memoryDao().getAllMemories()

    suspend fun getActiveMemories(): List<MemoryEntity> = db.memoryDao().getActiveMemoriesList()

    suspend fun saveMemory(key: String, content: String, category: String = "General"): Long {
        return db.memoryDao().insertMemory(
            MemoryEntity(
                key = key,
                content = content,
                category = category
            )
        )
    }

    suspend fun updateMemory(memory: MemoryEntity) = db.memoryDao().updateMemory(memory)

    suspend fun deleteMemory(id: Long) = db.memoryDao().deleteMemoryById(id)

    suspend fun clearAllMemories() = db.memoryDao().deleteAllMemories()

    // Notes
    val allNotes: Flow<List<NoteEntity>> = db.noteDao().getAllNotes()

    suspend fun saveNote(title: String, content: String, tags: String = "general"): Long {
        return db.noteDao().insertNote(
            NoteEntity(
                title = title,
                content = content,
                tags = tags
            )
        )
    }

    suspend fun searchNotes(query: String): List<NoteEntity> = db.noteDao().searchNotes(query)

    suspend fun updateNote(note: NoteEntity) = db.noteDao().updateNote(note)

    suspend fun deleteNote(id: Long) = db.noteDao().deleteNoteById(id)

    // Tool Logs
    val recentToolLogs: Flow<List<ToolLogEntity>> = db.toolLogDao().getRecentToolLogs()
    val totalToolCallsCount: Flow<Int> = db.toolLogDao().getTotalToolCallsCount()

    suspend fun logToolCall(toolName: String, input: String, output: String, durationMs: Long, status: String = "SUCCESS") {
        db.toolLogDao().insertToolLog(
            ToolLogEntity(
                toolName = toolName,
                inputParams = input,
                outputResult = output,
                durationMs = durationMs,
                status = status
            )
        )
    }

    suspend fun clearToolLogs() = db.toolLogDao().clearToolLogs()

    // User Settings
    val userSettings: Flow<UserSettingsEntity?> = db.userSettingsDao().getUserSettings()

    suspend fun getUserSettingsDirect(): UserSettingsEntity {
        var settings = db.userSettingsDao().getUserSettingsDirect()
        if (settings == null) {
            settings = UserSettingsEntity()
            db.userSettingsDao().saveUserSettings(settings)
        }
        return settings
    }

    suspend fun saveUserSettings(settings: UserSettingsEntity) {
        db.userSettingsDao().saveUserSettings(settings)
    }

    // User Accounts
    val allUserAccounts: Flow<List<UserAccountEntity>> = db.userAccountDao().getAllAccounts()

    suspend fun getAccountByEmail(email: String): UserAccountEntity? =
        db.userAccountDao().getAccountByEmail(email)

    suspend fun getAccountByPhone(phone: String): UserAccountEntity? =
        db.userAccountDao().getAccountByPhone(phone)

    suspend fun createOrUpdateAccount(account: UserAccountEntity): Long =
        db.userAccountDao().insertAccount(account)

    suspend fun deleteAccount(account: UserAccountEntity) =
        db.userAccountDao().deleteAccount(account)
}
