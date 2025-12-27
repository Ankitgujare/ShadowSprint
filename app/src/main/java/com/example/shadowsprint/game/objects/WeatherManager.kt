package com.example.shadowsprint.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.random.Random

class WeatherManager(private val screenWidth: Int, private val screenHeight: Int) {
    
    private val rainDrops = mutableListOf<RainDrop>()
    private val paint = Paint().apply {
        color = Color.CYAN
        strokeWidth = 2f
        alpha = 100
        isAntiAlias = true
    }
    
    // Simple rain
    class RainDrop(var x: Float, var y: Float, var speed: Float)

    fun setWeather(ignored: Any?) {
        // No-op for now
    }

    fun update() {
        if (Random.nextInt(10) > 2) { 
             rainDrops.add(RainDrop(Random.nextFloat() * screenWidth, -50f, Random.nextFloat() * 20 + 20))
        }
        
        val iter = rainDrops.iterator()
        while(iter.hasNext()) {
             val r = iter.next()
             r.y += r.speed
             r.x -= 2f // Wind
             if (r.y > screenHeight) iter.remove()
        }
    }

    fun draw(canvas: Canvas) {
        for (r in rainDrops) {
            canvas.drawLine(r.x, r.y, r.x - 5, r.y + 15, paint)
        }
    }
}
