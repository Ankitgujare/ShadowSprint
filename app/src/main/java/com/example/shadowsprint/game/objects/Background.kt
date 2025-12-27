package com.example.shadowsprint.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect

class Background(private val screenWidth: Int, private val screenHeight: Int) {
    
    private val starPaint = Paint().apply { color = Color.WHITE; alpha = 100 }
    private val stars = List(50) { 
        Pair(Math.random().toFloat() * screenWidth, Math.random().toFloat() * screenHeight) 
    }
    
    // Building Silhouette
    private val buildings = List(10) {
        val w = 100 + Math.random().toFloat() * 150
        val h = 200 + Math.random().toFloat() * 400
        Rect(0, 0, w.toInt(), h.toInt())
    }
    private var scrollX = 0f
    
    fun update(speed: Float) {
        scrollX -= speed * 0.5f // Parallax layer
        if (scrollX < -screenWidth) scrollX = 0f
    }
    
    fun draw(canvas: Canvas) {
        // Deep Dark Void Sky
        canvas.drawColor(Color.rgb(10, 5, 20)) 
        
        // Stars
        for (star in stars) {
            canvas.drawCircle(star.first, star.second, 2f, starPaint)
        }
        
        // Buildings (Rear)
        val bPaint = Paint().apply { color = Color.rgb(30, 20, 40) }
        var currentX = scrollX
        for (i in 0 until 20) { // Repeat for infinite feel
             val b = buildings[i % buildings.size]
             val y = screenHeight - b.height()
             canvas.drawRect(currentX, y.toFloat(), currentX + b.width(), screenHeight.toFloat(), bPaint)
             
             // Windows
             val winPaint = Paint().apply { color = Color.rgb(100, 0, 100); alpha = 150 }
             if (i % 3 == 0) {
                 canvas.drawRect(currentX + 20, y + 20f, currentX + b.width() - 20, y + 50f, winPaint)
             }
             
             currentX += b.width()
        }
    }
    
    fun setLevelTheme(ignored: Any?) {} // Stub
}
