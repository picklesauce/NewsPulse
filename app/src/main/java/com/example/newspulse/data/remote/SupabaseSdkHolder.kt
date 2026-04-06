package com.example.newspulse.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.ExternalAuthAction
import io.github.jan.supabase.createSupabaseClient

// supabase sdk held

object SupabaseSdkHolder {

    @Volatile
    var client: SupabaseClient? = null
        private set

    fun init(supabaseUrl: String, supabaseAnonKey: String): SupabaseClient? {
        if (supabaseUrl.isBlank() || supabaseAnonKey.isBlank()) return null
        val c = createSupabaseClient(
            supabaseUrl = supabaseUrl.trimEnd('/'),
            supabaseKey = supabaseAnonKey
        ) {
            install(Auth) {
                host = "login-callback"
                scheme = "com.example.newspulse"
                defaultExternalAuthAction = ExternalAuthAction.CustomTabs()
            }
        }
        client = c
        return c
    }
}
