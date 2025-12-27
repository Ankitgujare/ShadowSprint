package com.example.shadowsprint.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect

class Projectile(var x: Float, var y: Float, var velocityX: Float = 25f, var velocityY: Float = 0f) {
    var radius = 20f
    var width = radius * 2
        get() = radius * 2
        set(value) { field = value; radius = value / 2 }
    var height = radius * 2
        get() = radius * 2
        set(value) { field = value; radius = value / 2 } // Simplified
        
    var color: Int
        get() = paint.color
        set(value) { paint.color = value }
        
    private val paint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    var active = true

    fun update() {
        x += velocityX
        y += velocityY
    }

    fun draw(canvas: Canvas) {
        // Draw glow
        paint.setShadowLayer(10f, 0f, 0f, color)
        if (height > width * 2) {
             // Wave shape
             canvas.drawRect(x - width/2, y - height/2, x + width/2, y + height/2, paint)
        } else {
             canvas.drawCircle(x, y, radius, paint)
        }
        paint.setShadowLayer(0f,0f,0f,0)
    }

    val hitbox: Rect
        get() = Rect((x - width/2).toInt(), (y - height/2).toInt(), (x + width/2).toInt(), (y + height/2).toInt())
}
