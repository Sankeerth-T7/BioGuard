package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.cloud.CloudAuthResult
import com.example.data.cloud.CloudAuthService
import com.example.data.cloud.CloudSyncResult
import com.example.data.cloud.UserCloudData
import com.example.data.local.AppDatabase
import com.example.data.model.Achievement
import com.example.data.model.BioLocation
import com.example.data.model.ConservationTip
import com.example.data.model.Observation
import com.example.data.model.QuizQuestion
import com.example.data.model.Species
import com.example.data.model.ThreatReport
import com.example.data.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

import com.example.data.firebase.FirebaseAuthManager
import com.example.data.firebase.FirebaseSyncManager
import com.google.firebase.auth.FirebaseUser

class BioGuardRepository(context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val observationDao = db.observationDao()
    private val threatReportDao = db.threatReportDao()
    private val cloudAuthService = CloudAuthService()
    val firebaseAuthManager = FirebaseAuthManager(context)
    val firebaseSyncManager = FirebaseSyncManager()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val prefs: SharedPreferences =
        context.getSharedPreferences("bioguard_user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val PREF_IS_LOGGED_IN = "is_logged_in"
        private const val PREF_EMAIL = "user_email"
        private const val PREF_NAME = "user_name"
        private const val PREF_OBJECT_ID = "user_object_id"
        private const val PREF_FIREBASE_UID = "firebase_uid"
        private const val PREF_AUTH_PROVIDER = "auth_provider"
        private const val PREF_IS_GUEST = "is_guest"
        private const val PREF_POINTS = "user_points"
        private const val PREF_LEVEL = "user_level"
        private const val PREF_LAST_SYNC = "last_sync_timestamp"
        private const val PREF_UNLOCKED_ACHIEVEMENTS = "unlocked_achievements_set"
    }

    private var currentObjectId: String? = prefs.getString(PREF_OBJECT_ID, null)
    private var currentFirebaseUid: String? = prefs.getString(PREF_FIREBASE_UID, null)

    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean(PREF_IS_LOGGED_IN, false))
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(prefs.getLong(PREF_LAST_SYNC, 0L))
    val lastSyncTime = _lastSyncTime.asStateFlow()

    private val _cloudStatusMessage = MutableStateFlow("Ready")
    val cloudStatusMessage = _cloudStatusMessage.asStateFlow()

    private val _isDemoMode = MutableStateFlow(false)
    val isDemoMode = _isDemoMode.asStateFlow()

    private val _darkThemeOverride = MutableStateFlow<Boolean?>(null)
    val darkThemeOverride = _darkThemeOverride.asStateFlow()

    private val _userProfile = MutableStateFlow(
        UserProfile(
            name = prefs.getString(PREF_NAME, "Eco Explorer") ?: "Eco Explorer",
            email = prefs.getString(PREF_EMAIL, "explorer@bioguard.eco") ?: "explorer@bioguard.eco",
            points = prefs.getInt(PREF_POINTS, 100),
            level = prefs.getInt(PREF_LEVEL, 1),
            isGuest = prefs.getBoolean(PREF_IS_GUEST, false),
            uid = prefs.getString(PREF_FIREBASE_UID, null),
            authProvider = prefs.getString(PREF_AUTH_PROVIDER, "Firebase Auth") ?: "Firebase Auth"
        )
    )
    val userProfile = _userProfile.asStateFlow()

    private val savedAchievements = prefs.getStringSet(PREF_UNLOCKED_ACHIEVEMENTS, emptySet()) ?: emptySet()
    private val _achievements = MutableStateFlow(
        BioSeedData.achievements.map { ach ->
            if (savedAchievements.contains(ach.id)) ach.copy(isUnlocked = true, progress = 1f) else ach
        }
    )
    val achievements = _achievements.asStateFlow()

    private val _customTips = MutableStateFlow<List<ConservationTip>>(emptyList())
    val customTips = _customTips.asStateFlow()

    private var authStateListener: com.google.firebase.auth.FirebaseAuth.AuthStateListener? = null

    init {
        // Setup Firebase Auth state listener for automatic cross-device sync
        setupFirebaseAuthStateListener()

        // If user is already authenticated, perform automatic background pull from Firestore / Cloud
        if (_isLoggedIn.value && !userProfile.value.isGuest) {
            scope.launch {
                val uid = currentFirebaseUid ?: userProfile.value.uid
                if (!uid.isNullOrBlank()) {
                    syncFromFirestore(uid)
                } else if (!currentObjectId.isNullOrBlank()) {
                    syncFromCloud()
                }
            }
        }
    }

    /**
     * Firebase Auth State Listener:
     * When a user logs in on a new or existing device, automatically sync their
     * profile data, species observations, and achievements from Firestore.
     */
    private fun setupFirebaseAuthStateListener() {
        authStateListener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            Log.d("BioGuardRepo", "FirebaseAuth AuthStateListener triggered: uid=${user?.uid}, email=${user?.email}, isAnon=${user?.isAnonymous}")
            if (user != null && !user.isAnonymous) {
                val uid = user.uid
                currentFirebaseUid = uid
                scope.launch {
                    syncFromFirestore(uid, user)
                }
            }
        }
        authStateListener?.let { firebaseAuthManager.addAuthStateListener(it) }
    }

    fun toggleDarkTheme(isSystemDark: Boolean = false) {
        val current = _darkThemeOverride.value
        _darkThemeOverride.value = if (current == null) !isSystemDark else !current
    }

    fun setDarkTheme(isDark: Boolean?) {
        _darkThemeOverride.value = isDark
    }

    fun setDarkThemeOverride(isDark: Boolean?) {
        _darkThemeOverride.value = isDark
    }

    // Combined Observations: If demo mode is on, merge seed observations with user observations
    fun getObservations(): Flow<List<Observation>> {
        return observationDao.getAllObservations().map { userList ->
            if (_isDemoMode.value) {
                (userList + BioSeedData.sampleObservations).sortedByDescending { it.createdAt }
            } else {
                userList
            }
        }
    }

    // Combined Threat Reports
    fun getThreatReports(): Flow<List<ThreatReport>> {
        return threatReportDao.getAllReports().map { userList ->
            if (_isDemoMode.value) {
                (userList + BioSeedData.sampleThreatReports).sortedByDescending { it.createdAt }
            } else {
                userList
            }
        }
    }

    suspend fun saveObservation(obs: Observation): Long {
        val id = observationDao.insert(obs)
        addPoints(50)
        checkAchievements()
        scope.launch { syncToCloud() }
        return id
    }

    suspend fun deleteObservation(obs: Observation) {
        if (!obs.isDemo) {
            observationDao.delete(obs)
            scope.launch { syncToCloud() }
        }
    }

    suspend fun saveThreatReport(report: ThreatReport): Long {
        val id = threatReportDao.insert(report)
        addPoints(35)
        checkAchievements()
        scope.launch { syncToCloud() }
        return id
    }

    fun toggleDemoMode() {
        _isDemoMode.value = !_isDemoMode.value
    }

    fun addPoints(pts: Int) {
        val current = _userProfile.value
        val newPoints = current.points + pts
        val newLevel = calculateLevel(newPoints)
        _userProfile.value = current.copy(points = newPoints, level = newLevel)
        prefs.edit()
            .putInt(PREF_POINTS, newPoints)
            .putInt(PREF_LEVEL, newLevel)
            .apply()
        checkAchievements()
        scope.launch { syncToCloud() }
    }

    private fun calculateLevel(points: Int): Int {
        return when {
            points >= 500 -> 5 // Conservation Champion
            points >= 350 -> 4 // Wildlife Sentinel
            points >= 200 -> 3 // Flora & Fauna Warden
            points >= 100 -> 2 // Habitat Scout
            else -> 1 // Novice Ranger
        }
    }

    private fun checkAchievements() {
        val currentPts = _userProfile.value.points
        val updated = _achievements.value.map { ach ->
            when (ach.id) {
                "ach_conservation_champion" -> {
                    val prog = (currentPts / 500f).coerceIn(0f, 1f)
                    ach.copy(isUnlocked = currentPts >= 500, progress = prog)
                }
                "ach_first_discovery" -> ach.copy(isUnlocked = true, progress = 1f)
                else -> ach
            }
        }
        _achievements.value = updated
        val unlockedSet = updated.filter { it.isUnlocked }.map { it.id }.toSet()
        prefs.edit().putStringSet(PREF_UNLOCKED_ACHIEVEMENTS, unlockedSet).apply()
    }

    fun addCustomTip(tip: ConservationTip) {
        _customTips.value = listOf(tip) + _customTips.value
        addPoints(10)
    }

    fun updateProfile(name: String, email: String, isGuest: Boolean) {
        _userProfile.value = _userProfile.value.copy(
            name = name,
            email = email,
            isGuest = isGuest
        )
    }

    // ==========================================
    // CLOUD AUTHENTICATION & CROSS-DEVICE SYNC
    // ==========================================

    suspend fun login(email: String, password: String): Result<UserProfile> {
        _isSyncing.value = true
        _cloudStatusMessage.value = "Authenticating with Firebase Auth..."

        // 1. Authenticate with Firebase Authentication
        val firebaseResult = firebaseAuthManager.signInWithEmailAndPassword(email, password)
        var firebaseUid: String? = firebaseResult.getOrNull()?.uid

        // 2. Also authenticate/verify with Cloud Auth Service for full cross-device record sync
        val cloudResult = cloudAuthService.login(email, password)
        _isSyncing.value = false

        if (firebaseResult.isSuccess || cloudResult is CloudAuthResult.Success) {
            val userCloudData = (cloudResult as? CloudAuthResult.Success)?.userCloudData
            val objectId = (cloudResult as? CloudAuthResult.Success)?.objectId ?: currentObjectId
            currentObjectId = objectId
            currentFirebaseUid = firebaseUid ?: prefs.getString(PREF_FIREBASE_UID, null)

            // If user logged into Firebase but wasn't in cloud service, initialize cloud record
            if (cloudResult is CloudAuthResult.Error && firebaseResult.isSuccess) {
                val fbUser = firebaseResult.getOrNull()!!
                val displayName = fbUser.displayName?.ifBlank { null } ?: fbUser.email?.substringBefore("@") ?: "Eco Explorer"
                val regCloud = cloudAuthService.register(displayName, email, password)
                if (regCloud is CloudAuthResult.Success) {
                    currentObjectId = regCloud.objectId
                }
            }

            // Automatically sync profile data, species observations, and achievements from Firestore
            if (!currentFirebaseUid.isNullOrBlank()) {
                val synced = syncFromFirestore(currentFirebaseUid!!)
                if (synced) {
                    _cloudStatusMessage.value = "Connected & Synced (Firestore)"
                    return Result.success(_userProfile.value)
                }
            }

            // Populate local Room database from cloud service data if available
            if (userCloudData != null) {
                try {
                    observationDao.clearAll()
                    if (userCloudData.observations.isNotEmpty()) {
                        observationDao.insertAll(userCloudData.observations)
                    }
                    threatReportDao.clearAll()
                    if (userCloudData.threatReports.isNotEmpty()) {
                        threatReportDao.insertAll(userCloudData.threatReports)
                    }
                } catch (e: Exception) {
                    Log.e("BioGuardRepo", "Failed to cache cloud records to Room DB", e)
                }

                val newProfile = UserProfile(
                    name = userCloudData.name,
                    email = userCloudData.email,
                    points = userCloudData.points,
                    level = userCloudData.level,
                    isGuest = false,
                    uid = currentFirebaseUid,
                    authProvider = "Firebase Email"
                )
                _userProfile.value = newProfile

                // Sync achievements
                _achievements.value = _achievements.value.map { ach ->
                    if (userCloudData.unlockedAchievementIds.contains(ach.id)) {
                        ach.copy(isUnlocked = true, progress = 1f)
                    } else {
                        ach
                    }
                }

                saveSession(
                    name = userCloudData.name,
                    email = userCloudData.email,
                    objectId = objectId,
                    firebaseUid = currentFirebaseUid,
                    authProvider = "Firebase Email",
                    isGuest = false,
                    points = userCloudData.points,
                    level = userCloudData.level
                )

                _isLoggedIn.value = true
                _lastSyncTime.value = System.currentTimeMillis()
                _cloudStatusMessage.value = "Connected & Synced"
                return Result.success(newProfile)
            } else {
                val fbUser = firebaseResult.getOrNull()
                val name = fbUser?.displayName ?: email.substringBefore("@")
                val newProfile = UserProfile(
                    name = name,
                    email = email,
                    points = 150,
                    level = 2,
                    isGuest = false,
                    uid = currentFirebaseUid,
                    authProvider = "Firebase Email"
                )
                _userProfile.value = newProfile
                saveSession(name, email, currentObjectId, currentFirebaseUid, "Firebase Email", false, 150, 2)
                _isLoggedIn.value = true
                _lastSyncTime.value = System.currentTimeMillis()
                _cloudStatusMessage.value = "Connected (Firebase Auth)"
                return Result.success(newProfile)
            }
        } else {
            val err = firebaseResult.exceptionOrNull()?.localizedMessage
                ?: (cloudResult as? CloudAuthResult.Error)?.message
                ?: "Authentication failed"
            _cloudStatusMessage.value = "Auth failed: $err"
            return Result.failure(Exception(err))
        }
    }

    suspend fun register(name: String, email: String, password: String): Result<UserProfile> {
        _isSyncing.value = true
        _cloudStatusMessage.value = "Registering with Firebase Auth..."

        // 1. Register with Firebase Authentication
        val firebaseResult = firebaseAuthManager.createUserWithEmailAndPassword(name, email, password)
        val firebaseUid = firebaseResult.getOrNull()?.uid
        currentFirebaseUid = firebaseUid

        // 2. Also register in Cloud Registry
        val cloudResult = cloudAuthService.register(name, email, password)
        _isSyncing.value = false

        if (firebaseResult.isSuccess || cloudResult is CloudAuthResult.Success) {
            val objectId = (cloudResult as? CloudAuthResult.Success)?.objectId ?: currentObjectId
            currentObjectId = objectId

            try {
                observationDao.clearAll()
                threatReportDao.clearAll()
            } catch (e: Exception) {
                Log.e("BioGuardRepo", "Clear DB error", e)
            }

            val newProfile = UserProfile(
                name = name,
                email = email,
                points = 100,
                level = 1,
                isGuest = false,
                uid = firebaseUid,
                authProvider = "Firebase Email"
            )
            _userProfile.value = newProfile

            saveSession(
                name = name,
                email = email,
                objectId = objectId,
                firebaseUid = firebaseUid,
                authProvider = "Firebase Email",
                isGuest = false,
                points = 100,
                level = 1
            )

            // Initial sync to Firestore
            if (!firebaseUid.isNullOrBlank()) {
                scope.launch {
                    firebaseSyncManager.syncUserToFirestore(
                        userId = firebaseUid,
                        email = email,
                        name = name,
                        points = 100,
                        level = 1,
                        observations = emptyList(),
                        threatReports = emptyList(),
                        unlockedAchievementIds = emptyList()
                    )
                }
            }

            _isLoggedIn.value = true
            _lastSyncTime.value = System.currentTimeMillis()
            _cloudStatusMessage.value = "Account created & synced (Firebase)"
            return Result.success(newProfile)
        } else {
            val err = firebaseResult.exceptionOrNull()?.localizedMessage
                ?: (cloudResult as? CloudAuthResult.Error)?.message
                ?: "Registration failed"
            _cloudStatusMessage.value = "Registration failed: $err"
            return Result.failure(Exception(err))
        }
    }

    suspend fun signInWithGoogle(activityContext: Context): Result<UserProfile> {
        _isSyncing.value = true
        _cloudStatusMessage.value = "Signing in with Google Credential Manager..."

        val result = firebaseAuthManager.signInWithGoogleCredentialManager(activityContext)
        _isSyncing.value = false

        return if (result.isSuccess) {
            val fbUser = result.getOrNull()!!
            val displayName = fbUser.displayName ?: fbUser.email?.substringBefore("@") ?: "Google Scout"
            val email = fbUser.email ?: "google.user@bioguard.eco"
            val uid = fbUser.uid
            val photoUrl = fbUser.photoUrl?.toString()
            currentFirebaseUid = uid

            // Automatically sync profile data, species observations, and achievements from Firestore
            syncFromFirestore(uid, fbUser)

            _cloudStatusMessage.value = "Signed in with Google ✓"
            Result.success(_userProfile.value)
        } else {
            val err = result.exceptionOrNull()?.localizedMessage ?: "Google Sign-In failed"
            _cloudStatusMessage.value = err
            Result.failure(Exception(err))
        }
    }

    fun continueAsGuest() {
        val guestProfile = UserProfile(
            name = "Guest Explorer",
            email = "guest@bioguard.eco",
            points = 100,
            level = 1,
            isGuest = true,
            authProvider = "Guest Mode"
        )
        _userProfile.value = guestProfile
        currentObjectId = null
        currentFirebaseUid = null
        saveSession(
            name = guestProfile.name,
            email = guestProfile.email,
            objectId = null,
            firebaseUid = null,
            authProvider = "Guest Mode",
            isGuest = true,
            points = guestProfile.points,
            level = guestProfile.level
        )
        _isLoggedIn.value = true
        _cloudStatusMessage.value = "Guest mode (Offline)"

        // Also initiate Firebase anonymous authentication silently
        scope.launch {
            try {
                firebaseAuthManager.signInAnonymously()
            } catch (e: Exception) {
                Log.i("BioGuardRepo", "Anonymous auth optional in guest mode")
            }
        }
    }

    fun logout() {
        firebaseAuthManager.signOut()
        prefs.edit().clear().apply()
        currentObjectId = null
        currentFirebaseUid = null
        _isLoggedIn.value = false
        _userProfile.value = UserProfile(
            name = "Eco Scout",
            email = "scout@bioguard.eco",
            points = 100,
            level = 1,
            isGuest = false
        )
        // Reset achievements to default
        _achievements.value = BioSeedData.achievements
        _cloudStatusMessage.value = "Logged out"
    }

    /**
     * Automatically syncs profile data, species observations, and achievements from Firestore.
     * Invoked automatically by the Firebase Auth state listener upon login or device switch.
     */
    suspend fun syncFromFirestore(uid: String, firebaseUser: com.google.firebase.auth.FirebaseUser? = null): Boolean {
        if (uid.isBlank()) return false
        _isSyncing.value = true
        _cloudStatusMessage.value = "Syncing from Firestore..."
        Log.i("BioGuardRepo", "Starting automatic Firestore sync for UID: $uid")

        try {
            val user = firebaseUser ?: firebaseAuthManager.getCurrentUser()
            val firestoreData = firebaseSyncManager.fetchUserFromFirestore(uid)

            if (firestoreData != null) {
                // 1. Sync Species Observations into local Room database
                try {
                    observationDao.clearAll()
                    if (firestoreData.observations.isNotEmpty()) {
                        observationDao.insertAll(firestoreData.observations)
                    }
                    Log.d("BioGuardRepo", "Auto-synced ${firestoreData.observations.size} species observations from Firestore to Room")
                } catch (e: Exception) {
                    Log.e("BioGuardRepo", "Error caching species observations to Room", e)
                }

                // 2. Sync Threat Reports into local Room database
                try {
                    threatReportDao.clearAll()
                    if (firestoreData.threatReports.isNotEmpty()) {
                        threatReportDao.insertAll(firestoreData.threatReports)
                    }
                } catch (e: Exception) {
                    Log.e("BioGuardRepo", "Error caching threat reports to Room", e)
                }

                // 3. Sync Achievements
                val unlockedSet = firestoreData.achievements.toSet()
                _achievements.value = _achievements.value.map { ach ->
                    if (unlockedSet.contains(ach.id)) {
                        ach.copy(isUnlocked = true, progress = 1f)
                    } else {
                        ach
                    }
                }
                prefs.edit().putStringSet(PREF_UNLOCKED_ACHIEVEMENTS, unlockedSet).apply()

                // 4. Sync User Profile Data
                val displayName = firestoreData.name.ifBlank {
                    user?.displayName?.ifBlank { null }
                        ?: user?.email?.substringBefore("@")
                        ?: _userProfile.value.name
                }
                val email = firestoreData.email.ifBlank {
                    user?.email ?: _userProfile.value.email
                }
                val points = firestoreData.points
                val level = calculateLevel(points).coerceAtLeast(firestoreData.level)
                val provider = user?.let {
                    if (it.providerData.any { p -> p.providerId.contains("google") }) "Google Sign-In"
                    else "Firebase Email"
                } ?: _userProfile.value.authProvider ?: "Firebase Auth"

                val updatedProfile = UserProfile(
                    name = displayName,
                    email = email,
                    points = points,
                    level = level,
                    isGuest = false,
                    uid = uid,
                    photoUrl = user?.photoUrl?.toString() ?: _userProfile.value.photoUrl,
                    authProvider = provider
                )
                _userProfile.value = updatedProfile

                saveSession(
                    name = displayName,
                    email = email,
                    objectId = currentObjectId,
                    firebaseUid = uid,
                    authProvider = provider,
                    isGuest = false,
                    points = points,
                    level = level
                )

                _isLoggedIn.value = true
                _lastSyncTime.value = System.currentTimeMillis()
                _cloudStatusMessage.value = "Synced: ${firestoreData.observations.size} species, ${firestoreData.achievements.size} badges ✓"
                Log.i("BioGuardRepo", "Auto-sync complete: profile, ${firestoreData.observations.size} observations, ${firestoreData.achievements.size} achievements")
                return true
            } else {
                // First-time login: initialize remote Firestore record with local seed/records
                Log.i("BioGuardRepo", "User record not yet in Firestore. Creating initial cloud profile for UID: $uid")
                val displayName = user?.displayName?.ifBlank { null }
                    ?: user?.email?.substringBefore("@")
                    ?: _userProfile.value.name
                val email = user?.email ?: _userProfile.value.email
                val localObs = observationDao.getAllObservations().first()
                val localThreats = threatReportDao.getAllReports().first()
                val unlockedIds = _achievements.value.filter { it.isUnlocked }.map { it.id }

                firebaseSyncManager.syncUserToFirestore(
                    userId = uid,
                    email = email,
                    name = displayName,
                    points = _userProfile.value.points,
                    level = _userProfile.value.level,
                    observations = localObs,
                    threatReports = localThreats,
                    unlockedAchievementIds = unlockedIds
                )

                val provider = user?.let {
                    if (it.providerData.any { p -> p.providerId.contains("google") }) "Google Sign-In"
                    else "Firebase Email"
                } ?: "Firebase Auth"

                _userProfile.value = _userProfile.value.copy(
                    name = displayName,
                    email = email,
                    uid = uid,
                    authProvider = provider,
                    isGuest = false
                )
                saveSession(
                    name = displayName,
                    email = email,
                    objectId = currentObjectId,
                    firebaseUid = uid,
                    authProvider = provider,
                    isGuest = false,
                    points = _userProfile.value.points,
                    level = _userProfile.value.level
                )
                _isLoggedIn.value = true
                _lastSyncTime.value = System.currentTimeMillis()
                _cloudStatusMessage.value = "Firestore profile initialized ✓"
                return true
            }
        } catch (e: Exception) {
            Log.e("BioGuardRepo", "Error syncing from Firestore for user $uid", e)
            _cloudStatusMessage.value = "Firestore sync error: ${e.message}"
        } finally {
            _isSyncing.value = false
        }
        return false
    }

    suspend fun syncFromCloud(): Boolean {
        if (_userProfile.value.isGuest) return false
        _isSyncing.value = true
        _cloudStatusMessage.value = "Checking cloud updates..."
        try {
            var syncedAny = false

            // 1. Try pulling from Firestore first if UID is present
            val uid = currentFirebaseUid ?: _userProfile.value.uid
            if (!uid.isNullOrBlank()) {
                val fsSuccess = syncFromFirestore(uid)
                if (fsSuccess) syncedAny = true
            }

            // 2. Also check legacy cloud service registry
            val objectId = currentObjectId
            if (!objectId.isNullOrBlank()) {
                val remoteData = cloudAuthService.pullUserData(objectId)
                if (remoteData != null) {
                    if (!syncedAny) {
                        observationDao.clearAll()
                        if (remoteData.observations.isNotEmpty()) {
                            observationDao.insertAll(remoteData.observations)
                        }
                        threatReportDao.clearAll()
                        if (remoteData.threatReports.isNotEmpty()) {
                            threatReportDao.insertAll(remoteData.threatReports)
                        }
                        _userProfile.value = _userProfile.value.copy(
                            name = remoteData.name,
                            email = remoteData.email,
                            points = remoteData.points,
                            level = remoteData.level
                        )
                        val unlockedSet = remoteData.unlockedAchievementIds.toSet()
                        _achievements.value = _achievements.value.map { ach ->
                            if (unlockedSet.contains(ach.id)) ach.copy(isUnlocked = true, progress = 1f) else ach
                        }
                        prefs.edit().putStringSet(PREF_UNLOCKED_ACHIEVEMENTS, unlockedSet).apply()
                    }
                    syncedAny = true
                }
            }

            if (syncedAny) {
                _lastSyncTime.value = System.currentTimeMillis()
                _cloudStatusMessage.value = "Synced with Firebase & Cloud ✓"
                return true
            }
        } catch (e: Exception) {
            Log.e("BioGuardRepo", "Sync from cloud failed", e)
            _cloudStatusMessage.value = "Sync failed"
        } finally {
            _isSyncing.value = false
        }
        return false
    }

    suspend fun syncToCloud(): Boolean {
        if (_userProfile.value.isGuest) return false
        _isSyncing.value = true
        try {
            val localObs = observationDao.getAllObservations().first()
            val localThreats = threatReportDao.getAllReports().first()
            var success = false

            // 1. Sync to Firestore
            val uid = currentFirebaseUid ?: _userProfile.value.uid
            if (!uid.isNullOrBlank()) {
                val fsSuccess = firebaseSyncManager.syncUserToFirestore(
                    userId = uid,
                    email = _userProfile.value.email,
                    name = _userProfile.value.name,
                    points = _userProfile.value.points,
                    level = _userProfile.value.level,
                    observations = localObs,
                    threatReports = localThreats,
                    unlockedAchievementIds = _achievements.value.filter { it.isUnlocked }.map { it.id }
                )
                if (fsSuccess) success = true
            }

            // 2. Sync to Cloud Registry
            val objectId = currentObjectId
            if (!objectId.isNullOrBlank()) {
                val cloudData = UserCloudData(
                    email = _userProfile.value.email,
                    name = _userProfile.value.name,
                    points = _userProfile.value.points,
                    level = _userProfile.value.level,
                    observations = localObs.filter { !it.isDemo },
                    threatReports = localThreats.filter { !it.isDemo },
                    unlockedAchievementIds = _achievements.value.filter { it.isUnlocked }.map { it.id },
                    updatedAt = System.currentTimeMillis()
                )

                val res = cloudAuthService.syncUserData(objectId, cloudData)
                if (res is CloudSyncResult.Success) {
                    success = true
                }
            }

            if (success) {
                _lastSyncTime.value = System.currentTimeMillis()
                prefs.edit().putLong(PREF_LAST_SYNC, _lastSyncTime.value).apply()
                _cloudStatusMessage.value = "Cloud up to date"
                return true
            } else {
                _cloudStatusMessage.value = "Sync pending"
            }
        } catch (e: Exception) {
            Log.e("BioGuardRepo", "Sync to cloud failed", e)
            _cloudStatusMessage.value = "Sync failed"
        } finally {
            _isSyncing.value = false
        }
        return false
    }

    private fun saveSession(
        name: String,
        email: String,
        objectId: String?,
        firebaseUid: String?,
        authProvider: String,
        isGuest: Boolean,
        points: Int,
        level: Int
    ) {
        prefs.edit()
            .putBoolean(PREF_IS_LOGGED_IN, true)
            .putString(PREF_NAME, name)
            .putString(PREF_EMAIL, email)
            .putString(PREF_OBJECT_ID, objectId)
            .putString(PREF_FIREBASE_UID, firebaseUid)
            .putString(PREF_AUTH_PROVIDER, authProvider)
            .putBoolean(PREF_IS_GUEST, isGuest)
            .putInt(PREF_POINTS, points)
            .putInt(PREF_LEVEL, level)
            .putLong(PREF_LAST_SYNC, System.currentTimeMillis())
            .apply()
    }

    fun getSpeciesCatalog(): List<Species> = BioSeedData.speciesList

    fun getLocations(): List<BioLocation> = BioSeedData.locations

    fun getTips(): List<ConservationTip> = BioSeedData.conservationTips + _customTips.value

    fun getQuizQuestions(count: Int = 10): List<QuizQuestion> {
        return BioSeedData.quizQuestions.shuffled().take(count)
    }
}
