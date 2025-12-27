package com.example.shadowsprint.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class Pulse(val x: Float, val y: Float, val color: Int, val maxRadius: Float) {
    var currentRadius = 0f
    var active = true
    private val speed = maxRadius / 15f // Expand in 15 frames

    fun update() {
        currentRadius += speed
        if (currentRadius >= maxRadius) {
            active = false
        }
    }

    fun draw(canvas: Canvas) {
        val paint = Paint().apply {
            color = this@Pulse.color
            style = Paint.Style.STROKE
            strokeWidth = 10f * (1f - currentRadius / maxRadius) // Fades thickness
            alpha = (255 * (1f - currentRadius / maxRadius)).toInt()
            isAntiAlias = true
        }
        canvas.drawCircle(x, y, currentRadius, paint)
    }
}
