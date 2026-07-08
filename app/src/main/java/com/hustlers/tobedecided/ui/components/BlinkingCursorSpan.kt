package com.hustlers.tobedecided.ui.components

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ReplacementSpan

class BlinkingCursorSpan(private val color: Int) : ReplacementSpan() {
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