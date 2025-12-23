package com.example.shadowsprint.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect

class Coin(var x: Float, var y: Float) {
    val width = 40
    val height = 40
    var active = true
    
    // Magnet logic
    private val magnetSpeed = 30f

    fun update(speed: Float, playerX: Float, playerY: Float, isMagnetActive: Boolean) {
        if (isMagnetActive) {
            // Move towards player
            val dx = playerX - x
            val dy = playerY - y
            val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
            
            if (dist > 0) {
                x += (dx / dist) * magnetSpeed
                y += (dy / dist) * magnetSpeed
            }
        } else {
            // Normal scrolling
            x -= speed
        }

        if (x + width < 0) {
            active = false
        }
    }

    fun draw(canvas: Canvas) {
        val paint = Paint()
        paint.color = Color.YELLOW
        paint.style = Paint.Style.FILL
        
        canvas.drawCircle(x + width / 2, y + height / 2, width / 2f, paint)
        
        // Inner detail
        paint.color = Color.parseColor("#FFD700") // Gold
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas.drawCircle(x + width / 2, y + height / 2, width / 2f - 5, paint)
    }

    fun getRect(): Rect {
        return Rect(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt())
    }
}
