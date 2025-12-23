package com.example.shadowsprint.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import kotlin.random.Random

abstract class Obstacle(var x: Float, var y: Float, val width: Int, val height: Int) {
    abstract fun draw(canvas: Canvas, paint: Paint)
    fun getRect(): Rect = Rect(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt())
}

class Block(x: Float, y: Float) : Obstacle(x, y, 100, 100) {
    override fun draw(canvas: Canvas, paint: Paint) {
        paint.color = Color.RED
        canvas.drawRect(x, y, x + width, y + height, paint)
    }
}

class Spike(x: Float, y: Float) : Obstacle(x, y, 80, 80) {
    override fun draw(canvas: Canvas, paint: Paint) {
        paint.color = Color.MAGENTA
        // Simplified spike as triangle/rect for now
        canvas.drawRect(x, y, x + width, y + height, paint)
    }
}

class ObstacleManager(private val screenWidth: Int, private val screenHeight: Int) {
    val obstacles = mutableListOf<Obstacle>()
    private val paint = Paint()
    private val speed = 15f
    private var spawnTimer = 0
    private val groundY = screenHeight - 100f // Align with player ground + player height roughly

    fun update() {
        spawnTimer++
        // Spawn random obstacles
        if (spawnTimer > 60) { // Every 60 frames approx 1 sec
             if (Random.nextBoolean()) {
                spawnObstacle()
                spawnTimer = 0
             }
        }

        val iterator = obstacles.iterator()
        while (iterator.hasNext()) {
            val obs = iterator.next()
            obs.x -= speed
            if (obs.x + obs.width < 0) {
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
