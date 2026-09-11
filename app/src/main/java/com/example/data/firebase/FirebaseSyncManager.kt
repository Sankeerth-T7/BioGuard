package com.example.data.firebase

import android.util.Log
import com.example.data.model.Observation
import com.example.data.model.ThreatReport
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirebaseSyncManager {

    companion object {
        private const val TAG = "FirebaseSyncManager"
        private const val USERS_COLLECTION = "bioguard_users"
        private const val OBS_SUBCOLLECTION = "observations"
        private const val THREATS_SUBCOLLECTION = "threat_reports"
    }

    private var firestore: FirebaseFirestore? = null

    init {
        try {
            firestore = FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore instance not available yet: ${e.message}")
        }
    }

    suspend fun syncUserToFirestore(
        userId: String,
        email: String,
        name: String,
        points: Int,
        level: Int,
        observations: List<Observation>,
        threatReports: List<ThreatReport>,
        unlockedAchievementIds: List<String>
    ): Boolean = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext false
        if (userId.isBlank()) return@withContext false

        try {
            val userRef = db.collection(USERS_COLLECTION).document(userId)

            val profileData = hashMapOf(
                "email" to email,
                "name" to name,
                "points" to points,
                "level" to level,
                "achievements" to unlockedAchievementIds,
                "lastSyncAt" to System.currentTimeMillis()
            )
            userRef.set(profileData, SetOptions.merge()).awaitResult()

            // Save observations
            val obsBatch = db.batch()
            observations.filter { !it.isDemo }.forEach { obs ->
                val obsDoc = userRef.collection(OBS_SUBCOLLECTION).document(obs.id.toString().ifBlank { "obs_${obs.createdAt}" })
                val obsMap = hashMapOf(
                    "commonName" to obs.commonName,
                    "scientificName" to obs.scientificName,
                    "category" to obs.category,
                    "confidence" to obs.confidence,
                    "description" to obs.description,
                    "identifyingFeatures" to obs.identifyingFeatures,
                    "habitat" to obs.habitat,
                    "threats" to obs.threats,
                    "conservationStatus" to obs.conservationStatus,
                    "conservationAdvice" to obs.conservationAdvice,
                    "ecologicalRole" to obs.ecologicalRole,
                    "imageUri" to obs.imageUri,
                    "location" to obs.location,
                    "createdAt" to obs.createdAt
                )
                obsBatch.set(obsDoc, obsMap, SetOptions.merge())
            }

            // Save threat reports
            threatReports.filter { !it.isDemo }.forEach { tr ->
                val trDoc = userRef.collection(THREATS_SUBCOLLECTION).document(tr.reportCode.ifBlank { "tr_${tr.createdAt}" })
                val trMap = hashMapOf(
                    "reportCode" to tr.reportCode,
                    "threatType" to tr.threatType,
                    "title" to tr.title,
                    "description" to tr.description,
                    "location" to tr.location,
                    "dateStr" to tr.dateStr,
                    "severity" to tr.severity,
                    "imageUri" to (tr.imageUri ?: ""),
                    "status" to tr.status,
                    "createdAt" to tr.createdAt
                )
                obsBatch.set(trDoc, trMap, SetOptions.merge())
            }

            obsBatch.commit().awaitResult()
            Log.d(TAG, "Successfully synced user $userId to Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed syncing to Firestore: ${e.message}", e)
            false
        }
    }

    suspend fun fetchUserFromFirestore(userId: String): FirestoreUserData? = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext null
        if (userId.isBlank()) return@withContext null

        try {
            val userRef = db.collection(USERS_COLLECTION).document(userId)
            val docSnapshot = userRef.get().awaitResult()
            if (!docSnapshot.exists()) return@withContext null

            val name = docSnapshot.getString("name") ?: "Eco Explorer"
            val email = docSnapshot.getString("email") ?: ""
            val points = docSnapshot.getLong("points")?.toInt() ?: 100
            val level = docSnapshot.getLong("level")?.toInt() ?: 1
            @Suppress("UNCHECKED_CAST")
            val achievements = (docSnapshot.get("achievements") as? List<String>) ?: emptyList()

            // Fetch observations
            val obsSnapshot = userRef.collection(OBS_SUBCOLLECTION).get().awaitResult()
            val observations = obsSnapshot.documents.mapNotNull { doc ->
                Observation(
                    commonName = doc.getString("commonName") ?: return@mapNotNull null,
                    scientificName = doc.getString("scientificName") ?: "",
                    category = doc.getString("category") ?: "Fauna",
                    confidence = doc.getLong("confidence")?.toInt() ?: 90,
                    description = doc.getString("description") ?: "",
                    identifyingFeatures = doc.getString("identifyingFeatures") ?: "",
                    habitat = doc.getString("habitat") ?: "",
                    threats = doc.getString("threats") ?: "",
                    conservationStatus = doc.getString("conservationStatus") ?: "Least Concern",
                    conservationAdvice = doc.getString("conservationAdvice") ?: "",
                    ecologicalRole = doc.getString("ecologicalRole") ?: "",
                    imageUri = doc.getString("imageUri") ?: "",
                    location = doc.getString("location") ?: "Field Observation",
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                    isDemo = false
                )
            }

            // Fetch threat reports
            val threatSnapshot = userRef.collection(THREATS_SUBCOLLECTION).get().awaitResult()
            val threatReports = threatSnapshot.documents.mapNotNull { doc ->
                ThreatReport(
                    reportCode = doc.getString("reportCode") ?: return@mapNotNull null,
                    threatType = doc.getString("threatType") ?: "Habitat Loss",
                    title = doc.getString("title") ?: "Incident Report",
                    description = doc.getString("description") ?: "",
                    location = doc.getString("location") ?: "Field Location",
                    dateStr = doc.getString("dateStr") ?: "Recent",
                    severity = doc.getString("severity") ?: "Medium",
                    imageUri = doc.getString("imageUri").takeIf { !it.isNullOrBlank() },
                    status = doc.getString("status") ?: "Under Review",
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                    isDemo = false
                )
            }

            FirestoreUserData(
                name = name,
                email = email,
                points = points,
                level = level,
                achievements = achievements,
                observations = observations,
                threatReports = threatReports
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user from Firestore: ${e.message}", e)
            null
        }
    }
}

data class FirestoreUserData(
    val name: String,
    val email: String,
    val points: Int,
    val level: Int,
    val achievements: List<String>,
    val observations: List<Observation>,
    val threatReports: List<ThreatReport>
)
