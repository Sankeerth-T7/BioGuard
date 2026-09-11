package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Observation
import com.example.data.model.ThreatReport
import kotlinx.coroutines.flow.Flow

@Dao
interface ObservationDao {
    @Query("SELECT * FROM observations ORDER BY createdAt DESC")
    fun getAllObservations(): Flow<List<Observation>>

    @Query("SELECT * FROM observations WHERE id = :id")
    fun getObservationById(id: Long): Flow<Observation?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(observation: Observation): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(observations: List<Observation>)

    @Delete
    suspend fun delete(observation: Observation)

    @Query("DELETE FROM observations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM observations")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM observations")
    fun getObservationCount(): Flow<Int>
}

@Dao
interface ThreatReportDao {
    @Query("SELECT * FROM threat_reports ORDER BY createdAt DESC")
    fun getAllReports(): Flow<List<ThreatReport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(report: ThreatReport): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reports: List<ThreatReport>)

    @Delete
    suspend fun delete(report: ThreatReport)

    @Query("DELETE FROM threat_reports")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM threat_reports")
    fun getReportCount(): Flow<Int>
}
