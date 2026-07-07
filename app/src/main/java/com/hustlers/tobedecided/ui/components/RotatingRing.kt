package com.hustlers.tobedecided.ui.components

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.View

class RotatingRingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val strokeWidthPx = 6f * resources.displayMetrics.density
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = strokeWidthPx
        strokeCap = Paint.Cap.ROUND
    }

    private var rotationDeg = 0f
    private var animator: ValueAnimator? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val cx = w / 2f
        val cy = h / 2f
        val colors = intArrayOf(
            Color.parseColor("#2DD4BF"),
            Color.parseColor("#5AEB8F"),
            Color.parseColor("#EAFFF2"),
            Color.parseColor("#2DD4BF")
        )
        paint.shader = SweepGradient(cx, cy, colors, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val radius = (Math.min(width, height) / 2f) - strokeWidthPx
        canvas.save()
        canvas.rotate(rotationDeg, width / 2f, height / 2f)
        canvas.drawCircle(width / 2f, height / 2f, radius, paint)
        canvas.restore()
    }

    fun startSpinning(durationMs: Long = 5000L) {
        stopSpinning()
        animator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = durationMs
            repeatCount = ValueAnimator.INFINITE
            interpolator = android.view.animation.LinearInterpolator()
            addUpdateListener {
                rotationDeg = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun stopSpinning() {
        animator?.cancel()
        animator = null
    }

    override fun onDetachedFromWindow() {
        stopSpinning()
        super.onDetachedFromWindow()
    }
}