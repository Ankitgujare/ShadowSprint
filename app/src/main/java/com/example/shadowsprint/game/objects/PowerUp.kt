package com.example.shadowsprint.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect

enum class PowerUpType {
    SHIELD, MAGNET, SPEED
}

class PowerUp(var x: Float, var y: Float, val type: PowerUpType) {
    val width = 60
    val height = 60
    var active = true

    fun update(speed: Float) {
        x -= speed
        if (x + width < 0) {
            active = false
        }
    }

    fun draw(canvas: Canvas) {
        val paint = Paint()
        paint.style = Paint.Style.FILL
        
        when (type) {
            PowerUpType.SHIELD -> paint.color = Color.CYAN
            PowerUpType.MAGNET -> paint.color = Color.RED
            PowerUpType.SPEED -> paint.color = Color.GREEN
        }
        
        canvas.drawCircle(x + width / 2, y + height / 2, width / 2f, paint)
        
        // Icon/Text (Simplified)
        paint.color = Color.WHITE
        paint.textSize = 30f
        paint.textAlign = Paint.Align.CENTER
        val label = when (type) {
            PowerUpType.SHIELD -> "S"
            PowerUpType.MAGNET -> "M"
            PowerUpType.SPEED -> ">>"
        }
        val textY = y + height / 2 + 10
        canvas.drawText(label, x + width / 2, textY, paint)
    }

    fun getRect(): Rect {
        return Rect(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt())
    }
}
