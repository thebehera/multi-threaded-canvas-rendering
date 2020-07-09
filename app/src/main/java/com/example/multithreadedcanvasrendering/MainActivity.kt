package com.example.multithreadedcanvasrendering

import android.graphics.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.multithreadedcanvasrendering.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlin.math.roundToInt
import kotlin.time.*

@ExperimentalTime
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        launchSurfaceRendering(binding.surface)
        launchTextureRendering(binding.texture)
    }


    fun launchSurfaceRendering(surfaceView: SurfaceView) {
        val surfaceJob = Job() + Dispatchers.Default
        var color = Color.BLACK

        val textPaint = Paint()
        textPaint.color = Color.WHITE
        textPaint.textSize = 67f

        val contentPaint = Paint()
        contentPaint.color = Color.MAGENTA
        contentPaint.style = Paint.Style.STROKE
        contentPaint.strokeWidth = 4f

        var isRunning = true
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                lifecycleScope.launch(surfaceJob) {
                    val startTime = MonoClock.markNow()
                    var currentFrameCount = 0L
                    var postTime = 0.seconds
                    while (isActive && holder.surface.isValid && isRunning) {
                        val canvas = lockCanvas(holder)
                        color+=1
                        draw(canvas, color, holder.surfaceFrame.width(), holder.surfaceFrame.height(), contentPaint)
                        canvas.drawText("Surface View: ${(currentFrameCount++/startTime.elapsedNow().inSeconds).roundToInt()}fps", 30f, 125f, textPaint)
                        canvas.drawText("Unlock time: $postTime", 30f, 400f, textPaint)
                        postTime = measureTime { holder.unlockCanvasAndPost(canvas) }
                    }
                }
            }
            override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {}
            override fun surfaceDestroyed(holder: SurfaceHolder) { isRunning = false }
        })
    }

    fun lockCanvas(holder: SurfaceHolder):Canvas {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return holder.lockHardwareCanvas()
        } else {
            return holder.lockCanvas()
        }
    }


    fun launchTextureRendering(textureView: TextureView) {
        val surfaceJob = Job() + Dispatchers.Default
        var color = Color.BLACK

        val textPaint = Paint()
        textPaint.color = Color.WHITE
        textPaint.textSize = 67f


        val contentPaint = Paint()
        contentPaint.color = Color.MAGENTA
        contentPaint.style = Paint.Style.STROKE
        contentPaint.strokeWidth = 4f

        lifecycleScope.launchWhenResumed {
            while (!textureView.isAvailable) {
                delay(1)
            }
            launch(surfaceJob) {
                val startTime = MonoClock.markNow()
                var currentFrameCount = 0L
                while (isActive && textureView.isAvailable) {
                    val canvas = textureView.lockCanvas()
                    color+=1
                    draw(canvas, color, textureView.width, textureView.height, contentPaint)
                    canvas.drawText("Texture View: ${(currentFrameCount++/startTime.elapsedNow().inSeconds).roundToInt()}fps", 30f, 125f, textPaint)
                    textureView.unlockCanvasAndPost(canvas)
                }
            }
        }
    }
}

fun draw(canvas: Canvas, color: Int, w: Int, h: Int, paint: Paint) {
    canvas.drawColor(color)
    canvas.drawLine(0f, 0f, w.toFloat(), h.toFloat(), paint)
}
