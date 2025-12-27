package com.example.shadowsprint.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import kotlin.random.Random

abstract class Obstacle(var x: Float, var y: Float, val width: Int, val height: Int) {
    var active = true
    var isSliced = false
    var sliceOffset = 0f
    
    abstract fun draw(canvas: Canvas, paint: Paint)
    fun getRect(): Rect = Rect(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt())
    
    open fun onSlice() {
        isSliced = true
        active = false
    }
}

class Block(x: Float, y: Float) : Obstacle(x, y, 100, 100) {
    override fun draw(canvas: Canvas, paint: Paint) {
        paint.color = Color.CYAN
        if (isSliced) {
            sliceOffset += 10f
            // Draw top half
            canvas.drawRect(x - sliceOffset, y - sliceOffset, x + width - sliceOffset, y + height / 2 - 5, paint)
            // Draw bottom half
            canvas.drawRect(x + sliceOffset, y + height / 2 + 5, x + width + sliceOffset, y + height + sliceOffset, paint)
        } else {
            canvas.drawRect(x, y, x + width, y + height, paint)
        }
    }
}

class Spike(x: Float, y: Float) : Obstacle(x, y, 80, 80) {
    override fun draw(canvas: Canvas, paint: Paint) {
        paint.color = Color.MAGENTA
        if (isSliced) {
            sliceOffset += 10f
            // Draw diagonal halves
            canvas.drawRect(x - sliceOffset, y, x + width / 2 - sliceOffset, y + height, paint)
            canvas.drawRect(x + width / 2 + sliceOffset, y, x + width + sliceOffset, y + height, paint)
        } else {
            canvas.drawRect(x, y, x + width, y + height, paint)
        }
    }
}

class ObstacleManager(private val screenWidth: Int, private val screenHeight: Int) {
    val obstacles = mutableListOf<Obstacle>()
    private val paint = Paint()
    private val speed = 15f
    private var spawnTimer = 0
    private val groundY = screenHeight - 450f 

    fun update(worldSpeed: Float) {
        spawnTimer++
        // Spawn random obstacles
        if (spawnTimer > 100) { 
             if (Random.nextInt(10) > 6) {
                spawnObstacle()
                spawnTimer = 0
             }
        }

        val iterator = obstacles.iterator()
        while (iterator.hasNext()) {
            val obs = iterator.next()
            obs.x -= worldSpeed
            if (obs.isSliced && obs.sliceOffset > 200) {
                iterator.remove()
                continue
            }
            if (!obs.isSliced && obs.x + obs.width < 0) {
                iterator.remove()
            }
        }
    }

    private fun spawnObstacle() {
        val type = Random.nextInt(2)
        // Adjust Y to sit on ground properly
        // Player ground is at (screenHeight - 100 - PlayerHeight)
        // If ground level is screenHeight - 100:
        
        if (type == 0) {
            // Block (Standard jump over)
            obstacles.add(Block(screenWidth.toFloat(), groundY - 100))
        } else {
            // Spike (Maybe smaller or different placement)
            obstacles.add(Spike(screenWidth.toFloat(), groundY - 80))
        }
    }

    fun draw(canvas: Canvas) {
        for (obs in obstacles) {
            obs.draw(canvas, paint)
        }
    }
}
