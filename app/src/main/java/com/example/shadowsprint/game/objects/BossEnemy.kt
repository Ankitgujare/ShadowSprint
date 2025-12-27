package com.example.shadowsprint.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.abs
import kotlin.random.Random

class BossEnemy(x: Float, y: Float) : Enemy(x, y, 150f, 250f) {
    
    var maxHealth = 500
    private var phase = 1
    private var attackTimer = 0
    private var teleportTimer = 0
    private val paint = Paint()

    init {
        health = 500
    }

    override fun update(player: Player, platforms: List<RectF>) {
        if (state == EnemyState.DEAD) return
        
        // Phase logic
        if (health < 250 && phase == 1) {
            phase = 2
            // Teleport away immediately
            x = player.x + 400
            y = player.y - 400
            velY = 0f
        }
        
        // Physics
        velY += gravity
        
        // AI
        if (state == EnemyState.ATTACK) {
            attackTimer--
            if (attackTimer <= 0) {
                state = EnemyState.IDLE
                attackTimer = 100
                // Check hit
                if (RectF.intersects(hitbox, player.hitbox)) {
                    player.onHit(20)
                }
            }
        } else {
             val dist = player.x - x
             
             if (phase == 1) {
                 // Slow walk and heavy slash
                 if (abs(dist) < 200) {
                     state = EnemyState.ATTACK
                     attackTimer = 60
                     // Telegraph
                 } else {
                     velX = if (dist > 0) 4f else -4f
                 }
             } else {
                 // Teleport spam
                 teleportTimer++
                 if (teleportTimer > 120) {
                     teleportTimer = 0
                     x = player.x + (if (Random.nextBoolean()) 200f else -200f)
                     y = player.y - 100f
                     state = EnemyState.ATTACK
                     attackTimer = 30
                 } else {
                     velX = 0f
                 }
             }
        }
        
        y += velY
        x += velX
        checkCollisions(platforms)
    }

    override fun draw(canvas: Canvas) {
        if (state == EnemyState.DEAD) return
        
        paint.color = if (phase == 1) Color.rgb(100, 0, 0) else Color.rgb(50, 0, 50)
        canvas.drawRect(x, y, x + w, y + h, paint)
        
        // Name
        paint.color = Color.WHITE
        paint.textSize = 30f
        canvas.drawText("IRON RONIN", x, y - 40, paint)
        
        // Boss HP Bar
        paint.color = Color.RED
        canvas.drawRect(x, y - 20, x + w, y - 10, paint)
        paint.color = Color.MAGENTA
        canvas.drawRect(x, y - 20, x + (w * (health/500f)), y - 10, paint)
    }
}
