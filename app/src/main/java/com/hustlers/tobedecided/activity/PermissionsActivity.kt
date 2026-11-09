package com.hustlers.tobedecided.activity

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hustlers.tobedecided.data.AppPrefs
import com.hustlers.tobedecided.data.Route
import com.hustlers.tobedecided.data.SessionRouter
import com.hustlers.tobedecided.databinding.ActivityStepsBinding
import com.hustlers.tobedecided.device.PermissionCheck
import com.hustlers.tobedecided.ui.StepRow
import com.hustlers.tobedecided.ui.StepViews
import kotlinx.coroutines.launch

class PermissionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStepsBinding
    private lateinit var prefs: AppPrefs
    private lateinit var locationRow: StepRow
    private lateinit var backgroundRow: StepRow
    private var notificationRow: StepRow? = null
    private lateinit var smsRow: StepRow

    private val multiLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { refresh() }

    private val singleLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { refresh() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStepsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = AppPrefs(this)

        binding.titleTop.text = "A few"
        binding.titleBottom.text = "permissions"
        binding.subtitle.text =
            "These let your family see where you are, even when the app is closed. Tap Allow on each one."

        locationRow = StepViews.add(
            binding.stepContainer,
            "Location",
            "Lets the app know where you are. Choose \"While using the app\" and keep \"Precise\" on.",
            "Allow"
        ) {
            request(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            ) { multiLauncher.launch(it) }
        }

        backgroundRow = StepViews.add(
            binding.stepContainer,
            "Allow all the time",
            "On the next screen choose \"Allow all the time\". This keeps sharing on when the app is closed.",
            "Open"
        ) { requestBackground() }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationRow = StepViews.add(
                binding.stepContainer,
                "Notifications",
                "Shows a notification while sharing is on, so you always know.",
                "Allow"
            ) {
                request(arrayOf(Manifest.permission.POST_NOTIFICATIONS)) {
                    singleLauncher.launch(it.first())
                }
            }
        }

        smsRow = StepViews.add(
            binding.stepContainer,
            "SMS for SOS",
            "Sends your SOS location by SMS when there is no internet. Strongly recommended.",
            "Allow"
        ) {
            request(arrayOf(Manifest.permission.SEND_SMS)) {
                singleLauncher.launch(it.first())
            }
        }

        binding.continueButton.setOnClickListener { goNext() }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun request(permissions: Array<String>, launch: (Array<String>) -> Unit) {
        val blocked = permissions.all {
            prefs.wasPermissionAsked(it) && !shouldShowRequestPermissionRationale(it)
        }
        if (blocked) {
            openAppSettings()
            return
        }
        permissions.forEach { prefs.markPermissionAsked(it) }
        launch(permissions)
    }

    private fun requestBackground() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            refresh()
            return
        }
        if (!PermissionCheck.foregroundLocation(this)) {
            Toast.makeText(this, "Allow Location first", Toast.LENGTH_SHORT).show()
            return
        }
        request(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            singleLauncher.launch(it.first())
        }
    }

    private fun openAppSettings() {
        Toast.makeText(this, "Open Permissions and allow it there", Toast.LENGTH_LONG).show()
        startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
        )
    }

    private fun refresh() {
        val foreground = PermissionCheck.foregroundLocation(this)
        val fine = PermissionCheck.fineLocation(this)
        val needsPrecise = prefs.sharingMode == AppPrefs.MODE_PRECISE
        val locationOk = foreground && (fine || !needsPrecise)

        locationRow.setStatus(
            locationOk,
            "Done",
            if (foreground) "Precise location is off" else "Not done"
        )
        locationRow.showAction(!locationOk)

        val background = PermissionCheck.backgroundLocation(this)
        backgroundRow.setStatus(background, "Done", if (foreground) "Not done" else "Allow Location first")
        backgroundRow.showAction(!background)
        backgroundRow.setActionEnabled(foreground)

        notificationRow?.let { row ->
            val granted = PermissionCheck.notifications(this)
            row.setStatus(granted)
            row.showAction(!granted)
        }

        val sms = PermissionCheck.sms(this)
        smsRow.setStatus(sms, "Done", "Recommended")
        smsRow.showAction(!sms)

        val ready = PermissionCheck.coreGranted(this)
        binding.continueButton.isEnabled = ready
        binding.continueButton.alpha = if (ready) 1f else 0.5f
    }

    private fun goNext() {
        lifecycleScope.launch {
            val route = SessionRouter.resolve(this@PermissionsActivity, refresh = false)
            if (route is Route.Go) SessionRouter.open(this@PermissionsActivity, route.target)
        }
    }
}