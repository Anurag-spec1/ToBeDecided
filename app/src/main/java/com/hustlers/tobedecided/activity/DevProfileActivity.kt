package com.hustlers.tobedecided.activity

import android.animation.ValueAnimator
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ReplacementSpan
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.hustlers.tobedecided.R
import com.hustlers.tobedecided.databinding.ActivityDevProfileBinding

class DevProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDevProfileBinding
    private var cursorBlinkAnimator: ValueAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDevProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadHeaderImages()
        setupBioWithBlinkingCursor()
        setupLinkTiles()
    }

    private fun loadHeaderImages() {
        Glide.with(this)
            .load("https://images.unsplash.com/photo-1550439062-609e1531270e?w=800&q=80")
            .centerCrop()
            .into(binding.coverImage)

        Glide.with(this)
            .load("https://i.pravatar.cc/200?img=13")
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

    private data class LinkTile(
        val title: String,
        val subtitle: String,
        val iconRes: Int,
        val brand: Int,
        val brandBg: Int,
        val url: String
    )

    private fun setupLinkTiles() {
        val tiles = listOf(
            binding.tileGithub.root to LinkTile(
                title = "GitHub",
                subtitle = "/anurag-shrivastav",
                iconRes = R.drawable.ic_github,
                brand = Color.parseColor("#F1F7F3"),
                brandBg = Color.parseColor("#26F1F7F3"),
                url = "https://github.com/"
            ),
            binding.tileLinkedin.root to LinkTile(
                title = "LinkedIn",
                subtitle = "/in/anurag-shrivastav",
                iconRes = R.drawable.ic_linkedin,
                brand = Color.parseColor("#3B9EFF"),
                brandBg = Color.parseColor("#383B9EFF"),
                url = "https://linkedin.com/"
            ),
            binding.tileInstagram.root to LinkTile(
                title = "Instagram",
                subtitle = "@anurag.shrivastav",
                iconRes = R.drawable.ic_instagram,
                brand = Color.parseColor("#E1306C"),
                brandBg = Color.parseColor("#38E1306C"),
                url = "https://instagram.com/"
            ),
            binding.tileGmail.root to LinkTile(
                title = "Gmail",
                subtitle = "anurag.shrivastav",
                iconRes = R.drawable.ic_gmail,
                brand = Color.parseColor("#EA4335"),
                brandBg = Color.parseColor("#38EA4335"),
                url = "mailto:anurag.shrivastav@gmail.com"
            )
        )

        tiles.forEach { (view, data) -> bindLinkTile(view, data) }
    }

    private fun bindLinkTile(root: View, data: LinkTile) {
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

        root.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(data.url)))
            } catch (e: Exception) {
                Log.e(TAG, "No app found to handle: ${data.url}", e)
            }
        }
    }

    override fun onDestroy() {
        cursorBlinkAnimator?.cancel()
        cursorBlinkAnimator = null
        super.onDestroy()
    }

    private class BlinkingCursorSpan(private val color: Int) : ReplacementSpan() {
        var alpha = 255
        private val paint = Paint()

        override fun getSize(
            paint: Paint,
            text: CharSequence,
            start: Int,
            end: Int,
            fm: Paint.FontMetricsInt?
        ): Int = (paint.textSize * 0.55f).toInt()

        override fun draw(
            canvas: Canvas,
            text: CharSequence,
            start: Int,
            end: Int,
            x: Float,
            top: Int,
            y: Int,
            bottom: Int,
            paint: Paint
        ) {
            this.paint.color = color
            this.paint.alpha = alpha
            val w = paint.textSize * 0.42f
            val h = paint.textSize * 0.92f
            canvas.drawRect(x, y - h, x + w, y.toFloat(), this.paint)
        }
    }
}