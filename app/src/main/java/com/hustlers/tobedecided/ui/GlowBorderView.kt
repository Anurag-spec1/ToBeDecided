package com.hustlers.tobedecided.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.hustlers.tobedecided.R
import kotlin.math.abs
import kotlin.random.Random

class GlowBorderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var borderPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private var glowPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        maskFilter = BlurMaskFilter(12f, BlurMaskFilter.Blur.NORMAL)
    }

    private val path = Path()
    private val pathMeasure = PathMeasure()
    private var pathLength = 0f
    private var animatedPhase = 0f

    // Animation properties
    private var animationDirection = 1f // 1 for clockwise, -1 for counter-clockwise
    private var animationDuration = 3000L
    private var animationStartOffset = 0L

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = animationDuration
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            val rawPhase = it.animatedValue as Float
            // Apply direction
            animatedPhase = if (animationDirection > 0) rawPhase else 1f - rawPhase
            invalidate()
        }
    }

    var cornerRadius = 16f
    var borderColor = Color.parseColor("#00FF88")
    var glowColor = Color.parseColor("#00FF88")
    var glowLength = 0.3f

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.GlowBorderView)
            cornerRadius = typedArray.getDimension(R.styleable.GlowBorderView_cornerRadius, 16f)
            borderColor = typedArray.getColor(R.styleable.GlowBorderView_borderColor, Color.parseColor("#00FF88"))
            glowColor = typedArray.getColor(R.styleable.GlowBorderView_glowColor, Color.parseColor("#00FF88"))
            glowLength = typedArray.getFloat(R.styleable.GlowBorderView_glowLength, 0.3f)
            val strokeW = typedArray.getDimension(R.styleable.GlowBorderView_strokeWidth, 2.5f)
            borderPaint.strokeWidth = strokeW
            glowPaint.strokeWidth = strokeW * 2
            typedArray.recycle()
        }

        // Randomize animation for each instance
        randomizeAnimation()
    }

    private fun randomizeAnimation() {
        // Random direction (clockwise or counter-clockwise)
        animationDirection = if (Random.nextBoolean()) 1f else -1f

        // Random duration between 2000ms and 4000ms
        animationDuration = Random.nextLong(2000, 4001)

        // Random start offset so they don't sync
        animationStartOffset = Random.nextLong(0, animationDuration)

        // Update animator with new values
        animator.duration = animationDuration
        animator.startDelay = animationStartOffset
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val padding = glowPaint.strokeWidth / 2 + 2f
        updatePath(w, h, padding)
    }

    private fun updatePath(w: Int, h: Int, padding: Float) {
        path.reset()
        path.addRoundRect(
            RectF(padding, padding, w.toFloat() - padding, h.toFloat() - padding),
            cornerRadius,
            cornerRadius,
            Path.Direction.CW
        )
        pathMeasure.setPath(path, false)
        pathLength = pathMeasure.length
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (pathLength == 0f) return

        // Draw the moving glow segment
        val startDistance = animatedPhase * pathLength
        val endDistance = (startDistance + pathLength * glowLength) % pathLength

        if (endDistance > startDistance) {
            drawSegment(canvas, startDistance, endDistance)
        } else {
            drawSegment(canvas, startDistance, pathLength)
            drawSegment(canvas, 0f, endDistance)
        }
    }

    private fun drawSegment(canvas: Canvas, startDist: Float, endDist: Float) {
        val segmentPath = Path()
        pathMeasure.getSegment(startDist, endDist, segmentPath, true)

        // Draw glow (outer)
        glowPaint.color = glowColor
        glowPaint.alpha = 80
        canvas.drawPath(segmentPath, glowPaint)

        // Draw border (inner)
        borderPaint.color = borderColor
        borderPaint.alpha = 255
        canvas.drawPath(segmentPath, borderPaint)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animator.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator.cancel()
    }

    fun setColors(borderColor: Int, glowColor: Int) {
        this.borderColor = borderColor
        this.glowColor = glowColor
    }

    fun setAnimationParams(direction: Float, duration: Long, startOffset: Long) {
        this.animationDirection = direction
        this.animationDuration = duration
        this.animationStartOffset = startOffset

        animator.duration = duration
        animator.startDelay = startOffset
    }
}