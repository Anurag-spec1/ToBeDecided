package com.hustlers.tobedecided.device

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import com.hustlers.tobedecided.data.AppPrefs

enum class Oem { SAMSUNG, XIAOMI, MOTOROLA, OTHER }

data class SetupStep(
    val id: String,
    val title: String,
    val instructions: String,
    val intents: List<Intent>,
    val isDone: ((Context) -> Boolean)? = null
)

object DeviceSetup {

    fun oem(): Oem {
        val m = (Build.MANUFACTURER + " " + Build.BRAND).lowercase()
        return when {
            "samsung" in m -> Oem.SAMSUNG
            "xiaomi" in m || "redmi" in m || "poco" in m -> Oem.XIAOMI
            "motorola" in m || "moto" in m -> Oem.MOTOROLA
            else -> Oem.OTHER
        }
    }

    fun steps(context: Context): List<SetupStep> {
        val pkg = context.packageName
        val battery = batteryOptimizationStep(pkg)
        val autoRevoke = autoRevokeStep(pkg)
        return when (oem()) {
            Oem.SAMSUNG -> listOf(battery, samsungNeverSleeping(), autoRevoke)
            Oem.XIAOMI -> listOf(
                xiaomiAutostart(pkg),
                xiaomiBatterySaver(context, pkg),
                battery,
                autoRevoke,
                lockInRecents()
            )
            Oem.MOTOROLA, Oem.OTHER -> listOf(battery, autoRevoke)
        }
    }

    fun allDone(context: Context): Boolean {
        val prefs = AppPrefs(context)
        return steps(context).all { step ->
            step.isDone?.invoke(context) ?: prefs.isSetupConfirmed(step.id)
        }
    }

    fun open(context: Context, step: SetupStep): Boolean {
        for (intent in step.intents) {
            try {
                context.startActivity(Intent(intent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                return true
            } catch (e: ActivityNotFoundException) {
            } catch (e: SecurityException) {
            }
        }
        return false
    }

    private fun appDetails(pkg: String) =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", pkg, null))

    private fun batteryOptimizationStep(pkg: String) = SetupStep(
        id = "battery_optimization",
        title = "Allow unrestricted battery",
        instructions = "Tap Open, then Allow. This stops the phone from pausing location sharing when the screen is off.",
        intents = listOf(
            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$pkg")),
            appDetails(pkg)
        ),
        isDone = { ctx ->
            ctx.getSystemService(PowerManager::class.java)
                .isIgnoringBatteryOptimizations(ctx.packageName)
        }
    )

    private fun autoRevokeStep(pkg: String) = SetupStep(
        id = "auto_revoke",
        title = "Keep permissions",
        instructions = "Turn OFF \"Pause app activity if unused\" (it may say \"Remove permissions if app is unused\").",
        intents = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            listOf(
                Intent(Intent.ACTION_AUTO_REVOKE_PERMISSIONS, Uri.fromParts("package", pkg, null)),
                appDetails(pkg)
            )
        } else {
            listOf(appDetails(pkg))
        },
        isDone = { ctx ->
            Build.VERSION.SDK_INT < Build.VERSION_CODES.R || ctx.packageManager.isAutoRevokeWhitelisted
        }
    )

    private fun samsungNeverSleeping() = SetupStep(
        id = "samsung_never_sleeping",
        title = "Add to Never sleeping apps",
        instructions = "Settings → Battery → Background usage limits → Never sleeping apps → + → add this app. " +
                "On the same screen turn OFF \"Put unused apps to sleep\".",
        intents = listOf(
            Intent().setComponent(
                ComponentName("com.samsung.android.lool", "com.samsung.android.sm.battery.ui.BatteryActivity")
            ),
            Intent().setComponent(
                ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")
            ),
            Intent(Settings.ACTION_SETTINGS)
        )
    )

    private fun xiaomiAutostart(pkg: String) = SetupStep(
        id = "xiaomi_autostart",
        title = "Turn on Autostart",
        instructions = "Find this app in the list and turn Autostart ON. Without it, sharing will not restart after the phone reboots.",
        intents = listOf(
            Intent().setComponent(
                ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
            ),
            appDetails(pkg)
        )
    )

    private fun xiaomiBatterySaver(context: Context, pkg: String): SetupStep {
        val label = context.applicationInfo.loadLabel(context.packageManager).toString()
        return SetupStep(
            id = "xiaomi_battery_saver",
            title = "Battery saver: No restrictions",
            instructions = "Choose \"No restrictions\". If the screen does not open, go to App info → Battery saver → No restrictions.",
            intents = listOf(
                Intent().setComponent(
                    ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity")
                ).putExtra("package_name", pkg).putExtra("package_label", label),
                appDetails(pkg)
            )
        )
    }

    private fun lockInRecents() = SetupStep(
        id = "lock_in_recents",
        title = "Lock the app in Recents",
        instructions = "Open Recents, long-press this app's card, and tap the lock icon. This stops \"Clear all\" from closing it.",
        intents = emptyList()
    )
}