package com.example.shadowsprint.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.example.shadowsprint.game.GlobalEnemyMind
import kotlin.math.abs

enum class PlayerState {
    IDLE, RUNNING, JUMPING, FALLING, WALL_SLIDING, DASHING, ATTACKING, DEAD
}

class Player(private val screenWidth: Int, private val screenHeight: Int) {

    // Position & Physics
    var x = 100f
    var y = screenHeight - 300f
    var width = 60f
    var height = 100f
    
    var velX = 0f
    var velY = 0f
    private val speed = 15f
    private val gravity = 1.2f
    private val jumpForce = -25f
    private val dashSpeed = 40f
    
    // State
    var state = PlayerState.IDLE
    var facingRight = true
    var inputX = 0f // -1.0 to 1.0 from Joystick
    var inputY = 0f
    
    // Combat / Gameplay
    var maxHealth = 100
    var health = 100
    var isInvincible = false
    private var damageTimer = 0
    
    // Combo System
    var comboCount = 0
    var comboWindow = 0
    
    private var dashTimer = 0
    private var attackTimer = 0
    private var wallJumpTimer = 0
    
    // Cooldowns
    var phaseCooldown = 0
    var dragonCooldown = 0
    var cloneCooldown = 0
    var chronosCooldown = 0
    var momentum = 1.0f

    // Visuals
    private val paint = Paint().apply { isAntiAlias = true }
    
    // Hitboxes
    val hitbox: RectF
        get() = RectF(x, y, x + width, y + height)
        
    val swordHitbox: RectF
        get() {
            // Combo 3 is bigger
            val range = if(comboCount >= 3) 200f else 120f 
            return if (facingRight) {
                RectF(x + width, y, x + width + range, y + height + 20)
            } else {
                RectF(x - range, y, x, y + height + 20)
            }
        }
        
    var isAttacking: Boolean
        get() = state == PlayerState.ATTACKING
        set(value) { if(value) attack() }

    fun update(platforms: List<RectF>) {
        if (state == PlayerState.DEAD) return

        // --- PHYSICS ---
        
        // Horizontal Movement
        if (state != PlayerState.DASHING && wallJumpTimer <= 0 && damageTimer <= 0) {
            velX = inputX * speed
            if (inputX > 0) facingRight = true
            if (inputX < 0) facingRight = false
        }
        
        // Damage Knockback
        if (damageTimer > 0) {
            damageTimer--
            if (damageTimer <= 0) isInvincible = false
        }
        
        // Dash Physics
        if (state == PlayerState.DASHING) {
            velX = if (facingRight) dashSpeed else -dashSpeed
            velY = 0f
            dashTimer--
            if (dashTimer <= 0) {
                state = PlayerState.FALLING
                velX = 0f
            }
        } else {
            // Gravity
            velY += gravity
        }
        
        // Wall Jump Lockout
        if (wallJumpTimer > 0) wallJumpTimer--

        // Apply Velocity
        x += velX
        y += velY
        
        // --- COLLISIONS ---
        checkCollisions(platforms)
        
        // --- STATE MANAGEMENT ---
        updateState()
        
        // Attack Timer
        if (attackTimer > 0) {
            attackTimer--
            if (attackTimer <= 0) {
                state = PlayerState.IDLE
                // Combo window opens
                comboWindow = 30 
            }
        }
        
        if (comboWindow > 0) {
            comboWindow--
            if (comboWindow <= 0) comboCount = 0 // Reset
        }
    }
    
    private fun checkCollisions(platforms: List<RectF>) {
        var onGround = false
        
        // Floor / Ceiling
        val pRect = hitbox
        for (plat in platforms) {
            if (RectF.intersects(pRect, plat)) {
                // Determine side of collision
                val overlapBottom = (pRect.bottom - plat.top)
                val overlapTop = (plat.bottom - pRect.top)
                val overlapLeft = (pRect.right - plat.left)
                val overlapRight = (plat.right - pRect.left)
                
                // Smallest overlap determines correction
                val minOverlap = listOf(overlapBottom, overlapTop, overlapLeft, overlapRight).minOrNull() ?: 0f
                
                if (minOverlap == overlapBottom && velY > 0) {
                    // Landed on top
                    y = plat.top - height
                    velY = 0f
                    onGround = true
                } else if (minOverlap == overlapTop && velY < 0) {
                    // Hit head
                    y = plat.bottom
                    velY = 0f
                } else if (minOverlap == overlapLeft && velX > 0) {
                    // Hit right wall
                    x = plat.left - width
                    velX = 0f
                    if (!onGround && velY > 0) state = PlayerState.WALL_SLIDING
                } else if (minOverlap == overlapRight && velX < 0) {
                    // Hit left wall
                    x = plat.right
                    velX = 0f
                    if (!onGround && velY > 0) state = PlayerState.WALL_SLIDING
                }
            }
        }
        
        // Screen Bounds (Temp)
        if (y > screenHeight) {
            y = 0f // Respawn loop for testing
            velY = 0f
        }
    }
    
    private fun updateState() {
        if (state == PlayerState.DASHING || state == PlayerState.ATTACKING) return
        
        // If colliding with wall logic didn't set sliding...
        if (state != PlayerState.WALL_SLIDING) {
             if (abs(velY) > 1f) {
                 state = if (velY < 0) PlayerState.JUMPING else PlayerState.FALLING
             } else if (abs(velX) > 0.5f) {
                 state = PlayerState.RUNNING
             } else {
                 state = PlayerState.IDLE
             }
        } else {
            // Wall Slide Friction
            if (velY > 5f) velY = 5f
            
            // Wall Jump Input check would happen in jumps
            // If input is away from wall, detach?
        }
    }

    // --- ACTIONS ---
    
    fun setJoystickInput(x: Float, y: Float) {
        inputX = x
        inputY = y
    }
    
    fun jump() {
        if (state == PlayerState.DEAD) return
        
        // Ground Jump
        if (state == PlayerState.IDLE || state == PlayerState.RUNNING) {
            velY = jumpForce
            state = PlayerState.JUMPING
        } 
        // Wall Jump
        else if (state == PlayerState.WALL_SLIDING) {
            velY = jumpForce
            velX = if (facingRight) -speed * 1.5f else speed * 1.5f // Jump away
            facingRight = !facingRight
            wallJumpTimer = 15 // Lock control briefly
            state = PlayerState.JUMPING
        }
    }
    
    fun dash() {
        if (state != PlayerState.DASHING && state != PlayerState.DEAD) {
            state = PlayerState.DASHING
            dashTimer = 10 // Frames
        }
    }
    
    fun attack() {
        if (state == PlayerState.DEAD) return
        
        // If already attacking, check for next combo input
        if (state == PlayerState.ATTACKING) {
            if (attackTimer < 10 && comboCount < 3) {
                 // Buffer next attack? Or just reset timer for next stage?
                 // Simple: Next click extends combo if within window?
                 // For now, let's keep it simple: Attacking locks you.
                 // We rely on "comboWindow" which is set AFTER attack finishes in update().
                 return 
            }
        }
        
        // Start Attack or Continue Combo
        if (state != PlayerState.ATTACKING) {
             if (comboWindow > 0 && comboCount < 3) {
                 comboCount++
             } else {
                 comboCount = 1
             }
             state = PlayerState.ATTACKING
             attackTimer = 20 // Duration
             velX = 0f // Stop moving for a split second?
        }
    }
    
    fun onHit(damage: Int) {
        if (isInvincible || state == PlayerState.DASHING || state == PlayerState.DEAD) return
        
        health -= damage
        if (health <= 0) {
            health = 0
            state = PlayerState.DEAD
        } else {
            isInvincible = true
            damageTimer = 30 // 0.5s invincibility
            // Knockback
            velY = -15f
            velX = if (facingRight) -20f else 20f
            state = PlayerState.FALLING
        }
    }

    fun onKill() {
        // momentum logic
        if (health < maxHealth) health += 5
        
        // Report to AI
        val killType = if (state == PlayerState.JUMPING) "JUMP_ATTACK" else "SLASH"
        GlobalEnemyMind.recordDeath(killType)
    }
    
    // Stub methods for legacy compatibility if needed
    fun usePhaseDash() { dash() }
    fun useDragonBreath(): Boolean = false
    fun useStaticClone() {}
    fun useChronosEdge() {}
    fun strike() { attack() } // Alias
    fun slide() { } // Handled by joystick down eventually

    // --- DRAWING ---

    fun draw(canvas: Canvas) {
        // Kuro Stylization: Black Silhouette, glowing eyes, scarf
        
        // 1. Scarf (Trail)
        drawScarf(canvas)
        
        // 2. Body (Ninja Shape)
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
        
        // Simple shape to represent crouch/jump/run
        val drawHeight = if (state == PlayerState.DASHING) height * 0.6f else height
        val drawY = y + (height - drawHeight)
        
        canvas.drawRect(x, drawY, x + width, y + height, paint)
        
        // 3. Eyes (Glowing)
        paint.color = Color.RED
        paint.setShadowLayer(10f, 0f, 0f, Color.RED)
        val eyeX = if (facingRight) x + width - 15f else x + 5f
        val eyeY = drawY + 25f
        canvas.drawRect(eyeX, eyeY, eyeX + 10f, eyeY + 5f, paint)
        paint.setShadowLayer(0f, 0f, 0f, 0) // Reset
        
        // 4. Katana Visuals
        if (attackTimer > 0) {
            drawKatana(canvas)
        }
    }
    
    private fun drawKatana(canvas: Canvas) {
        val p = android.graphics.Path()
        val cx = x + width / 2
        val cy = y + height / 2
        
        val progress = 1f - (attackTimer / 20f) // 0 to 1
        
        // Swipe properties based on combo
        val swipeRange = if(comboCount >= 3) 180f else 120f
        val startAngle = if (comboCount == 2) 45f else -135f // Up vs Down
        val direction = if (comboCount == 2) -1 else 1
        
        // Dynamic Swipe
        val currentAngle = startAngle + (swipeRange * progress * direction)
        val rad = Math.toRadians(currentAngle.toDouble())
        val bladeLen = 140f
        
        val tipX = cx + (Math.cos(rad) * bladeLen).toFloat() * (if(facingRight) 1 else -1)
        val tipY = cy + (Math.sin(rad) * bladeLen).toFloat()
        
        // Draw Blade
        paint.color = Color.WHITE
        paint.strokeWidth = 8f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(cx, cy, tipX, tipY, paint)
        
        // Draw Swipe Trail (Blur)
        if (attackTimer > 5) {
            val trailPaint = Paint().apply {
                color = Color.CYAN
                style = Paint.Style.STROKE
                strokeWidth = 20f
                alpha = 100
                strokeCap = Paint.Cap.ROUND
            }
            // Draw a curve representing the path
            val trailPath = android.graphics.Path()
            // Approximate arc.. simplified
            trailPath.moveTo(cx + (if(facingRight) 50 else -50), cy - 50)
            trailPath.quadTo(cx + (if(facingRight) 150 else -150), cy, cx + (if(facingRight) 50 else -50), cy + 100)
            canvas.drawPath(trailPath, trailPaint)
        }
        
        paint.style = Paint.Style.FILL // Reset
    }
    
    private fun drawScarf(canvas: Canvas) {
        // Simple physics implementation for now
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 8f
        
        val startX = if (facingRight) x else x + width
        val startY = y + 25f
        
        // "Flowing" behind
        val endX = startX - (if(facingRight) 60f else -60f)
        val endY = startY - (velY * 2f) - 10f
        
        canvas.drawLine(startX, startY, endX, endY, paint)
    }
}
