package com.example.shadowsprint.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect

abstract class Enemy(var x: Float, var y: Float, val width: Int, val height: Int) {
    var active = true
    abstract fun update(playerSpeed: Float)
    abstract fun draw(canvas: Canvas)
    
    fun getRect(): Rect = Rect(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt())
    
    open fun onHit() {
        active = false
        // Play sound or particle effect?
    }
}

class RunnerEnemy(startX: Float, startY: Float) : Enemy(startX, startY, 80, 120) {
    private val paint = Paint().apply { color = Color.RED }
    
    override fun update(playerSpeed: Float) {
        x -= (playerSpeed + 5f) // Runs towards player
    }

    override fun draw(canvas: Canvas) {
        // Draw Simple Enemy Shape
        canvas.drawRect(x, y, x + width, y + height, paint)
        // Red eyes
        paint.color = Color.YELLOW
        canvas.drawCircle(x + 20, y + 30, 5f, paint)
        paint.color = Color.RED
    }
}

class FlyingEnemy(startX: Float, startY: Float) : Enemy(startX, startY, 80, 60) {
    private val paint = Paint().apply { color = Color.MAGENTA }
    private var angle = 0.0

    override fun update(playerSpeed: Float) {
        x -= (playerSpeed + 8f) // Faster
        y += Math.sin(angle).toFloat() * 2f // Bobbing motion
        angle += 0.1
    }

    override fun draw(canvas: Canvas) {
        // Bird/Bat shape
        canvas.drawOval(x, y, x + width, y + height, paint)
    }
}
