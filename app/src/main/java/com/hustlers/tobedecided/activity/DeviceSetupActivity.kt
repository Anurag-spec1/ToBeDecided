package com.hustlers.tobedecided.activity

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hustlers.tobedecided.data.AppPrefs
import com.hustlers.tobedecided.data.Route
import com.hustlers.tobedecided.data.SessionRouter
import com.hustlers.tobedecided.databinding.ActivityStepsBinding
import com.hustlers.tobedecided.device.DeviceSetup
import com.hustlers.tobedecided.device.SetupStep
import com.hustlers.tobedecided.ui.StepRow
import com.hustlers.tobedecided.ui.StepViews
import kotlinx.coroutines.launch

class DeviceSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStepsBinding
    private lateinit var prefs: AppPrefs
    private val rows = mutableListOf<Pair<SetupStep, StepRow>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStepsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = AppPrefs(this)

        val brand = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        binding.titleTop.text = "Keep it"
        binding.titleBottom.text = "running"
        binding.subtitle.text =
            "$brand phones can stop this app in the background. Do each step once so sharing never stops on its own."

        DeviceSetup.steps(this).forEach { step ->
            val row = StepViews.add(binding.stepContainer, step.title, step.instructions, "Open") {
                openStep(step)
            }
            rows += step to row
        }

        binding.continueButton.setOnClickListener { goNext() }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun openStep(step: SetupStep) {
        if (!DeviceSetup.open(this, step)) {
            Toast.makeText(
                this,
                "Could not open that screen. Follow the steps written above.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun refresh() {
        rows.forEach { (step, row) ->
            val checker = step.isDone
            if (checker != null) {
                val done = checker(this)
                row.setStatus(done)
                row.showAction(!done)
                row.hideSecondary()
            } else {
                val done = prefs.isSetupConfirmed(step.id)
                row.setStatus(done, "Done", "Tap \"I did this\" when finished")
                row.showAction(!done && step.intents.isNotEmpty())
                row.setSecondary(if (done) "Undo" else "I did this") {
                    prefs.setSetupConfirmed(step.id, !done)
                    refresh()
                }
            }
        }

        val ready = DeviceSetup.allDone(this)
        binding.continueButton.isEnabled = ready
        binding.continueButton.alpha = if (ready) 1f else 0.5f
    }

    private fun goNext() {
        lifecycleScope.launch {
            val route = SessionRouter.resolve(this@DeviceSetupActivity, refresh = false)
            if (route is Route.Go) SessionRouter.open(this@DeviceSetupActivity, route.target)
        }
    }
}