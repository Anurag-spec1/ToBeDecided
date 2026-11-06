package com.hustlers.tobedecided.data

import com.hustlers.tobedecided.config.ApiConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient

object SupabaseProvider {

    private const val SUPABASE_URL = ApiConfig.SUPABASE_URL
    private const val SUPABASE_KEY = ApiConfig.SUPABASE_KEY

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Auth)
        }
    }
}