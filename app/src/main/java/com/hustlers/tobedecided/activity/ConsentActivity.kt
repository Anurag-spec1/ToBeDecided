package com.hustlers.tobedecided.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hustlers.tobedecided.data.AppPrefs
import com.hustlers.tobedecided.data.ProfileRepository
import com.hustlers.tobedecided.data.Route
import com.hustlers.tobedecided.data.SessionRouter
import com.hustlers.tobedecided.databinding.ActivityConsentBinding
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class ConsentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConsentBinding
    private lateinit var prefs: AppPrefs
    private var mode = AppPrefs.MODE_PRECISE
    private var saving = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConsentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = AppPrefs(this)

        binding.visibleTo.text = prefs.adminName?.let { "$it (Admin)" } ?: "Your family admin"
        loadAdminName()

        binding.optionPrecise.setOnClickListener { selectMode(AppPrefs.MODE_PRECISE) }
        binding.optionApproximate.setOnClickListener { selectMode(AppPrefs.MODE_APPROXIMATE) }
        selectMode(prefs.sharingMode)

        binding.startSharing.setOnClickListener { accept() }
        binding.notNow.setOnClickListener { decline() }
    }

    private fun loadAdminName() {
        lifecycleScope.launch {
            val name = try {
                ProfileRepository.fetchAdminName()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
            if (name != null) {
                prefs.adminName = name
                binding.visibleTo.text = "$name (Admin)"
            }
        }
    }

    private fun selectMode(value: String) {
        mode = value
        val precise = value == AppPrefs.MODE_PRECISE
        binding.preciseCheck.text = if (precise) "◉" else "○"
        binding.approximateCheck.text = if (precise) "○" else "◉"
        binding.optionPrecise.alpha = if (precise) 1f else 0.55f
        binding.optionApproximate.alpha = if (precise) 0.55f else 1f
    }

    private fun accept() {
        if (saving) return
        saving = true
        binding.errorText.visibility = View.GONE
        binding.startSharing.text = "Saving…"
        binding.startSharing.alpha = 0.7f

        lifecycleScope.launch {
            try {
                ProfileRepository.acceptConsent(mode)
                prefs.consentDone = true
                prefs.consentDeclined = false
                prefs.sharingEnabled = true
                prefs.sharingMode = mode

                val route = SessionRouter.resolve(this@ConsentActivity, refresh = false)
                if (route is Route.Go) SessionRouter.open(this@ConsentActivity, route.target)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                saving = false
                binding.startSharing.text = "Start sharing"
                binding.startSharing.alpha = 1f
                binding.errorText.text = "Could not save. Check your internet and try again."
                binding.errorText.visibility = View.VISIBLE
            }
        }
    }

    private fun decline() {
        prefs.consentDeclined = true
        prefs.sharingEnabled = false
        SessionRouter.open(this, MainActivity::class.java)
    }
}