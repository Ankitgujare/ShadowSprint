package com.example.shadowsprint.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.example.shadowsprint.game.objects.*

class GameManager(private val context: Context, private val screenWidth: Int, private val screenHeight: Int) {

    // Entities
    val player = Player(screenWidth, screenHeight)
    private val background = Background(screenWidth, screenHeight)
    private val enemies = mutableListOf<Enemy>()
    
    // Level Data
    private val platforms = mutableListOf<RectF>()
    
    // Camera
    private var cameraX = 0f
    private var cameraY = 0f
    
    // Game State
    var isGameOver = false
    private var score = 0
    private var stageCompleted = false
    
    // Visuals
    private val paint = Paint().apply {
        color = Color.WHITE
        textSize = 60f
        isAntiAlias = true
    }

    init {
        buildStage1()
    }

    private fun buildStage1() {
        platforms.clear()
        enemies.clear()
        
        // Continuous Floor Layer (No gaps as requested)
        platforms.add(RectF(-500f, screenHeight - 100f, 8000f, screenHeight + 200f))
        
        // Walls & Platforms Challenges
        platforms.add(RectF(1000f, screenHeight - 400f, 1500f, screenHeight - 350f)) // Platform 1
        platforms.add(RectF(2500f, screenHeight - 600f, 3000f, screenHeight - 550f)) // High Platform
        
        // Walls (Obstacles to jump/climb)
        platforms.add(RectF(4000f, screenHeight - 500f, 4200f, screenHeight - 100f)) 
        
        // Boss Arena (x=6000)
        platforms.add(RectF(5800f, screenHeight - 800f, 6000f, screenHeight - 100f)) // Gate
        platforms.add(RectF(7500f, screenHeight - 800f, 7700f, screenHeight - 100f)) // Back wall
        
        // Enemies (More density)
        // Wave 1
        enemies.add(BladeSoldier(1200f, screenHeight - 300f))
        enemies.add(BladeSoldier(1800f, screenHeight - 300f))
        
        // Wave 2 (Ambush)
        enemies.add(BladeSoldier(2800f, screenHeight - 700f)) // On platform
        enemies.add(BladeSoldier(3200f, screenHeight - 300f))
        
        // Wave 3 (Gauntlet)
        enemies.add(BladeSoldier(4500f, screenHeight - 300f))
        enemies.add(BladeSoldier(4700f, screenHeight - 300f))
        enemies.add(BladeSoldier(5000f, screenHeight - 300f))
        
        // Boss
        enemies.add(BossEnemy(6500f, screenHeight - 300f))
    }

    fun update() {
        if (isGameOver) return

        // Update Player
        player.update(platforms)
        
        if (player.state == PlayerState.DEAD) {
            isGameOver = true
        }
        
        // Update Enemies
        val iter = enemies.iterator()
        while(iter.hasNext()) {
            val e = iter.next()
            if (e.health <= 0 && e.state == EnemyState.DEAD) {
                if (e is BossEnemy) stageCompleted = true
                iter.remove()
                score += 100
                player.onKill()
                continue
            }
            
            // Activate if close to camera to save perf/logic
            if (kotlin.math.abs(e.x - player.x) < 2000) {
                 e.update(player, platforms)
                 
                 // Player Hits Enemy
                 if (player.isAttacking && RectF.intersects(player.swordHitbox, e.hitbox)) {
                     // Only hit if enemy not HURT (i-frames)
                     if (e.state != EnemyState.HURT) {
                         val dmg = 35 + (player.comboCount * 10)
                         e.onHit(dmg, if (player.facingRight) 15f else -15f)
                     }
                 }
            }
        }
        
        // Camera Follow Logic (Smooth)
        val targetCamX = player.x - screenWidth / 2 + player.width / 2
        val targetCamY = player.y - screenHeight / 2 + player.height / 2
        
        cameraX += (targetCamX - cameraX) * 0.1f
        
        // Clamp Camera Y
        if (targetCamY < 0) {
             cameraY += (targetCamY - cameraY) * 0.1f
        } else {
             cameraY += (0 - cameraY) * 0.1f
        }
        
        background.update(player.velX)
        
        if (player.y > screenHeight + 500) {
            isGameOver = true
        }
    }

    fun draw(canvas: Canvas) {
        background.draw(canvas)
        
        canvas.save()
        canvas.translate(-cameraX, -cameraY)
        
        // Draw Level
        paint.color = Color.DKGRAY
        for (plat in platforms) {
            canvas.drawRect(plat, paint)
            val neonPaint = Paint().apply { 
                color = Color.rgb(100, 0, 100) 
                style = Paint.Style.STROKE 
                strokeWidth = 5f 
            }
            canvas.drawRect(plat, neonPaint)
        }
        
        // Draw Enemies
        for (e in enemies) {
             // Culling
             if (kotlin.math.abs(e.x - (cameraX + screenWidth/2)) < screenWidth) {
                 e.draw(canvas)
             }
        }
        
        player.draw(canvas)
        canvas.restore()
        
        // UI Layer
        drawUI(canvas)
    }
    
    private fun drawUI(canvas: Canvas) {
        // Score
        paint.color = Color.WHITE
        paint.textSize = 60f
        paint.style = Paint.Style.FILL
        canvas.drawText("SCORE: $score", 50f, 80f, paint)
        
        // Player Health Bar
        val hpWidth = 400f
        paint.color = Color.DKGRAY
        canvas.drawRect(50f, 100f, 50f + hpWidth, 140f, paint)
        paint.color = if(player.health > 30) Color.GREEN else Color.RED
        val hpPct = player.health / player.maxHealth.toFloat()
        canvas.drawRect(50f, 100f, 50f + (hpWidth * hpPct), 140f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = Color.WHITE
        paint.strokeWidth = 3f
        canvas.drawRect(50f, 100f, 50f + hpWidth, 140f, paint)
        
        // Combo Counter
        if (player.comboCount > 0) {
            paint.style = Paint.Style.FILL
            paint.color = Color.YELLOW
            paint.textSize = 100f
            canvas.drawText("${player.comboCount} HITS!", screenWidth - 400f, 150f, paint)
        }

        if (isGameOver) {
            paint.color = Color.RED
            paint.textSize = 100f
            val text = "YOU DIED"
            val textWidth = paint.measureText(text)
            canvas.drawText(text, (screenWidth - textWidth) / 2, screenHeight / 2f, paint)
        }
        
        if (stageCompleted) {
            paint.color = Color.CYAN
            paint.textSize = 100f
            val text = "STAGE CLEARED"
            val textWidth = paint.measureText(text)
            canvas.drawText(text, (screenWidth - textWidth) / 2, screenHeight / 2f, paint)
        }
    }
    
    // --- INPUTS ---
    fun setJoystickLines(x: Float, y: Float) {
        if (!isGameOver) player.setJoystickInput(x, y)
    }
    
    fun onJump() {
        if (!isGameOver) player.jump()
        else restart()
    }
    
    fun onAttack() {
        if (!isGameOver) player.attack()
    }
    
    fun onDash() {
        if (!isGameOver) player.dash()
    }
    
    private fun restart() {
        buildStage1() // Reset level
        isGameOver = false
        score = 0
        stageCompleted = false
        player.health = player.maxHealth
        player.x = 200f
        player.y = screenHeight - 300f
        player.velX = 0f
        player.velY = 0f
        cameraX = 0f
        cameraY = 0f
        player.state = PlayerState.IDLE
    }
}
