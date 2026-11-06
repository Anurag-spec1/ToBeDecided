package com.hustlers.tobedecided.data

import android.content.Context

class AppPrefs(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("family_circle_prefs", Context.MODE_PRIVATE)

    val role: String?
        get() = prefs.getString(KEY_ROLE, null)

    val name: String?
        get() = prefs.getString(KEY_NAME, null)

    var adminName: String?
        get() = prefs.getString(KEY_ADMIN_NAME, null)
        set(value) = prefs.edit().putString(KEY_ADMIN_NAME, value).apply()

    var sharingEnabled: Boolean
        get() = prefs.getBoolean(KEY_SHARING_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_SHARING_ENABLED, value).apply()

    var sharingMode: String
        get() = prefs.getString(KEY_SHARING_MODE, MODE_PRECISE) ?: MODE_PRECISE
        set(value) = prefs.edit().putString(KEY_SHARING_MODE, value).apply()

    var consentDone: Boolean
        get() = prefs.getBoolean(KEY_CONSENT_DONE, false)
        set(value) = prefs.edit().putBoolean(KEY_CONSENT_DONE, value).apply()

    var consentDeclined: Boolean
        get() = prefs.getBoolean(KEY_CONSENT_DECLINED, false)
        set(value) = prefs.edit().putBoolean(KEY_CONSENT_DECLINED, value).apply()

    fun saveProfile(profile: Profile) {
        prefs.edit()
            .putString(KEY_ROLE, profile.role)
            .putString(KEY_NAME, profile.name)
            .putBoolean(KEY_SHARING_ENABLED, profile.sharingEnabled)
            .putString(KEY_SHARING_MODE, profile.sharingMode)
            .putBoolean(KEY_CONSENT_DONE, profile.consentAt != null)
            .apply()
    }

    fun isSetupConfirmed(stepId: String): Boolean =
        prefs.getBoolean(KEY_SETUP_PREFIX + stepId, false)

    fun setSetupConfirmed(stepId: String, value: Boolean) =
        prefs.edit().putBoolean(KEY_SETUP_PREFIX + stepId, value).apply()

    fun wasPermissionAsked(permission: String): Boolean =
        prefs.getBoolean(KEY_ASKED_PREFIX + permission, false)

    fun markPermissionAsked(permission: String) =
        prefs.edit().putBoolean(KEY_ASKED_PREFIX + permission, true).apply()

    fun clear() = prefs.edit().clear().apply()

    companion object {
        const val MODE_PRECISE = "PRECISE"
        const val MODE_APPROXIMATE = "APPROXIMATE"
        const val ROLE_ADMIN = "ADMIN"

        private const val KEY_ROLE = "role"
        private const val KEY_NAME = "name"
        private const val KEY_ADMIN_NAME = "admin_name"
        private const val KEY_SHARING_ENABLED = "sharing_enabled"
        private const val KEY_SHARING_MODE = "sharing_mode"
        private const val KEY_CONSENT_DONE = "consent_done"
        private const val KEY_CONSENT_DECLINED = "consent_declined"
        private const val KEY_SETUP_PREFIX = "setup_confirmed_"
        private const val KEY_ASKED_PREFIX = "permission_asked_"
    }
}