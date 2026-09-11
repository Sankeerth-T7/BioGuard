package com.example.data.firebase

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseAuthManager(private val context: Context) {

    companion object {
        private const val TAG = "FirebaseAuthManager"
        // Default Web Client ID for Google Sign-In with Credential Manager
        const val DEFAULT_WEB_CLIENT_ID = "842196312107-default.apps.googleusercontent.com"
    }

    private var firebaseAuth: FirebaseAuth? = null

    private val _currentUserState = MutableStateFlow<FirebaseUser?>(null)
    val currentUserState = _currentUserState.asStateFlow()

    private val _authProviderName = MutableStateFlow<String>("Email")
    val authProviderName = _authProviderName.asStateFlow()

    init {
        initFirebase()
    }

    private fun initFirebase() {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:842196312107:android:com.aistudio.bioguard")
                    .setApiKey("AIzaSyBioGuardMobileClientDefaultKey")
                    .setProjectId("bioguard-eco")
                    .build()
                FirebaseApp.initializeApp(context, options)
                Log.d(TAG, "Initialized default FirebaseApp")
            }
            firebaseAuth = FirebaseAuth.getInstance().apply {
                _currentUserState.value = currentUser
                addAuthStateListener { auth ->
                    _currentUserState.value = auth.currentUser
                    updateProvider(auth.currentUser)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase Auth: ${e.message}", e)
        }
    }

    private fun updateProvider(user: FirebaseUser?) {
        if (user == null) {
            _authProviderName.value = "None"
            return
        }
        if (user.isAnonymous) {
            _authProviderName.value = "Guest (Anonymous)"
            return
        }
        val isGoogle = user.providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID }
        _authProviderName.value = if (isGoogle) "Google Sign-In" else "Firebase Email/Password"
    }

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth?.currentUser ?: _currentUserState.value
    }

    fun getFirebaseAuthInstance(): FirebaseAuth? = firebaseAuth

    fun addAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        firebaseAuth?.addAuthStateListener(listener)
    }

    fun removeAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        firebaseAuth?.removeAuthStateListener(listener)
    }

    /**
     * Cold Flow of FirebaseUser that emits whenever authentication state changes
     */
    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        val currentAuth = firebaseAuth
        if (currentAuth != null) {
            currentAuth.addAuthStateListener(listener)
            trySend(currentAuth.currentUser)
        } else {
            trySend(null)
        }
        awaitClose {
            firebaseAuth?.removeAuthStateListener(listener)
        }
    }


    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<FirebaseUser> =
        withContext(Dispatchers.IO) {
            val auth = firebaseAuth ?: return@withContext Result.failure(
                IllegalStateException("Firebase Auth is not initialized.")
            )
            try {
                val authResult = auth.signInWithEmailAndPassword(email.trim(), password).awaitResult()
                val user = authResult.user ?: return@withContext Result.failure(
                    IllegalStateException("No user returned from Firebase.")
                )
                _currentUserState.value = user
                updateProvider(user)
                Result.success(user)
            } catch (e: Exception) {
                Log.e(TAG, "Firebase signInWithEmailAndPassword failed", e)
                Result.failure(e)
            }
        }

    suspend fun createUserWithEmailAndPassword(
        name: String,
        email: String,
        password: String
    ): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val auth = firebaseAuth ?: return@withContext Result.failure(
            IllegalStateException("Firebase Auth is not initialized.")
        )
        try {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).awaitResult()
            val user = authResult.user ?: return@withContext Result.failure(
                IllegalStateException("User creation failed.")
            )

            // Update display name
            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name.trim())
                    .build()
                user.updateProfile(profileUpdates).awaitResult()
            } catch (e: Exception) {
                Log.w(TAG, "Could not update display name in Firebase profile", e)
            }

            _currentUserState.value = user
            updateProvider(user)
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Firebase createUserWithEmailAndPassword failed", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogleCredentialManager(
        activityContext: Context,
        serverClientId: String = DEFAULT_WEB_CLIENT_ID
    ): Result<FirebaseUser> = withContext(Dispatchers.Main) {
        val auth = firebaseAuth ?: return@withContext Result.failure(
            IllegalStateException("Firebase Auth is not initialized.")
        )

        try {
            val credentialManager = CredentialManager.create(activityContext)
            val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialResponse = credentialManager.getCredential(
                request = request,
                context = activityContext
            )

            val credential = credentialResponse.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                // Sign in with Firebase using the Google ID Token
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = withContext(Dispatchers.IO) {
                    auth.signInWithCredential(authCredential).awaitResult()
                }

                val user = authResult.user ?: return@withContext Result.failure(
                    IllegalStateException("Google Sign-In succeeded, but Firebase user is null.")
                )
                _currentUserState.value = user
                updateProvider(user)
                Result.success(user)
            } else {
                Result.failure(IllegalStateException("Unexpected credential format from Credential Manager."))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.i(TAG, "User cancelled Google Sign-In dialog")
            Result.failure(Exception("Google Sign-In cancelled by user."))
        } catch (e: GetCredentialException) {
            Log.w(TAG, "Credential Manager error: ${e.message}", e)
            Result.failure(Exception("Google Sign-In requires active Google Play Services or configured Web Client ID. Error: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In with Credential Manager failed", e)
            Result.failure(e)
        }
    }

    suspend fun signInAnonymously(): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val auth = firebaseAuth ?: return@withContext Result.failure(
            IllegalStateException("Firebase Auth is not initialized.")
        )
        try {
            val authResult = auth.signInAnonymously().awaitResult()
            val user = authResult.user ?: return@withContext Result.failure(
                IllegalStateException("Anonymous sign-in returned null user.")
            )
            _currentUserState.value = user
            updateProvider(user)
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Firebase signInAnonymously failed", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        try {
            firebaseAuth?.signOut()
            _currentUserState.value = null
            _authProviderName.value = "None"
        } catch (e: Exception) {
            Log.e(TAG, "Sign out error", e)
        }
    }
}

/**
 * Await extension for Firebase / Google Play Services Tasks without extra libraries
 */
suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitResult(): T =
    suspendCancellableCoroutine { cont ->
        addOnSuccessListener { result ->
            if (cont.isActive) cont.resume(result)
        }
        addOnFailureListener { exception ->
            if (cont.isActive) cont.resumeWithException(exception)
        }
        addOnCanceledListener {
            if (cont.isActive) cont.cancel()
        }
    }
