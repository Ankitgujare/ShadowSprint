package com.example.shadowsprint.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.example.shadowsprint.game.objects.Background
import com.example.shadowsprint.game.objects.Enemy
import com.example.shadowsprint.game.objects.FlyingEnemy
import com.example.shadowsprint.game.objects.ObstacleManager
import com.example.shadowsprint.game.objects.Player
import com.example.shadowsprint.game.objects.Projectile
import com.example.shadowsprint.game.objects.RunnerEnemy
import com.example.shadowsprint.game.objects.BossEnemy
import com.example.shadowsprint.game.objects.Particle
import com.example.shadowsprint.game.objects.FloatingText
import com.example.shadowsprint.game.HapticManager
import kotlin.random.Random

class GameManager(private val context: Context, private val screenWidth: Int, private val screenHeight: Int) {

    private val player = Player(screenWidth, screenHeight)
    // Keep Obstacle Manager for static spikes? Or merge? keeping for now.
    private val obstacleManager = ObstacleManager(screenWidth, screenHeight)
    private val background = Background(screenWidth, screenHeight)
    
    // New Entities
    private val enemies = mutableListOf<Enemy>()
    private val projectiles = mutableListOf<Projectile>()
    private val particles = mutableListOf<Particle>()
    private val floatingTexts = mutableListOf<FloatingText>()
    
    // Items
    private val coins = mutableListOf<com.example.shadowsprint.game.objects.Coin>()
    private val powerUps = mutableListOf<com.example.shadowsprint.game.objects.PowerUp>()
    
    private var boss: BossEnemy? = null
    
    private var isGameOver = false
    private var score = 0
    private var highScore = 0
    private var distanceTraveled = 0f
    private var spawnTimer = 0
    
    // UX Features
    private val haptics = HapticManager(context)
    private var shakeIntensity = 0f
    private var shakeTimer = 0
    private var comboCount = 0
    private var comboTimer = 0
    private val comboMaxTime = 90 // 1.5 seconds @ 60fps
    
    private var impactFlashTimer = 0
    private var wasInAir = false
    
    private val prefs = context.getSharedPreferences("ShadowSprintPrefs", Context.MODE_PRIVATE)

    init {
        highScore = prefs.getInt("HIGH_SCORE", 0)
    }

    private val paint = Paint().apply {
        color = Color.WHITE
        textSize = 60f
        isAntiAlias = true
    }

    fun update() {
        if (isGameOver) return

        player.update()
        obstacleManager.update()
        background.update()
        
        // Update Projectiles
        val projIter = projectiles.iterator()
        while(projIter.hasNext()) {
            val p = projIter.next()
            p.update()
            if (p.x > screenWidth || p.x + p.width < 0) {
                projIter.remove()
                continue
            }
            
            // Projectile vs Player (for Boss projectiles)
            if (p.velocityX < 0 && Rect.intersects(p.hitbox, player.hitbox)) {
                if (!player.isInvincible) gameOver()
                projIter.remove()
            }
        }
        
        // Update Particles
        val partIter = particles.iterator()
        while(partIter.hasNext()) {
            val p = partIter.next()
            p.update()
            if (!p.active) partIter.remove()
        }
        
        // Update Boss
        if (boss != null) {
            boss?.update(0f) // Boss moves independently
            
            // Boss Projectiles (Special Boss-only projectiles)
            if (boss!!.shouldFireProjectile()) {
                // Calculate direction towards player
                val targetX = player.x + player.width / 2f
                val targetY = player.y + player.height / 2f
                val startX = boss!!.x
                val startY = boss!!.y + 100f // From boss's approximate center
                
                val dx = targetX - startX
                val dy = targetY - startY
                val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                
                val speed = 20f // Projectile speed
                val velocityX = (dx / distance) * speed
                val velocityY = (dy / distance) * speed
                
                projectiles.add(Projectile(startX, startY, velocityX, velocityY)) // Fires towards player
            }

            if (!boss!!.active) {
                 spawnParticles(boss!!.x + boss!!.width/2, boss!!.y + boss!!.height/2, Color.RED, 50)
                 boss = null
                 score += 1000 // Huge bonus
                 floatingTexts.add(FloatingText(screenWidth/2f, screenHeight/2f, "VICTORY!", Color.YELLOW))
            }
        }
        
        // Update Enemies
        spawnEnemies()
        val enemyIter = enemies.iterator()
        while(enemyIter.hasNext()) {
            val e = enemyIter.next()
            e.update(15f) // Base speed
            if (e.x + e.width < 0 || !e.active) enemyIter.remove()
        }
        
        // Update Items
        spawnItems()
        
        val scrollSpeed = if (player.isSpeedActive) 25f else 15f
        
        val coinIter = coins.iterator()
        while(coinIter.hasNext()) {
            val c = coinIter.next()
            c.update(scrollSpeed, player.x + 40, player.y + 80, player.isMagnetActive) // Center of player
            if (c.x + c.width < 0 || !c.active) coinIter.remove()
        }
        
        val powerIter = powerUps.iterator()
        while(powerIter.hasNext()) {
            val pu = powerIter.next()
            pu.update(scrollSpeed)
            if (pu.x + pu.width < 0 || !pu.active) powerIter.remove()
        }
        
        // Update Floating Texts
        val textIter = floatingTexts.iterator()
        while(textIter.hasNext()) {
            val t = textIter.next()
            t.update()
            if (!t.active) textIter.remove()
        }
        
        // Update UX Timers
        if (shakeTimer > 0) shakeTimer-- else shakeIntensity = 0f
        if (comboTimer > 0) {
            comboTimer--
            if (comboTimer <= 0) comboCount = 0
        }
        
        if (impactFlashTimer > 0) impactFlashTimer--

        // Dust & Landing
        val isInAir = player.state == com.example.shadowsprint.game.objects.PlayerState.JUMPING || 
                      player.state == com.example.shadowsprint.game.objects.PlayerState.FALLING
        
        if (wasInAir && !isInAir) {
            // Landed!
            spawnParticles(player.x + 40, player.y + 160, Color.LTGRAY, 15)
            haptics.vibrateLight()
        }
        wasInAir = isInAir
        
        // Running dust
        if (player.state == com.example.shadowsprint.game.objects.PlayerState.RUNNING && Random.nextInt(10) == 0) {
            spawnParticles(player.x, player.y + 160, Color.LTGRAY, 1)
        }

        checkCollisions()
        
        distanceTraveled += 0.1f
        score = distanceTraveled.toInt()
    }
    
    private fun spawnEnemies() {
        if (boss != null) return // Don't spawn minions during boss fight
        
        // Check Boss Spawn
        if (score > 50 && boss == null && enemies.isEmpty()) { // Boss at 50 score
             boss = BossEnemy(screenWidth.toFloat(), screenHeight.toFloat())
             floatingTexts.add(FloatingText(screenWidth / 2f, screenHeight / 4f, "BOSS ARRIVED", Color.RED))
             triggerShake(40f, 30)
             haptics.vibrateStrong()
             return
        }
        
        spawnTimer++
        if (spawnTimer > 120) { // Every 2 seconds
            if (Random.nextBoolean()) {
                val groundY = screenHeight - 100f - 120f // approx ground - enemy height
                enemies.add(RunnerEnemy(screenWidth.toFloat(), groundY))
            } else {
                val airY = screenHeight - 400f // Mid air
                enemies.add(FlyingEnemy(screenWidth.toFloat(), airY))
            }
            spawnTimer = 0
        }
    }
    
    private fun spawnItems() {
        if (boss != null) return
        
        // Coins
        if (Random.nextInt(50) == 0) { // 2% chance per frame? Maybe too high. approx 1/sec
             val groundY = screenHeight - 100f - 100f
             // Small pattern of coins or single
             coins.add(com.example.shadowsprint.game.objects.Coin(screenWidth.toFloat(), groundY - Random.nextInt(200)))
        }
        
        // PowerUps
        if (Random.nextInt(500) == 0) { // Rare
            val type = com.example.shadowsprint.game.objects.PowerUpType.values().random()
            val y = screenHeight - 300f 
            powerUps.add(com.example.shadowsprint.game.objects.PowerUp(screenWidth.toFloat(), y, type))
        }
    }
    
    private fun checkCollisions() {
        val playerRect = player.hitbox
        
        // Boss Collisions
        if (boss != null && boss!!.active) {
             val bossRect = boss!!.getRect()
             
             // Projectiles vs Boss
             val pIter = projectiles.iterator()
             while(pIter.hasNext()) {
                 val p = pIter.next()
                  if (Rect.intersects(p.hitbox, bossRect)) {
                      boss!!.onHit()
                      pIter.remove()
                      spawnParticles(p.x, p.y, Color.YELLOW, 5)
                      triggerShake(10f, 5)
                      haptics.vibrateLight()
                      impactFlashTimer = 3
                  }
              }
             
              // Player vs Boss (Body or Sword)
              if (Rect.intersects(playerRect, bossRect)) {
                   if(!player.isInvincible) gameOver()
              }
              
              if (player.isAttacking && Rect.intersects(player.swordHitbox, bossRect)) {
                  boss!!.onHit() // Sword deal 1 damage (same as shuriken but easier to hit)
                  // Let's make sword deal 2 damage if we want it stronger?
                  // for now 1 hit is 1 health dec.
                  spawnParticles(player.x + player.width.toFloat(), player.y + player.height/2f, Color.WHITE, 10)
                  haptics.vibrateMedium()
                  triggerShake(15f, 5)
              }
        }
        
        // Coins
        for (c in coins) {
            if (c.active && Rect.intersects(playerRect, c.getRect())) {
                c.active = false
                comboCount++
                comboTimer = comboMaxTime
                val bonus = comboCount * 5
                score += 10 + bonus
                haptics.vibrateLight()
                spawnParticles(c.x + c.width/2, c.y + c.height/2, Color.YELLOW, 10)
                floatingTexts.add(FloatingText(c.x, c.y, "+${10 + bonus}", Color.YELLOW))
                
                if (comboCount > 5) {
                    floatingTexts.add(FloatingText(player.x, player.y - 100, "COMBO X$comboCount", Color.CYAN))
                }
                // Sound effect here
            }
        }
        
        // PowerUps
        for (p in powerUps) {
             if (p.active && Rect.intersects(playerRect, p.getRect())) {
                 p.active = false
                 player.activatePowerUp(p.type, 600) // 10 seconds @ 60fps
                 haptics.vibrateMedium()
                 floatingTexts.add(FloatingText(p.x, p.y, p.type.name, Color.CYAN))
                 // Show floating text?
             }
        }
        
        // Obstacles (Static)
        for (obstacle in obstacleManager.obstacles) {
            if (Rect.intersects(playerRect, obstacle.getRect())) {
                if(!player.isInvincible) gameOver()
            }
        }
        
        // Enemies
        for (enemy in enemies) {
            if (enemy.active) {
                // 1. Ranged Attack (Shuriken vs Enemy)
                val pIter = projectiles.iterator()
                while(pIter.hasNext()) {
                    val p = pIter.next()
                    if (Rect.intersects(p.hitbox, enemy.getRect())) {
                        enemy.onHit()
                        pIter.remove()
                        score += 50 // Bonus points
                        spawnParticles(enemy.x, enemy.y, Color.RED, 15)
                        haptics.vibrateLight()
                        impactFlashTimer = 3
                    }
                }
                
                // 2. Dash Attack (Player Dashing vs Enemy)
                if (player.state == com.example.shadowsprint.game.objects.PlayerState.DASHING) {
                     if (Rect.intersects(playerRect, enemy.getRect())) {
                         enemy.onHit() // Smash through
                         spawnParticles(enemy.x, enemy.y, Color.RED, 15)
                         triggerShake(15f, 8)
                         haptics.vibrateMedium()
                         impactFlashTimer = 5
                     }
                } 
                // 3. Player Hit (Enemy vs Player Body)
                else if (Rect.intersects(playerRect, enemy.getRect()) && enemy.active) {
                    if(!player.isInvincible) gameOver()
                }
                
                // 4. Sword Attack (Sword Hitbox vs Enemy)
                if (player.isAttacking && Rect.intersects(player.swordHitbox, enemy.getRect())) {
                    enemy.onHit()
                    spawnParticles(enemy.x, enemy.y, Color.RED, 20)
                    score += 100 // Bonus for sword kill
                    haptics.vibrateMedium()
                    impactFlashTimer = 3
                }
            }
        }
    }
    
    private fun gameOver() {
        isGameOver = true
        triggerShake(30f, 20)
        haptics.vibrateStrong()
        if (score > highScore) {
            highScore = score
            prefs.edit().putInt("HIGH_SCORE", highScore).apply()
        }
    }

    fun draw(canvas: Canvas) {
        canvas.save()
        if (shakeTimer > 0) {
            val offsetX = (Random.nextFloat() * 2 - 1) * shakeIntensity
            val offsetY = (Random.nextFloat() * 2 - 1) * shakeIntensity
            canvas.translate(offsetX, offsetY)
        }

        background.draw(canvas)
        obstacleManager.draw(canvas)
        
        for (e in enemies) e.draw(canvas)
        boss?.draw(canvas)
        for (p in projectiles) p.draw(canvas)
        for (part in particles) part.draw(canvas)
        for (ft in floatingTexts) ft.draw(canvas)
        
        for (c in coins) c.draw(canvas)
        for (pu in powerUps) pu.draw(canvas)
        
        player.draw(canvas)
        
        canvas.restore() // Restore after drawing world but before UI (optional choice)
        
        // Impact Flash
        if (impactFlashTimer > 0) {
            val flashPaint = Paint().apply {
                color = Color.WHITE
                alpha = 100
            }
            canvas.drawRect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat(), flashPaint)
        }
        
        canvas.drawText("Score: $score", 50f, 100f, paint)
        
        if (comboCount > 1) {
            paint.color = Color.YELLOW
            paint.textSize = 50f
            canvas.drawText("Combo x$comboCount", 50f, 170f, paint)
            paint.color = Color.WHITE
            paint.textSize = 60f
        }
        
        if (isGameOver) {
            paint.color = Color.RED
            paint.textSize = 100f
            val text = "GAME OVER"
            val width = paint.measureText(text)
            canvas.drawText(text, (screenWidth - width) / 2, screenHeight / 2f, paint)
            paint.color = Color.WHITE // Reset
            paint.textSize = 60f
        }
    }
    
    fun onTouch(action: Int, x: Float, y: Float) {
        if (isGameOver) {
            restart()
        } else {
            player.jump() // Jump on Tap
        }
    }
    
    fun onSwipeDown() {
        if (!isGameOver) player.slide()
    }
    
    fun onSwipeRight() {
        if (!isGameOver) player.dash()
    }
    
    fun onDoubleTap() {
        if (!isGameOver) {
            // Spawn Projectile at player player
            projectiles.add(Projectile(player.x + 50, player.y + 50))
        }
    }
    
    fun onSwipeUp() {
        if (!isGameOver) player.strike() // Strike on Swipe Up
    }
    
    private fun restart() {
        isGameOver = false
        score = 0
        distanceTraveled = 0f
        obstacleManager.obstacles.clear()
        enemies.clear()
        projectiles.clear()
        player.x = 200f
        player.y = (screenHeight - 250).toFloat()
        player.state = com.example.shadowsprint.game.objects.PlayerState.RUNNING
        boss = null
        particles.clear()
        coins.clear()
        powerUps.clear()
        player.x = 200f // Reset player pos completely incase of speed changes
    }
    
    private fun spawnParticles(x: Float, y: Float, color: Int, count: Int) {
        for (i in 0 until count) {
            particles.add(Particle(x, y, color))
        }
    }

    fun triggerShake(intensity: Float, duration: Int) {
        shakeIntensity = intensity
        shakeTimer = duration
    }
}
