package com.hustlers.tobedecided.data

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.hustlers.tobedecided.activity.ConsentActivity
import com.hustlers.tobedecided.activity.DeviceSetupActivity
import com.hustlers.tobedecided.activity.MainActivity
import com.hustlers.tobedecided.activity.PermissionsActivity
import com.hustlers.tobedecided.device.DeviceSetup
import com.hustlers.tobedecided.device.PermissionCheck
import io.github.jan.supabase.exceptions.HttpRequestException
import kotlinx.coroutines.CancellationException

object SessionRouter {

    private const val TAG = "SessionRouter"

    suspend fun resolve(context: Context, refresh: Boolean = true): Route {
        val prefs = AppPrefs(context)

        if (refresh) {
            try {
                val profile = ProfileRepository.fetchMyProfile() ?: return Route.NoProfile
                prefs.saveProfile(profile)
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpRequestException) {
                Log.w(TAG, "Offline while loading profile", e)
            } catch (e: Exception) {
                Log.e(TAG, "Profile load failed", e)
                if (prefs.role == null) {
                    return Route.Error(e.message ?: e::class.java.simpleName)
                }
            }
        }

        val role = prefs.role ?: return Route.NeedsInternet

        val target: Class<out Activity> = when {
            role == AppPrefs.ROLE_ADMIN -> MainActivity::class.java
            !prefs.consentDone && prefs.consentDeclined -> MainActivity::class.java
            !prefs.consentDone -> ConsentActivity::class.java
            !PermissionCheck.coreGranted(context) -> PermissionsActivity::class.java
            !DeviceSetup.allDone(context) -> DeviceSetupActivity::class.java
            else -> MainActivity::class.java
        }
        return Route.Go(target)
    }

    fun open(activity: Activity, target: Class<out Activity>) {
        activity.startActivity(
            Intent(activity, target)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        activity.finish()
    }
}