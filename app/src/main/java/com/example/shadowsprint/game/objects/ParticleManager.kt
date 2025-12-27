package com.example.shadowsprint.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.random.Random

enum class ParticleType {
    BLOOD, SPARK, SMOKE, SHOCKWAVE
}

class Particle(var x: Float, var y: Float, var velX: Float, var velY: Float, val type: ParticleType, var life: Int)

class ParticleManager {
    private val particles = mutableListOf<Particle>()
    private val paint = Paint()

    fun spawn(x: Float, y: Float, type: ParticleType, count: Int) {
        repeat(count) {
            val vx = (Random.nextFloat() - 0.5f) * 10f
            val vy = (Random.nextFloat() - 0.5f) * 10f
            val life = 20 + Random.nextInt(20)
            
            when(type) {
                ParticleType.BLOOD -> particles.add(Particle(x, y, vx, vy - 5f, type, life))
                ParticleType.SPARK -> particles.add(Particle(x, y, vx * 2, vy * 2, type, 10))
                ParticleType.SMOKE -> particles.add(Particle(x, y, vx * 0.5f, -2f, type, 40))
                ParticleType.SHOCKWAVE -> particles.add(Particle(x, y, 15f, 0f, type, 30)) // Special handling in draw
            }
        }
    }
    
    fun spawnShockwave(x: Float, y: Float, direction: Int) {
         // Special case: Single shockwave particle that grows
         particles.add(Particle(x, y, 15f * direction, 0f, ParticleType.SHOCKWAVE, 40))
    }

    fun update() {
        val iter = particles.iterator()
        while (iter.hasNext()) {
            val p = iter.next()
            p.x += p.velX
            p.y += p.velY
            p.life--
            
            // Gravity for blood
            if (p.type == ParticleType.BLOOD) p.velY += 1f
            
            if (p.life <= 0) iter.remove()
        }
    }

    fun draw(canvas: Canvas) {
        for (p in particles) {
             when(p.type) {
                 ParticleType.BLOOD -> {
                     paint.color = Color.RED
                     paint.style = Paint.Style.FILL
                     canvas.drawCircle(p.x, p.y, 4f, paint)
                 }
                 ParticleType.SPARK -> {
                     paint.color = Color.YELLOW
                     canvas.drawCircle(p.x, p.y, 3f, paint)
                 }
                 ParticleType.SMOKE -> {
                     paint.color = Color.DKGRAY
                     paint.alpha = 150
                     canvas.drawCircle(p.x, p.y, 8f, paint)
                 }
                 ParticleType.SHOCKWAVE -> {
                     paint.color = Color.CYAN
                     paint.style = Paint.Style.STROKE
                     paint.strokeWidth = 5f
                     paint.alpha = (p.life * 5).coerceAtMost(255)
                     // Draw a vertical arc/wave
                     canvas.drawArc(p.x - 20, p.y - 60, p.x + 20, p.y + 10, 0f, 360f, false, paint)
                 }
             }
        }
        paint.alpha = 255 // Reset
    }
}
