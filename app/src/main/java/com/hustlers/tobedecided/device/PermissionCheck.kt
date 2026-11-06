package com.hustlers.tobedecided.device

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionCheck {

    fun has(context: Context, permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun fineLocation(context: Context): Boolean =
        has(context, Manifest.permission.ACCESS_FINE_LOCATION)

    fun foregroundLocation(context: Context): Boolean =
        fineLocation(context) || has(context, Manifest.permission.ACCESS_COARSE_LOCATION)

    fun backgroundLocation(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                has(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    fun notifications(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                has(context, Manifest.permission.POST_NOTIFICATIONS)

    fun sms(context: Context): Boolean =
        has(context, Manifest.permission.SEND_SMS)

    fun coreGranted(context: Context): Boolean =
        foregroundLocation(context) && backgroundLocation(context) && notifications(context)
}