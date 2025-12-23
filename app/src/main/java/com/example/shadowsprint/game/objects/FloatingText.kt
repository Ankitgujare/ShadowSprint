package com.example.shadowsprint.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class FloatingText(var x: Float, var y: Float, val text: String, val color: Int) {
    private var alpha = 255
    private var velocityY = -2f
    private val paint = Paint().apply {
        this.color = color
        textSize = 50f
        isAntiAlias = true
    }
    
    var active = true

    fun update() {
        y += velocityY
        alpha -= 5
        if (alpha <= 0) {
            alpha = 0
            active = false
        }
        paint.alpha = alpha
    }

    fun draw(canvas: Canvas) {
        canvas.drawText(text, x, y, paint)
    }
}
