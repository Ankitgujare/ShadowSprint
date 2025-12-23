package com.example.shadowsprint.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class Background(private val screenWidth: Int, private val screenHeight: Int) {
    private val stars = mutableListOf<Star>()
    private val paint = Paint()

    init {
        for (i in 0..50) {
            stars.add(Star(
                (Math.random() * screenWidth).toFloat(),
                (Math.random() * screenHeight).toFloat(),
                (Math.random() * 5 + 1).toFloat()
            ))
        }
    }

    fun update() {
        for (star in stars) {
            star.x -= star.speed
            if (star.x < 0) {
                star.x = screenWidth.toFloat()
                star.y = (Math.random() * screenHeight).toFloat()
            }
        }
    }

    fun draw(canvas: Canvas) {
        paint.color = Color.DKGRAY
        canvas.drawRect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat(), paint) // Fill background

        paint.color = Color.WHITE
        for (star in stars) {
             canvas.drawCircle(star.x, star.y, 2f, paint)
        }
        
        // Draw Ground
        paint.color = Color.GRAY
        canvas.drawRect(0f, screenHeight - 100f, screenWidth.toFloat(), screenHeight.toFloat(), paint)
    }

    class Star(var x: Float, var y: Float, val speed: Float)
}
