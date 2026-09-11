package com.example.data.cloud

import com.example.data.model.Observation
import com.example.data.model.ThreatReport

data class UserCloudData(
    val email: String,
    val name: String,
    val password: String = "",
    val points: Int = 100,
    val level: Int = 1,
    val observations: List<Observation> = emptyList(),
    val threatReports: List<ThreatReport> = emptyList(),
    val unlockedAchievementIds: List<String> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis()
)

sealed class CloudAuthResult {
    data class Success(
        val userCloudData: UserCloudData,
        val objectId: String,
        val isNewAccount: Boolean = false
    ) : CloudAuthResult()

    data class Error(val message: String) : CloudAuthResult()
}

sealed class CloudSyncResult {
    data class Success(val message: String, val syncedCount: Int = 0) : CloudSyncResult()
    data class Error(val message: String) : CloudSyncResult()
}
