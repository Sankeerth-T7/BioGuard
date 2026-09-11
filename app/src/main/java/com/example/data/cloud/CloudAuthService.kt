package com.example.data.cloud

import android.util.Log
import com.example.data.model.Observation
import com.example.data.model.ThreatReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class CloudAuthService {

    companion object {
        private const val TAG = "CloudAuthService"
        private const val BASE_URL = "https://api.restful-api.dev/objects"
        // Dedicated Master Registry for BioGuard users across devices
        private const val MASTER_REGISTRY_ID = "ff808181a067127101a08f47d01672b4"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun login(email: String, password: String): CloudAuthResult = withContext(Dispatchers.IO) {
        val normalizedEmail = email.trim().lowercase()
        if (normalizedEmail.isBlank() || password.isBlank()) {
            return@withContext CloudAuthResult.Error("Email and password cannot be empty.")
        }

        try {
            // 1. Fetch registry
            val registryRequest = Request.Builder()
                .url("$BASE_URL/$MASTER_REGISTRY_ID")
                .get()
                .build()

            val registryResponse = client.newCall(registryRequest).execute()
            if (!registryResponse.isSuccessful) {
                return@withContext CloudAuthResult.Error("Unable to connect to BioGuard Eco-Cloud (HTTP ${registryResponse.code}). Check your network.")
            }

            val registryBody = registryResponse.body?.string() ?: ""
            val registryJson = JSONObject(registryBody)
            val dataObj = registryJson.optJSONObject("data") ?: JSONObject()
            val accountsObj = dataObj.optJSONObject("accounts") ?: JSONObject()

            if (!accountsObj.has(normalizedEmail)) {
                return@withContext CloudAuthResult.Error("No account found for '$normalizedEmail'. Please verify email or tap Create Account.")
            }

            val userAccount = accountsObj.getJSONObject(normalizedEmail)
            val savedPassword = userAccount.optString("password", "")
            if (savedPassword != password) {
                return@withContext CloudAuthResult.Error("Incorrect password. Please try again.")
            }

            val userObjectId = userAccount.optString("objectId", "")
            if (userObjectId.isBlank()) {
                return@withContext CloudAuthResult.Error("Cloud profile record pointer is missing. Please contact support.")
            }

            // 2. Fetch User Cloud Data
            val userRequest = Request.Builder()
                .url("$BASE_URL/$userObjectId")
                .get()
                .build()

            val userResponse = client.newCall(userRequest).execute()
            if (!userResponse.isSuccessful) {
                // Return basic info from registry if detailed object fails
                val fallbackData = UserCloudData(
                    email = normalizedEmail,
                    name = userAccount.optString("name", "Eco Ranger"),
                    password = password,
                    points = 200,
                    level = 2
                )
                return@withContext CloudAuthResult.Success(fallbackData, userObjectId)
            }

            val userBody = userResponse.body?.string() ?: ""
            val userJson = JSONObject(userBody)
            val userData = parseUserCloudData(userJson.optJSONObject("data") ?: JSONObject(), normalizedEmail, userAccount.optString("name", "Eco Ranger"))

            Log.d(TAG, "Login successful for $normalizedEmail with ${userData.observations.size} observations and ${userData.threatReports.size} threats.")
            return@withContext CloudAuthResult.Success(userData, userObjectId)
        } catch (e: IOException) {
            Log.e(TAG, "Network error during login", e)
            return@withContext CloudAuthResult.Error("Network error: Please check your internet connection and try again.")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during login", e)
            return@withContext CloudAuthResult.Error("Authentication failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    suspend fun register(name: String, email: String, password: String): CloudAuthResult = withContext(Dispatchers.IO) {
        val normalizedEmail = email.trim().lowercase()
        val trimmedName = name.trim().ifBlank { "Eco Ranger" }

        if (normalizedEmail.isBlank() || password.length < 4) {
            return@withContext CloudAuthResult.Error("Please enter a valid email and password (minimum 4 characters).")
        }

        try {
            // 1. Fetch current registry to verify uniqueness
            val registryRequest = Request.Builder()
                .url("$BASE_URL/$MASTER_REGISTRY_ID")
                .get()
                .build()

            val registryResponse = client.newCall(registryRequest).execute()
            val registryJson = if (registryResponse.isSuccessful) {
                JSONObject(registryResponse.body?.string() ?: "{}")
            } else {
                JSONObject()
            }

            val dataObj = registryJson.optJSONObject("data") ?: JSONObject()
            val accountsObj = dataObj.optJSONObject("accounts") ?: JSONObject()

            if (accountsObj.has(normalizedEmail)) {
                return@withContext CloudAuthResult.Error("An account for '$normalizedEmail' already exists. Please Sign In instead.")
            }

            // 2. Create the User Data object in cloud
            val initialUserData = UserCloudData(
                email = normalizedEmail,
                name = trimmedName,
                password = password,
                points = 120,
                level = 1,
                observations = emptyList(),
                threatReports = emptyList(),
                unlockedAchievementIds = listOf("ach_first_discovery"),
                updatedAt = System.currentTimeMillis()
            )

            val createPayload = JSONObject().apply {
                put("name", "bioguard_userdata_$normalizedEmail")
                put("data", serializeUserCloudData(initialUserData))
            }

            val createRequest = Request.Builder()
                .url(BASE_URL)
                .post(createPayload.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val createResponse = client.newCall(createRequest).execute()
            if (!createResponse.isSuccessful) {
                return@withContext CloudAuthResult.Error("Failed to initialize cloud profile (HTTP ${createResponse.code}).")
            }

            val createdBody = createResponse.body?.string() ?: ""
            val createdJson = JSONObject(createdBody)
            val newObjectId = createdJson.optString("id", "")
            if (newObjectId.isBlank()) {
                return@withContext CloudAuthResult.Error("Cloud service did not return an account ID.")
            }

            // 3. Update Master Registry with new account pointer
            accountsObj.put(normalizedEmail, JSONObject().apply {
                put("objectId", newObjectId)
                put("password", password)
                put("email", normalizedEmail)
                put("name", trimmedName)
            })
            dataObj.put("accounts", accountsObj)

            val updateRegistryPayload = JSONObject().apply {
                put("name", "bioguard_cloud_registry_v1")
                put("data", dataObj)
            }

            val updateRegistryRequest = Request.Builder()
                .url("$BASE_URL/$MASTER_REGISTRY_ID")
                .put(updateRegistryPayload.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(updateRegistryRequest).execute()

            Log.d(TAG, "Registered new user: $normalizedEmail with objectId $newObjectId")
            return@withContext CloudAuthResult.Success(initialUserData, newObjectId, isNewAccount = true)
        } catch (e: IOException) {
            Log.e(TAG, "Network error during register", e)
            return@withContext CloudAuthResult.Error("Network error: Could not reach cloud server. Check your connection.")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during register", e)
            return@withContext CloudAuthResult.Error("Registration failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    suspend fun syncUserData(objectId: String, data: UserCloudData): CloudSyncResult = withContext(Dispatchers.IO) {
        if (objectId.isBlank()) {
            return@withContext CloudSyncResult.Error("No cloud object ID linked to this account.")
        }

        try {
            val payload = JSONObject().apply {
                put("name", "bioguard_userdata_${data.email}")
                put("data", serializeUserCloudData(data))
            }

            val request = Request.Builder()
                .url("$BASE_URL/$objectId")
                .put(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                return@withContext CloudSyncResult.Success(
                    message = "Synced successfully with BioGuard Cloud",
                    syncedCount = data.observations.size + data.threatReports.size
                )
            } else {
                return@withContext CloudSyncResult.Error("Sync failed (HTTP ${response.code})")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync error", e)
            return@withContext CloudSyncResult.Error(e.localizedMessage ?: "Cloud sync failed")
        }
    }

    suspend fun pullUserData(objectId: String): UserCloudData? = withContext(Dispatchers.IO) {
        if (objectId.isBlank()) return@withContext null
        try {
            val request = Request.Builder()
                .url("$BASE_URL/$objectId")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val body = response.body?.string() ?: return@withContext null
            val json = JSONObject(body)
            val dataObj = json.optJSONObject("data") ?: return@withContext null
            return@withContext parseUserCloudData(dataObj, dataObj.optString("email", ""), dataObj.optString("name", "Eco Ranger"))
        } catch (e: Exception) {
            Log.e(TAG, "Error pulling user data", e)
            return@withContext null
        }
    }

    private fun serializeUserCloudData(data: UserCloudData): JSONObject {
        return JSONObject().apply {
            put("email", data.email)
            put("name", data.name)
            put("password", data.password)
            put("points", data.points)
            put("level", data.level)
            put("updatedAt", System.currentTimeMillis())

            // Observations
            val obsArray = JSONArray()
            data.observations.forEach { obs ->
                if (!obs.isDemo) {
                    obsArray.put(JSONObject().apply {
                        put("commonName", obs.commonName)
                        put("scientificName", obs.scientificName)
                        put("category", obs.category)
                        put("confidence", obs.confidence)
                        put("description", obs.description)
                        put("identifyingFeatures", obs.identifyingFeatures)
                        put("habitat", obs.habitat)
                        put("threats", obs.threats)
                        put("conservationStatus", obs.conservationStatus)
                        put("conservationAdvice", obs.conservationAdvice)
                        put("ecologicalRole", obs.ecologicalRole)
                        put("imageUri", obs.imageUri)
                        put("location", obs.location)
                        put("createdAt", obs.createdAt)
                    })
                }
            }
            put("observations", obsArray)

            // Threat Reports
            val threatArray = JSONArray()
            data.threatReports.forEach { tr ->
                if (!tr.isDemo) {
                    threatArray.put(JSONObject().apply {
                        put("reportCode", tr.reportCode)
                        put("threatType", tr.threatType)
                        put("title", tr.title)
                        put("description", tr.description)
                        put("location", tr.location)
                        put("dateStr", tr.dateStr)
                        put("severity", tr.severity)
                        put("imageUri", tr.imageUri ?: "")
                        put("status", tr.status)
                        put("createdAt", tr.createdAt)
                    })
                }
            }
            put("threatReports", threatArray)

            // Achievements
            val achArray = JSONArray()
            data.unlockedAchievementIds.forEach { id ->
                achArray.put(id)
            }
            put("achievements", achArray)
        }
    }

    private fun parseUserCloudData(dataObj: JSONObject, fallbackEmail: String, fallbackName: String): UserCloudData {
        val email = dataObj.optString("email", fallbackEmail)
        val name = dataObj.optString("name", fallbackName)
        val password = dataObj.optString("password", "")
        val points = dataObj.optInt("points", 100)
        val level = dataObj.optInt("level", 1)
        val updatedAt = dataObj.optLong("updatedAt", System.currentTimeMillis())

        val obsList = mutableListOf<Observation>()
        val obsArray = dataObj.optJSONArray("observations")
        if (obsArray != null) {
            for (i in 0 until obsArray.length()) {
                val item = obsArray.optJSONObject(i) ?: continue
                obsList.add(
                    Observation(
                        commonName = item.optString("commonName", "Observed Species"),
                        scientificName = item.optString("scientificName", ""),
                        category = item.optString("category", "Fauna"),
                        confidence = item.optInt("confidence", 90),
                        description = item.optString("description", ""),
                        identifyingFeatures = item.optString("identifyingFeatures", ""),
                        habitat = item.optString("habitat", ""),
                        threats = item.optString("threats", ""),
                        conservationStatus = item.optString("conservationStatus", "Least Concern"),
                        conservationAdvice = item.optString("conservationAdvice", ""),
                        ecologicalRole = item.optString("ecologicalRole", ""),
                        imageUri = item.optString("imageUri", ""),
                        location = item.optString("location", "India"),
                        createdAt = item.optLong("createdAt", System.currentTimeMillis()),
                        isDemo = false
                    )
                )
            }
        }

        val threatList = mutableListOf<ThreatReport>()
        val threatArray = dataObj.optJSONArray("threatReports")
        if (threatArray != null) {
            for (i in 0 until threatArray.length()) {
                val item = threatArray.optJSONObject(i) ?: continue
                threatList.add(
                    ThreatReport(
                        reportCode = item.optString("reportCode", "BG-${System.currentTimeMillis() % 10000}"),
                        threatType = item.optString("threatType", "Habitat Loss"),
                        title = item.optString("title", "Reported Incident"),
                        description = item.optString("description", ""),
                        location = item.optString("location", "Field Location"),
                        dateStr = item.optString("dateStr", "Recent"),
                        severity = item.optString("severity", "Medium"),
                        imageUri = item.optString("imageUri").takeIf { it.isNotBlank() },
                        status = item.optString("status", "Under Review"),
                        createdAt = item.optLong("createdAt", System.currentTimeMillis()),
                        isDemo = false
                    )
                )
            }
        }

        val achIds = mutableListOf<String>()
        val achArray = dataObj.optJSONArray("achievements")
        if (achArray != null) {
            for (i in 0 until achArray.length()) {
                achIds.add(achArray.optString(i))
            }
        }

        return UserCloudData(
            email = email,
            name = name,
            password = password,
            points = points,
            level = level,
            observations = obsList,
            threatReports = threatList,
            unlockedAchievementIds = achIds,
            updatedAt = updatedAt
        )
    }
}
