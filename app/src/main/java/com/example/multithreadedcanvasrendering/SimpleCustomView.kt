package com.example.multithreadedcanvasrendering

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime
import kotlin.time.MonoClock

@ExperimentalTime
class SimpleCustomView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    var color = Color.BLACK

    val textPaint = Paint().also {

        it.color = Color.WHITE
        it.textSize = 67f
    }

    val contentPaint = Paint().also {
        it.color = Color.MAGENTA
        it.style = Paint.Style.STROKE
        it.strokeWidth = 4f
    }
    val startTime = MonoClock.markNow()
    var currentFrameCount = 0L

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        color+=1
        draw(canvas, color, width, height, contentPaint)
        canvas.drawText("Custom View: ${(currentFrameCount++/startTime.elapsedNow().inSeconds).roundToInt()}fps", 30f, 125f, textPaint)
        invalidate()
    }
}

