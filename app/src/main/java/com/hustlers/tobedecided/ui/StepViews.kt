package com.hustlers.tobedecided.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hustlers.tobedecided.databinding.ItemStepBinding

class StepRow(private val binding: ItemStepBinding) {

    fun setStatus(done: Boolean, doneText: String = "Done", pendingText: String = "Not done") {
        binding.stepStatus.text = if (done) "✓ $doneText" else pendingText
        binding.stepStatus.setTextColor(if (done) COLOR_DONE else COLOR_PENDING)
    }

    fun showAction(show: Boolean) {
        binding.stepAction.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun setActionEnabled(enabled: Boolean) {
        binding.stepAction.isEnabled = enabled
        binding.stepAction.alpha = if (enabled) 1f else 0.4f
    }

    fun setSecondary(text: String, onClick: () -> Unit) {
        binding.stepSecondary.text = text
        binding.stepSecondary.visibility = View.VISIBLE
        binding.stepSecondary.setOnClickListener { onClick() }
    }

    fun hideSecondary() {
        binding.stepSecondary.visibility = View.GONE
    }

    companion object {
        private val COLOR_DONE = Color.parseColor("#36DA77")
        private val COLOR_PENDING = Color.parseColor("#FFB74D")
    }
}

object StepViews {

    fun add(
        container: ViewGroup,
        title: String,
        description: String,
        actionText: String,
        onAction: () -> Unit
    ): StepRow {
        val binding = ItemStepBinding.inflate(LayoutInflater.from(container.context), container, true)
        binding.stepTitle.text = title
        binding.stepDescription.text = description
        binding.stepAction.text = actionText
        binding.stepAction.setOnClickListener { onAction() }
        return StepRow(binding)
    }
}