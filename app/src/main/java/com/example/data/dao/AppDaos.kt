package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.ConversationEntity
import com.example.data.entity.MemoryEntity
import com.example.data.entity.MessageEntity
import com.example.data.entity.NoteEntity
import com.example.data.entity.ToolLogEntity
import com.example.data.entity.UserAccountEntity
import com.example.data.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getConversationById(id: Long): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity): Long

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversationById(id: Long)

    @Query("DELETE FROM conversations")
    suspend fun deleteAllConversations()
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessagesListForConversation(conversationId: Long): List<MessageEntity>

    @Query("SELECT COUNT(*) FROM messages")
    fun getTotalMessageCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: Long)

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()
}

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE isActive = 1 ORDER BY timestamp DESC")
    suspend fun getActiveMemoriesList(): List<MemoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Update
    suspend fun updateMemory(memory: MemoryEntity)

    @Delete
    suspend fun deleteMemory(memory: MemoryEntity)

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteMemoryById(id: Long)

    @Query("DELETE FROM memories")
    suspend fun deleteAllMemories()
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    suspend fun searchNotes(query: String): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)
}

@Dao
interface ToolLogDao {
    @Query("SELECT * FROM tool_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentToolLogs(): Flow<List<ToolLogEntity>>

    @Query("SELECT COUNT(*) FROM tool_logs")
    fun getTotalToolCallsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToolLog(log: ToolLogEntity): Long

    @Query("DELETE FROM tool_logs")
    suspend fun clearToolLogs()
}

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun getUserSettings(): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = 1")
    suspend fun getUserSettingsDirect(): UserSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserSettings(settings: UserSettingsEntity)
}

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts ORDER BY lastLoginAt DESC")
    fun getAllAccounts(): Flow<List<UserAccountEntity>>

    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    suspend fun getAccountByEmail(email: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE phone = :phone LIMIT 1")
    suspend fun getAccountByPhone(phone: String): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: UserAccountEntity): Long

    @Update
    suspend fun updateAccount(account: UserAccountEntity)

    @Delete
    suspend fun deleteAccount(account: UserAccountEntity)
}
