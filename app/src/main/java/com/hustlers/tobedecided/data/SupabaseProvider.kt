package com.hustlers.tobedecided.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient

object SupabaseProvider {

    private const val SUPABASE_URL = "https://YOUR_PROJECT_REF.supabase.co"
    private const val SUPABASE_KEY = "YOUR_ANON_OR_PUBLISHABLE_KEY"

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Auth)
        }
    }
}