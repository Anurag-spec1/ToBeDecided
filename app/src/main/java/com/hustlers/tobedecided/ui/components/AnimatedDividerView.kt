package com.hustlers.tobedecided.ui.components

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator

class AnimatedDividerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4ADE80")
        alpha = 150
        strokeWidth = 2f * resources.displayMetrics.density
        strokeCap = Paint.Cap.ROUND
    }

    private var progress = 0f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val midY = height / 2f
        val fullWidth = width.toFloat()
        canvas.drawLine(0f, midY, fullWidth * progress, midY, paint)
    }

    fun playDrawIn(durationMs: Long = 1400L, startDelay: Long = 0L) {
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = durationMs
            this.startDelay = startDelay
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                progress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }
}