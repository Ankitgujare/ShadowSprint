package com.example.shadowsprint.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.random.Random

class Particle(var x: Float, var y: Float, color: Int) {
    private val paint = Paint().apply { this.color = color }
    private val angle = Random.nextDouble(0.0, Math.PI * 2)
    private val speed = Random.nextFloat() * 10f + 2f
    private var size = Random.nextFloat() * 15f + 5f
    private var alpha = 255
    
    var active = true

    fun update() {
        x += (Math.cos(angle) * speed).toFloat()
        y += (Math.sin(angle) * speed).toFloat()
        
        alpha -= 10
        if (alpha <= 0) {
            alpha = 0
            active = false
        }
        paint.alpha = alpha
        size *= 0.9f
    }

    fun draw(canvas: Canvas) {
        canvas.drawCircle(x, y, size, paint)
    }
}
