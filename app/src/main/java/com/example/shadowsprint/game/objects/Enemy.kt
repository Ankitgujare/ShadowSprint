package com.example.shadowsprint.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.example.shadowsprint.game.GlobalEnemyMind
import kotlin.math.abs
import kotlin.random.Random

enum class EnemyState {
    IDLE, PATROL, CHASE, ATTACK, HURT, DEAD
}

abstract class Enemy(var x: Float, var y: Float, val w: Float, val h: Float) {
    var health = 100
    var state = EnemyState.IDLE
    var velX = 0f
    var velY = 0f
    var facingRight = false
    
    // Physics
    val gravity = 1.0f
    var onGround = false
    
    val hitbox: RectF
        get() = RectF(x, y, x + w, y + h)

    abstract fun update(player: Player, platforms: List<RectF>)
    abstract fun draw(canvas: Canvas)
    
    open fun onHit(damage: Int, knockbackX: Float) {
        if (state == EnemyState.DEAD) return
        health -= damage
        if (health <= 0) {
            health = 0
            state = EnemyState.DEAD
        } else {
            state = EnemyState.HURT
            velX = knockbackX
            velY = -15f
        }
    }
    
    protected fun checkCollisions(platforms: List<RectF>) {
        onGround = false
        val pRect = hitbox
        for (plat in platforms) {
            if (RectF.intersects(pRect, plat)) {
                val overlapBottom = (pRect.bottom - plat.top)
                val overlapTop = (plat.bottom - pRect.top)
                
                // Land on floor
                if (overlapBottom < overlapTop && overlapBottom < 50f && velY >= 0) {
                    y = plat.top - h
                    velY = 0f
                    onGround = true
                }
            }
        }
    }
}

class BladeSoldier(x: Float, y: Float) : Enemy(x, y, 80f, 150f) {
    
    private val speed = 8f
    private val detectionRange = 800f
    private val attackRange = 120f
    private var attackTimer = 0
    private var hurtTimer = 0
    private val paint = Paint()

    override fun update(player: Player, platforms: List<RectF>) {
        if (state == EnemyState.DEAD) return
        
        // Physics
        velY += gravity
        
        if (state == EnemyState.HURT) {
            hurtTimer++
            if (hurtTimer > 20) {
                hurtTimer = 0
                state = EnemyState.CHASE
            }
        } else {
            // AI
            val dist = player.x - x
            
            if (abs(dist) < detectionRange && abs(player.y - y) < 200) {
                // Chase
                if (abs(dist) < attackRange) {
                    // Attack
                    if (attackTimer == 0) {
                        state = EnemyState.ATTACK
                        attackTimer = 60 // 1s cooldown
                        
                        // AI Adaptation: Jump if player uses ground slashes often
                        if (GlobalEnemyMind.jumpChance > 0.2f && onGround && Random.nextFloat() < GlobalEnemyMind.jumpChance) {
                            velY = -20f // Jump Attack
                            onGround = false
                        }

                        // Deal damage immediately? Or mid-frame?
                        // Simple: if player in range at start of attack
                        if (player.hitbox.intersect(hitbox)) { // Collision
                             player.onHit(10)
                        }
                    }
                } else {
                    state = EnemyState.CHASE
                    velX = if (dist > 0) speed else -speed
                    facingRight = dist > 0
                }
            } else {
                state = EnemyState.IDLE
                velX = 0f
            }
            
            if (attackTimer > 0) attackTimer--
        }

        x += velX
        y += velY
        checkCollisions(platforms)
    }

    override fun draw(canvas: Canvas) {
        if (state == EnemyState.DEAD) return
        
        paint.color = when(state) {
            EnemyState.IDLE -> Color.GRAY
            EnemyState.CHASE -> Color.DKGRAY
            EnemyState.ATTACK -> Color.RED
            EnemyState.HURT -> Color.WHITE
            else -> Color.GRAY
        }
        
        canvas.drawRect(x, y, x + w, y + h, paint)
        
        // Eye
        paint.color = Color.CYAN
        val eyeX = if (facingRight) x + w - 20 else x + 10
        canvas.drawRect(eyeX, y + 30, eyeX + 10, y + 40, paint)
        
        // Enemy Sword
        if (state == EnemyState.ATTACK) {
             val swordPaint = Paint().apply {
                 color = Color.RED
                 strokeWidth = 6f
                 style = Paint.Style.STROKE
             }
             val sx = x + w/2
             val sy = y + h/2
             val ex = sx + (if(facingRight) 100 else -100)
             val ey = sy + 50
             canvas.drawLine(sx, sy, ex.toFloat(), ey.toFloat(), swordPaint)
        }
        
        // Health Bar
        if (health < 100) {
            paint.color = Color.RED
            canvas.drawRect(x, y - 20, x + w, y - 10, paint)
            paint.color = Color.GREEN
            canvas.drawRect(x, y - 20, x + (w * (health/100f)), y - 10, paint)
        }
    }
}
