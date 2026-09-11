package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class Species(
    val id: String,
    val commonName: String,
    val scientificName: String,
    val category: String,
    val description: String,
    val habitat: String,
    val ecologicalRole: String,
    val economicValue: String,
    val threats: String,
    val conservationStatus: String,
    val conservationAdvice: String,
    val geographicDistribution: String,
    val identifyingFeatures: List<String>,
    val emoji: String,
    val isIndianSubcontinent: Boolean = true
)

@Entity(tableName = "observations")
data class Observation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val commonName: String,
    val scientificName: String,
    val category: String,
    val confidence: Int,
    val description: String,
    val identifyingFeatures: String,
    val habitat: String,
    val threats: String,
    val conservationStatus: String,
    val conservationAdvice: String,
    val ecologicalRole: String,
    val imageUri: String,
    val location: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isDemo: Boolean = false
)

@Entity(tableName = "threat_reports")
data class ThreatReport(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reportCode: String,
    val threatType: String,
    val title: String,
    val description: String,
    val location: String,
    val dateStr: String,
    val severity: String,
    val imageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "Under Review",
    val isDemo: Boolean = false
)

data class BioLocation(
    val id: String,
    val name: String,
    val state: String,
    val type: String, // National Park, Wildlife Sanctuary, Biodiversity Hotspot, Biosphere Reserve
    val description: String,
    val importance: String,
    val keySpecies: List<String>,
    val xPercent: Float, // For responsive visual pin placement on India/Conservation Map (0.0 to 1.0)
    val yPercent: Float,
    val coordinatesStr: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val areaSqKm: Double = 0.0,
    val elevationProfile: String = "",
    val establishedYear: Int = 0
) {
    val effectiveLat: Double
        get() = if (latitude != 0.0) latitude else {
            val part = coordinatesStr.split("°")[0].trim()
            part.toDoubleOrNull() ?: 22.0
        }

    val effectiveLon: Double
        get() = if (longitude != 0.0) longitude else {
            val parts = coordinatesStr.split(",")
            if (parts.size >= 2) {
                val lonPart = parts[1].replace("°", "").replace("E", "").replace("N", "").trim()
                lonPart.toDoubleOrNull() ?: 80.0
            } else 80.0
        }
}

data class QuizQuestion(
    val id: Int,
    val category: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class ConservationTip(
    val id: String,
    val category: String, // Home, Campus, Forests, Water, Wildlife, Waste, Plants, Community
    val title: String,
    val description: String,
    val actionStep: String,
    val isAiGenerated: Boolean = false
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val badgeEmoji: String,
    val pointsReward: Int,
    val isUnlocked: Boolean = false,
    val progress: Float = 0f // 0f to 1f
)

data class UserProfile(
    val name: String = "Aarav Sharma",
    val email: String = "aarav.nature@bioguard.eco",
    val points: Int = 180,
    val level: Int = 2,
    val isGuest: Boolean = false,
    val uid: String? = null,
    val photoUrl: String? = null,
    val authProvider: String = "Firebase Auth"
)

data class SpeciesIdentificationResult(
    val commonName: String,
    val scientificName: String,
    val category: String,
    val confidence: Int,
    val description: String,
    val identifyingFeatures: List<String>,
    val habitat: String,
    val geographicDistribution: String,
    val ecologicalRole: String,
    val threats: String,
    val conservationStatus: String,
    val conservationAdvice: String,
    val isLowConfidence: Boolean = false,
    val isStatusVerified: Boolean = true,
    val imageUri: String = ""
)
