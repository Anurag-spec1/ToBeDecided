package com.hustlers.tobedecided.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Profile(
    val id: String,
    val name: String,
    val role: String,
    val phone: String? = null,
    @SerialName("sharing_enabled") val sharingEnabled: Boolean = false,
    @SerialName("sharing_mode") val sharingMode: String = AppPrefs.MODE_PRECISE,
    @SerialName("consent_at") val consentAt: String? = null,
    @SerialName("offline_sms_enabled") val offlineSmsEnabled: Boolean = false,
    @SerialName("offline_sms_interval_min") val offlineSmsIntervalMin: Int = 10
)

@Serializable
private data class NameOnly(val name: String)

@Serializable
private data class ConsentUpdate(
    @SerialName("sharing_enabled") val sharingEnabled: Boolean,
    @SerialName("sharing_mode") val sharingMode: String,
    @SerialName("consent_at") val consentAt: String
)

object ProfileRepository {

    private val profileColumns = Columns.list(
        "id",
        "name",
        "role",
        "phone",
        "sharing_enabled",
        "sharing_mode",
        "consent_at",
        "offline_sms_enabled",
        "offline_sms_interval_min"
    )

    fun currentUserId(): String? = SupabaseProvider.client.auth.currentUserOrNull()?.id

    suspend fun fetchMyProfile(): Profile? {
        val uid = currentUserId() ?: return null
        return SupabaseProvider.client.from("profiles")
            .select(profileColumns) {
                filter { eq("id", uid) }
            }
            .decodeSingleOrNull<Profile>()
    }

    suspend fun fetchAdminName(): String? =
        SupabaseProvider.client.from("profiles")
            .select(Columns.list("name")) {
                filter { eq("role", AppPrefs.ROLE_ADMIN) }
                limit(1)
            }
            .decodeSingleOrNull<NameOnly>()
            ?.name

    suspend fun acceptConsent(mode: String) {
        val uid = currentUserId() ?: throw IllegalStateException("Not signed in")
        SupabaseProvider.client.from("profiles").update(
            ConsentUpdate(
                sharingEnabled = true,
                sharingMode = mode,
                consentAt = Instant.now().toString()
            )
        ) {
            filter { eq("id", uid) }
        }
    }
}