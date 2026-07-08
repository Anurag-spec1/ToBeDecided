package com.hustlers.tobedecided.activity

import android.animation.ValueAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.hustlers.tobedecided.R
import com.hustlers.tobedecided.databinding.ActivityDevProfileBinding
import com.hustlers.tobedecided.dataclass.LinkTile
import com.hustlers.tobedecided.ui.components.BlinkingCursorSpan

class DevProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDevProfileBinding
    private var cursorBlinkAnimator: ValueAnimator? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isMusicPrepared = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDevProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setMaxBrightness()

        setupFullScreenMode()

        loadHeaderImages()
        setupBioWithBlinkingCursor()
        setupLinkTiles()

        playBackgroundMusic()
    }

    private fun setupFullScreenMode() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                window?.apply {
                    setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN
                    )
                    insetsController?.apply {
                        hide(android.view.WindowInsets.Type.statusBars())
                        hide(android.view.WindowInsets.Type.navigationBars())
                        systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                window?.decorView?.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        )
            }
        } catch (e: Exception) {
            Log.e("DevProfileActivity", "Error setting full screen: ${e.message}")
            try {
                window?.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
            } catch (ex: Exception) {
                Log.e("DevProfileActivity", "Complete fullscreen failure: ${ex.message}")
            }
        }
    }

    private fun setMaxBrightness() {
        try {
            window?.attributes = window?.attributes?.apply {
                screenBrightness = 1.0f
            }

            if (Settings.System.canWrite(this)) {
                try {
                    Settings.System.putInt(
                        contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                    )
                    Settings.System.putFloat(
                        contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS,
                        1.0f
                    )
                } catch (e: Exception) {
                    Log.e("DevProfileActivity", "System brightness error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("DevProfileActivity", "Failed to set brightness: ${e.message}")
        }
    }

    private fun playBackgroundMusic() {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer().apply {
                    try {
                        setDataSource(
                            this@DevProfileActivity,
                            Uri.parse("android.resource://${packageName}/${R.raw.background_sound}")
                        )
                        isLooping = false
                        setVolume(0.7f, 0.7f)

                        setOnPreparedListener { mp ->
                            isMusicPrepared = true
                            mp.start()
                            startVibration()
                            Log.d("DevProfileActivity", "Music started playing successfully")
                        }

                        setOnCompletionListener { mp ->
                            Log.d("DevProfileActivity", "Music playback completed")
                            stopVibration()
                            mp.release()
                            mediaPlayer = null
                            isMusicPrepared = false
                        }

                        setOnErrorListener { mp, what, extra ->
                            Log.e("DevProfileActivity", "MediaPlayer error: what=$what, extra=$extra")
                            stopVibration()
                            isMusicPrepared = false
                            false
                        }

                        prepareAsync()
                    } catch (e: Exception) {
                        Log.e("DevProfileActivity", "MediaPlayer setup error: ${e.message}")
                    }
                }
            } else {
                if (isMusicPrepared && mediaPlayer?.isPlaying == false) {
                    mediaPlayer?.start()
                    startVibration()
                }
            }
        } catch (e: Exception) {
            Log.e("DevProfileActivity", "Failed to initialize music: ${e.message}")
        }
    }

    private fun startVibration() {
        try {
            val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
            }

            if (vibrator.hasVibrator()) {
                val pattern = longArrayOf(0, 500, 500)
                vibrator.vibrate(
                    android.os.VibrationEffect.createWaveform(
                        pattern,
                        0
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("DevProfileActivity", "Vibration error: ${e.message}")
        }
    }

    private fun stopVibration() {
        try {
            val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
            }
            vibrator.cancel()
        } catch (e: Exception) {
            Log.e("DevProfileActivity", "Stop vibration error: ${e.message}")
        }
    }

    private fun stopBackgroundMusic() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                reset()
                release()
            }
            mediaPlayer = null
            isMusicPrepared = false
        } catch (e: Exception) {
            Log.e("DevProfileActivity", "Error stopping music: ${e.message}")
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            setupFullScreenMode()
        }
    }

    override fun onResume() {
        super.onResume()
        setupFullScreenMode()
        if (isMusicPrepared && mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    private fun loadHeaderImages() {
        Glide.with(this)
            .load("https://media.licdn.com/dms/image/v2/D4E03AQGVvQPO3_rSiw/profile-displayphoto-shrink_400_400/B4EZauiQ3FHIAg-/0/1746684940985?e=1785369600&v=beta&t=SsbSZgXaUosFrtg1399GSZR6IEoWvcJXFNrF_o0MeN8")
            .centerCrop()
            .into(binding.avatarImage)
    }

    private fun setupBioWithBlinkingCursor() {
        val bioText = getString(R.string.bio_text)
        val cursorSpan = BlinkingCursorSpan(ContextCompat.getColor(this, R.color.accent))

        val spannable = SpannableStringBuilder(bioText).append(" \u2002")
        spannable.setSpan(
            cursorSpan,
            spannable.length - 1,
            spannable.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.bioText.text = spannable

        cursorBlinkAnimator = ValueAnimator.ofInt(255, 0).apply {
            duration = 550
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { animator ->
                cursorSpan.alpha = animator.animatedValue as Int
                binding.bioText.invalidate()
            }
            start()
        }
    }

    private fun setupLinkTiles() {
        val tiles = listOf(
            binding.tileGithub.root to LinkTile(
                title = "GitHub",
                subtitle = "/Anurag-spec1",
                iconRes = R.drawable.ic_github,
                brand = Color.parseColor("#F1F7F3"),
                brandBg = Color.parseColor("#26F1F7F3"),
                url = "https://github.com/Anurag-spec1"
            ),
            binding.tileLinkedin.root to LinkTile(
                title = "LinkedIn",
                subtitle = "/in/anurag-shrivastav",
                iconRes = R.drawable.ic_linkedin,
                brand = Color.parseColor("#3B9EFF"),
                brandBg = Color.parseColor("#383B9EFF"),
                url = "https://www.linkedin.com/in/anurag-shrivastav-b7a616327/"
            ),
            binding.tileInstagram.root to LinkTile(
                title = "Instagram",
                subtitle = "@anurag_shrivastav18",
                iconRes = R.drawable.ic_instagram,
                brand = Color.parseColor("#E1306C"),
                brandBg = Color.parseColor("#38E1306C"),
                url = "https://instagram.com/anurag_shrivastav18"
            ),
            binding.tileGmail.root to LinkTile(
                title = "Gmail",
                subtitle = "anuragshrivastav082",
                iconRes = R.drawable.ic_gmail,
                brand = Color.parseColor("#EA4335"),
                brandBg = Color.parseColor("#38EA4335"),
                url = "mailto:anuragshrivastav082@gmail.com"
            )
        )

        tiles.forEachIndexed { index, (rootView, data) ->
            bindLinkTile(rootView, data, index)
        }
    }

    private fun bindLinkTile(root: View, data: LinkTile, index: Int) {
        root.findViewById<TextView>(R.id.tileTitle).text = data.title
        root.findViewById<TextView>(R.id.tileSubtitle).text = data.subtitle

        root.findViewById<FrameLayout>(R.id.tileIconBg).apply {
            background.mutate()
            backgroundTintList = ColorStateList.valueOf(data.brandBg)
        }

        root.findViewById<ImageView>(R.id.tileIcon).apply {
            setImageResource(data.iconRes)
            imageTintList = ColorStateList.valueOf(data.brand)
        }

        root.findViewById<ImageView>(R.id.tileGoIcon).imageTintList =
            ColorStateList.valueOf(data.brand)

        val glowBorder = root.findViewById<com.hustlers.tobedecided.ui.GlowBorderView>(R.id.glowBorder)
        glowBorder.setColors(data.brand, data.brand)

        val directions = listOf(1f, -1f, 1f, -1f)
        val durations = listOf(2000L, 2800L, 3200L, 2500L)
        val offsets = listOf(0L, 700L, 1400L, 300L)

        glowBorder.setAnimationParams(
            direction = directions[index % directions.size],
            duration = durations[index % durations.size],
            startOffset = offsets[index % offsets.size]
        )

        root.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(data.url)))
            } catch (e: Exception) {
                Log.e("DevProfileActivity", "No app found to handle: ${data.url}", e)
            }
        }
    }

    override fun onDestroy() {
        cursorBlinkAnimator?.cancel()
        cursorBlinkAnimator = null
        stopBackgroundMusic()
        super.onDestroy()
    }
}