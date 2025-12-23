package com.example.shadowsprint.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

class BossEnemy(private val screenWidth: Float, private val screenHeight: Float) : Enemy(screenWidth, screenHeight / 2 - 100, 200, 200) {
    
    var maxHealth = 20
    var health = maxHealth
    private var moveDirection = 1
    private val speed = 6f
    private val paint = Paint()
    
    // Boss States
    private var isEnraged = false
    private var attackTimer = 0
    private var phaseTimer = 0
    
    override fun update(playerSpeed: Float) {
        phaseTimer++
        
        // Phase 2 Check
        if (!isEnraged && health < maxHealth / 2) {
            isEnraged = true
        }

        // Boss stays on screen, moving up and down
        if (x > screenWidth - 350) {
            x -= 4f // Enter screen slowly
        } else {
            // Bob up and down (Faster if enraged)
            val currentSpeed = if (isEnraged) speed * 1.5f else speed
            y += currentSpeed * moveDirection
            
            if (y < 150 || y > screenHeight - 250) {
                moveDirection *= -1
            }
        }
        
        // Enraged Attack: Fire projectiles
        if (isEnraged) {
            attackTimer++
            if (attackTimer > 90) { // Every 1.5 seconds
                fireProjectile()
                attackTimer = 0
            }
        }
    }
    
    private fun fireProjectile() {
        // This is handled by GameManager usually, but we can signal it or return a value
        // For now, let's assume we can trigger an event or GameManager checks a flag
    }

    override fun draw(canvas: Canvas) {
        val time = System.currentTimeMillis()
        val shake = if (isEnraged) (Math.sin(time / 50.0) * 5f).toFloat() else 0f
        
        val bossX = x + shake
        val bossY = y + shake

        // Sketchy Paint
        val sketchyStroke = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 10f
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }
        
        val bodyFill = Paint().apply {
            color = if (isEnraged) Color.rgb(60, 0, 0) else Color.DKGRAY
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // Body (Main block)
        canvas.drawRect(bossX, bossY, bossX + width, bossY + height, bodyFill)
        canvas.drawRect(bossX, bossY, bossX + width, bossY + height, sketchyStroke)
        
        // Eyes (Glowing Red)
        val eyeColor = if (isEnraged) Color.RED else Color.rgb(200, 0, 0)
        val eyePaint = Paint().apply { color = eyeColor; style = Paint.Style.FILL; isAntiAlias = true }
        
        // Glow effect if enraged
        if (isEnraged) {
            eyePaint.setShadowLayer(25f, 0f, 0f, Color.RED)
        }
        
        canvas.drawCircle(bossX + 60, bossY + 70, 25f, eyePaint)
        canvas.drawCircle(bossX + 140, bossY + 70, 25f, eyePaint)
        
        // Mouth (Grimace)
        val mouthPath = android.graphics.Path()
        mouthPath.moveTo(bossX + 50, bossY + 140)
        if (isEnraged) {
            mouthPath.quadTo(bossX + 100, bossY + 170, bossX + 150, bossY + 140) // Frown
        } else {
            mouthPath.lineTo(bossX + 150, bossY + 140) // Flat
        }
        canvas.drawPath(mouthPath, sketchyStroke)
        
        // Horns/Spikes (Boss uniqueness)
        val spikePath = android.graphics.Path()
        spikePath.moveTo(bossX + 20, bossY)
        spikePath.lineTo(bossX + 40, bossY - 50)
        spikePath.lineTo(bossX + 60, bossY)
        
        spikePath.moveTo(bossX + 140, bossY)
        spikePath.lineTo(bossX + 160, bossY - 50)
        spikePath.lineTo(bossX + 180, bossY)
        canvas.drawPath(spikePath, sketchyStroke)
        
        // Health Bar
        drawHealthBar(canvas)
    }

    private fun drawHealthBar(canvas: Canvas) {
        val barWidth = screenWidth * 0.6f
        val barHeight = 40f
        val healthPct = health.toFloat() / maxHealth.toFloat()
        
        // Center at bottom of screen
        val barX = (screenWidth - barWidth) / 2
        val barY = screenHeight - 100f
        
        val barPaint = Paint().apply { isAntiAlias = true }
        
        // Label
        barPaint.color = Color.WHITE
        barPaint.textSize = 40f
        canvas.drawText("SHADOW OVERLORD", barX, barY - 20, barPaint)
        
        // Background
        barPaint.color = Color.argb(100, 0, 0, 0)
        canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight, barPaint)
        
        // Health
        barPaint.color = if (isEnraged) Color.RED else Color.GREEN
        canvas.drawRect(barX, barY, barX + (barWidth * healthPct), barY + barHeight, barPaint)
        
        // Border
        barPaint.color = Color.WHITE
        barPaint.style = Paint.Style.STROKE
        barPaint.strokeWidth = 4f
        canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight, barPaint)
    }

    fun shouldFireProjectile(): Boolean {
        if (!isEnraged) return false
        return attackTimer == 0 // Triggered when timer resets in update
    }

    override fun onHit() {
        health--
        if (health <= 0) {
            super.onHit()
        }
    }
}
