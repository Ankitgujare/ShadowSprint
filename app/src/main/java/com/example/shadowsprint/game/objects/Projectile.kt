package com.example.shadowsprint.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect

class Projectile(var x: Float, var y: Float, var velocityX: Float = 25f, var velocityY: Float = 0f) {
    private val size = 20f
    val width = size * 2
    val height = size * 2
    private val paint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    var active = true

    fun update() {
        x += velocityX
        y += velocityY
        // Simple rotation visual could go here
    }

    fun draw(canvas: Canvas) {
        canvas.drawCircle(x, y, size, paint)
    }

    val hitbox: Rect
        get() = Rect((x - size).toInt(), (y - size).toInt(), (x + size).toInt(), (y + size).toInt())
}
