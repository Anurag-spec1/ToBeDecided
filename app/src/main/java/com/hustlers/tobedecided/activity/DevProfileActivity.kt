package com.hustlers.tobedecided.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.hustlers.tobedecided.R
import com.hustlers.tobedecided.databinding.ActivityDevProfileBinding
import kotlin.random.Random

class DevProfileActivity : AppCompatActivity() {

    private val displayName = "ARJUN MEHTA"
    private val githubUrl = "https://github.com/arjunmehta"
    private val linkedinUrl = "https://linkedin.com/in/arjunmehta"
    private val instagramUrl = "https://instagram.com/arjun.codes"
    private val emailAddress = "arjun.mehta.dev@example.com"

    private val mainHandler = Handler(Looper.getMainLooper())
    private var roleCursorOn = true
    private var scanlineAnimator: ValueAnimator? = null
    private var avatarScanAnimator: ObjectAnimator? = null
    private var shimmerAnimator: ObjectAnimator? = null
    private var statusPulseAnimator: ObjectAnimator? = null

    private lateinit var binding: ActivityDevProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDevProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ring.startSpinning(5000L)

        setupClipToOval(binding.avatarScan)
        startAmbientLoops()
        playBootSequence()
        setupClickListeners()
    }

    private fun setupClipToOval(scanView: View) {
        val parent = scanView.parent as? View ?: return
        parent.clipToOutline = true
        parent.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: android.graphics.Outline) {
                outline.setOval(0, 0, view.width, view.height)
            }
        }
    }

    private fun startAmbientLoops() {
        binding.scanlineBeam.post {
            val screenHeight = resources.displayMetrics.heightPixels.toFloat()
            scanlineAnimator =
                ValueAnimator.ofFloat(-binding.scanlineBeam.height.toFloat(), screenHeight).apply {
                    duration = 7000L
                    repeatCount = ValueAnimator.INFINITE
                    interpolator = LinearInterpolator()
                    addUpdateListener {
                        binding.scanlineBeam.translationY = it.animatedValue as Float
                    }
                    start()
                }
        }

        binding.avatarScan.post {
            avatarScanAnimator = ObjectAnimator.ofFloat(
                binding.avatarScan, "translationY", -30f * resources.displayMetrics.density,
                94f * resources.displayMetrics.density
            ).apply {
                duration = 3200L
                repeatCount = ValueAnimator.INFINITE
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }

        statusPulseAnimator = ObjectAnimator.ofFloat(binding.statusDot, "alpha", 1f, 0.35f).apply {
            duration = 1000L
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            start()
        }
    }

    private fun playBootSequence() {
        mainHandler.postDelayed(
            { binding.bootLine1.animate().alpha(1f).setDuration(300).start() },
            100
        )
        mainHandler.postDelayed(
            { binding.bootLine2.animate().alpha(1f).setDuration(300).start() },
            550
        )
        mainHandler.postDelayed(
            { binding.bootLine3.animate().alpha(1f).setDuration(300).start() },
            1050
        )

        ObjectAnimator.ofInt(binding.bootProgress, "progress", 0, 100).apply {
            duration = 1600L
            startDelay = 150
            interpolator = DecelerateInterpolator()
            start()
        }

        mainHandler.postDelayed({
            binding.bootOverlay.animate()
                .alpha(0f)
                .scaleX(1.02f).scaleY(1.02f)
                .setDuration(500)
                .withEndAction { binding.bootOverlay.visibility = View.GONE }
                .start()

            revealCard(binding.cardFrame)
            startDecrypt()
        }, 1750)
    }

    private fun revealCard(cardFrame: View) {
        cardFrame.scaleX = 0.94f
        cardFrame.scaleY = 0.94f
        cardFrame.translationY = 10f * resources.displayMetrics.density

        cardFrame.animate()
            .alpha(1f)
            .scaleX(1f).scaleY(1f)
            .translationY(0f)
            .setDuration(700)
            .setInterpolator(OvershootInterpolator(0.9f))
            .withEndAction { flashBrightness(cardFrame) }
            .start()

        fadeUp(binding.roleText, 1500)

        binding.dividerLeft.playDrawIn(1400L, 1800L)
        binding.dividerRight.playDrawIn(1400L, 1800L)

        val rows = listOf(
            binding.rowGithub to 1900L,
            binding.rowLinkedin to 2020L,
            binding.rowInstagram to 2140L,
            binding.rowMail to 2260L
        )
        rows.forEach { (row, delay) -> slideIn(row, delay) }

        fadeUp(binding.ctaWrap, 2380) { startShimmer() }

        fadeUp(binding.footerRow, 2500)
    }

    private fun flashBrightness(view: View) {
        view.animate().scaleX(1.015f).scaleY(1.015f).setDuration(120)
            .withEndAction {
                view.animate().scaleX(1f).scaleY(1f).setDuration(180).start()
            }.start()
    }

    private fun fadeUp(view: View, delay: Long, onEnd: (() -> Unit)? = null) {
        view.translationY = 6f * resources.displayMetrics.density
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(delay)
            .setDuration(600)
            .withEndAction { onEnd?.invoke() }
            .start()

        if (view.id == R.id.role_text) {
            mainHandler.postDelayed({ startRoleCursorBlink() }, delay)
        }
    }

    private fun slideIn(view: View, delay: Long) {
        view.translationX = -16f * resources.displayMetrics.density
        view.animate()
            .alpha(1f)
            .translationX(0f)
            .setStartDelay(delay)
            .setDuration(500)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun startRoleCursorBlink() {
        val baseText = "FULL STACK DEVELOPER"
        val blink = object : Runnable {
            override fun run() {
                binding.roleText.text = if (roleCursorOn) "$baseText _" else baseText
                roleCursorOn = !roleCursorOn
                mainHandler.postDelayed(this, 500)
            }
        }
        mainHandler.post(blink)
    }

    private fun startShimmer() {
        binding.ctaButton.post {
            val start = -binding.ctaShimmer.width.toFloat()
            val end = binding.ctaButton.width.toFloat()
            shimmerAnimator =
                ObjectAnimator.ofFloat(binding.ctaShimmer, "translationX", start, end).apply {
                    duration = 1300L
                    startDelay = 0
                    repeatCount = ValueAnimator.INFINITE
                    interpolator = AccelerateDecelerateInterpolator()
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationRepeat(animation: Animator) {
                            binding.ctaShimmer.translationX = start
                        }
                    })
                    start()
                }
        }
    }

    private fun startDecrypt() {
        val target = displayName
        val chars = "!<>-_\\/[]{}—=+*^?#01"
        val totalFrames = 22
        var frame = 0
        val resolveAt = target.mapIndexed { i, c ->
            if (c == ' ') 0 else (Random.nextInt((totalFrames * 0.55).toInt()) + (i * 1.1).toInt())
        }

        val runnable = object : Runnable {
            override fun run() {
                val sb = StringBuilder()
                for (i in target.indices) {
                    val c = target[i]
                    sb.append(
                        when {
                            c == ' ' -> ' '
                            frame >= resolveAt[i] -> c
                            else -> chars[Random.nextInt(chars.length)]
                        }
                    )
                }
                binding.nameText.text = sb.toString()
                frame++
                if (frame <= totalFrames + 6) {
                    mainHandler.postDelayed(this, 38)
                } else {
                    binding.nameText.text = target
                }
            }
        }
        mainHandler.post(runnable)
    }

    private fun setupClickListeners() {
        binding.rowGithub.setOnClickListener { openUrl(githubUrl) }
        binding.rowLinkedin.setOnClickListener { openUrl(linkedinUrl) }
        binding.rowInstagram.setOnClickListener { openUrl(instagramUrl) }
        binding.rowMail.setOnClickListener { openMail() }
        binding.ctaButton.setOnClickListener { openMail() }
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun openMail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$emailAddress")
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        mainHandler.removeCallbacksAndMessages(null)
        scanlineAnimator?.cancel()
        avatarScanAnimator?.cancel()
        shimmerAnimator?.cancel()
        statusPulseAnimator?.cancel()
        super.onDestroy()
    }
}