package com.example.newspulse.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

data class AuthResult(
    val success: Boolean,
    val errorMessage: String? = null
)

interface AuthRepository {
    suspend fun signUp(email: String, password: String): AuthResult
    suspend fun signIn(email: String, password: String): AuthResult

    /**
     * OAuth sign-in (e.g. Google). Opens system browser / Custom Tabs; session completes via deep link.
     * Only supported when using Supabase Auth + [observeSupabaseAuthUserId].
     */
    suspend fun signInWithGoogle(): AuthResult =
        AuthResult(false, "Google sign-in is not configured")

    /**
     * Emits the authenticated Supabase Auth user id when session becomes available (OAuth or restored).
     */
    fun observeSupabaseAuthUserId(): Flow<String?> = emptyFlow()

    /** Copies Supabase Auth JWT session into app prefs; call after OAuth redirect or cold start. */
    suspend fun syncSupabaseAuthSessionToApp(): Boolean = false

    fun signOut()
    fun getCurrentUserId(): String?

    /** Supabase Auth session email, if signed in with OAuth / Supabase Auth (not app_users-only). */
    fun getAuthenticatedUserEmail(): String? = null
}
