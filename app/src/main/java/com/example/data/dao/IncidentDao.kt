package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.IncidentReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentDao {
    @Query("SELECT * FROM incident_reports ORDER BY timestamp DESC")
    fun getAllIncidents(): Flow<List<IncidentReportEntity>>

    @Query("SELECT * FROM incident_reports WHERE id = :id LIMIT 1")
    suspend fun getIncidentById(id: Long): IncidentReportEntity?

    @Query("SELECT * FROM incident_reports ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestIncident(): IncidentReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentReportEntity): Long

    @Update
    suspend fun updateIncident(incident: IncidentReportEntity)

    @Query("DELETE FROM incident_reports WHERE id = :id")
    suspend fun deleteIncident(id: Long)

    @Query("DELETE FROM incident_reports")
    suspend fun clearAllIncidents()
}
