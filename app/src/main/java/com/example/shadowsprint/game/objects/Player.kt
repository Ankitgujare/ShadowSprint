package com.example.shadowsprint.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect

enum class PlayerState {
    RUNNING, JUMPING, FALLING, SLIDING, DASHING
}

class Player(private val screenWidth: Int, private val screenHeight: Int) {

    private val paint = Paint().apply {
        color = Color.CYAN
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // Ghosting Effect
    private data class ShadowFrame(val x: Float, val y: Float, val state: PlayerState, val timestamp: Long)
    private val shadows = mutableListOf<ShadowFrame>()
    private var shadowTimer = 0
    private var slashTimer = 0
    
    // Sword Combat
    var isAttacking = false
    private var attackTimer = 0
    private val attackDuration = 10 // Frames
    
    // Scarf Physics
    private val scarfPoints = mutableListOf<Pair<Float, Float>>()
    private val scarfLength = 8
    
    // Dimensions
    val width = 80
    val height = 160
    private val slideHeight = 80

    // Position
    var x = 200f
    var y = screenHeight - height - 100f // Ground level
    private val groundY = screenHeight - height - 100f
    private val baseGroundY = screenHeight - 100f

    // Physics
    private var velocityY = 0f
    private val gravity = 2f
    private val jumpStrength = -40f
    
    // Combat / abilities
    var isInvincible = false
    private var dashTimer = 0
    private val dashDuration = 20 // Frames

    var state = PlayerState.RUNNING

    // Power-ups
    private val activePowerUps = mutableMapOf<PowerUpType, Int>() // Type -> frames remaining
    
    val isShieldActive: Boolean
        get() = activePowerUps.containsKey(PowerUpType.SHIELD)
    
    val isMagnetActive: Boolean
        get() = activePowerUps.containsKey(PowerUpType.MAGNET)
        
    val isSpeedActive: Boolean
        get() = activePowerUps.containsKey(PowerUpType.SPEED)


    // Hitbox
    val hitbox: Rect
        get() {
            val h = if (state == PlayerState.SLIDING) slideHeight else height
            val top = if (state == PlayerState.SLIDING) (baseGroundY - slideHeight) else y
            return Rect(x.toInt(), top.toInt(), (x + width).toInt(), (y + h).toInt())
        }
        
    val swordHitbox: Rect
        get() {
            return Rect((x + width).toInt(), y.toInt(), (x + width + 120).toInt(), (y + height).toInt())
        }

    fun update() {
        when (state) {
            PlayerState.JUMPING, PlayerState.FALLING -> {
                velocityY += gravity
                y += velocityY

                if (y >= groundY) {
                    y = groundY
                    velocityY = 0f
                    state = PlayerState.RUNNING
                }
            }
            PlayerState.SLIDING -> {
                // Logic handled externally or by timer? Keep simple for now
            }
            PlayerState.DASHING -> {
                dashTimer--
                if (dashTimer <= 0) {
                    state = PlayerState.RUNNING
                    isInvincible = false
                    paint.color = Color.CYAN
                }
            }
            else -> {}
        }
        
        if (isAttacking) {
            attackTimer--
            if (attackTimer <= 0) isAttacking = false
        }
        
        // Update Scarf Trail
        val neckX = x + width / 2 + (if (state == PlayerState.RUNNING) 40f else 10f)
        val neckY = y + 50f
        scarfPoints.add(0, Pair(neckX, neckY))
        if (scarfPoints.size > scarfLength) scarfPoints.removeAt(scarfPoints.size - 1)
        
        // Update Ghosting
        shadowTimer++
        if (shadowTimer >= 3) { // Store pose every 3 frames
            shadows.add(ShadowFrame(x, y, state, System.currentTimeMillis()))
            if (shadows.size > 5) shadows.removeAt(0)
            shadowTimer = 0
        }
        
        if (slashTimer > 0) slashTimer--
        
        // Update PowerUps
        val iterator = activePowerUps.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            entry.setValue(entry.value - 1)
            if (entry.value <= 0) {
                iterator.remove()
            }
        }

    }

    fun draw(canvas: Canvas) {
        drawShadows(canvas)
        // Procedural Animation Variables
        val time = System.currentTimeMillis()
        val runCycle = (time / 80.0) 
        
        // --- PAINT SETUP ---
        // 1. Black Stroke (Body/Limbs)
        val blackStroke = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 14f
            strokeCap = Paint.Cap.ROUND // Rounds off ends like a marker
            isAntiAlias = true
        }
        
        // 2. Head (Red Hood/Cowl)
        val redFill = Paint().apply {
            color = Color.rgb(180, 0, 0)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        
        // 3. Face (White)
        val whiteFill = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        
        // 4. Eyes/Mask Detail (Black)
        val blackFill = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // --- MATH & POSITIONS ---
        val headRadius = 28f
        
        // Pivot Points
        val centerX = x + width / 2
        val bounce = if (state == PlayerState.RUNNING) (Math.sin(runCycle * 2) * 5f).toFloat() else 0f
        val currentTop = (if (state == PlayerState.SLIDING) (baseGroundY - slideHeight) + headRadius else y + headRadius * 2) + bounce
        
        // Head Position (Leaning forward)
        val neckLean = if (state == PlayerState.RUNNING) 50f else 20f
        val headX = centerX + neckLean
        val headY = currentTop - headRadius
        
        // --- DRAWING ---

        // 1. HEAD (Red Circle Outer, White Circle Inner)
        canvas.drawCircle(headX, headY, headRadius, redFill) // Red "Helmet"
        canvas.drawCircle(headX, headY, headRadius - 8f, whiteFill) // White Face
        
        // Angry Eyes (Angled Lines/Ovals)
        // Left Eye
        canvas.save()
        canvas.rotate(25f, headX - 10, headY - 5)
        canvas.drawOval(headX - 18, headY - 12, headX - 6, headY - 2, blackFill)
        canvas.restore()
        
        // Right Eye
        canvas.save()
        canvas.rotate(-25f, headX + 10, headY - 5)
        canvas.drawOval(headX + 6, headY - 12, headX + 18, headY - 2, blackFill)
        canvas.restore()
        
        // Ninja Mask (Covers lower face)
        val maskPath = android.graphics.Path()
        maskPath.moveTo(headX - headRadius + 5, headY + 5)
        maskPath.quadTo(headX, headY + headRadius + 2, headX + headRadius - 5, headY + 5)
        maskPath.close()
        canvas.drawPath(maskPath, redFill)
        
        // Draw Scarf
        if (scarfPoints.size > 1) {
            val scarfPaint = Paint().apply {
                color = Color.rgb(200, 0, 0)
                style = Paint.Style.STROKE
                strokeWidth = 22f
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
                isAntiAlias = true
            }
            val scarfPath = android.graphics.Path()
            scarfPath.moveTo(scarfPoints[0].first, scarfPoints[0].second)
            for (i in 1 until scarfPoints.size) {
                val wave = (Math.sin(time / 150.0 + i) * 15.0).toFloat()
                scarfPath.lineTo(scarfPoints[i].first - (i * 25), scarfPoints[i].second + wave)
            }
            canvas.drawPath(scarfPath, scarfPaint)
            // Add a sketchy "wind" line on top
            scarfPaint.color = Color.BLACK
            scarfPaint.strokeWidth = 3f
            canvas.drawPath(scarfPath, scarfPaint)
        }

        // 2. BODY & LIMBS
        val shoulderY = headY + 25f
        val hipY = shoulderY + 45f
        val hipX = centerX
        
        // Torso (Curved Sketchy Line)
        val spinePath = android.graphics.Path()
        spinePath.moveTo(headX, headY + 25f)
        spinePath.quadTo(centerX + neckLean / 2 + 10, (shoulderY + hipY) / 2, hipX, hipY)
        canvas.drawPath(spinePath, blackStroke)

        // Limbs Animation Logic (Keeping the wider sprint logic)
        var lLegAngle = 0.0; var rLegAngle = 0.0
        var lKneeBend = 0.1; var rKneeBend = 0.1
        var lArmAngle = 0.0; var rArmAngle = 0.0
        var lElbowBend = 1.0; var rElbowBend = 1.0

        when (state) {
            PlayerState.RUNNING -> {
                lLegAngle = Math.sin(runCycle) * 1.4
                rLegAngle = Math.sin(runCycle + Math.PI) * 1.4
                lKneeBend = 0.1 
                rKneeBend = 0.1
                // Ninja Arms: Pinned back
                lArmAngle = 2.4 + Math.sin(runCycle) * 0.2
                rArmAngle = 2.4 - Math.sin(runCycle) * 0.2
            }
            PlayerState.JUMPING, PlayerState.FALLING -> {
                lLegAngle = -0.6; rLegAngle = 0.8
                lArmAngle = 2.0; rArmAngle = 2.0 // Arms back during jump too
            }
            PlayerState.SLIDING -> {
                lLegAngle = -1.5; rLegAngle = -1.3
                lArmAngle = 1.0; rArmAngle = 1.0
            }
            PlayerState.DASHING -> {
                lLegAngle = 0.2; rLegAngle = 0.2
                lArmAngle = 2.8; rArmAngle = 2.8 // Deep ninja tuck
            }
        }

        val armLen = 35f
        val legLen = 45f

        // Helper for Sketchy Limbs
        fun drawSketchyLimb(startX: Float, startY: Float, angle: Double, length: Float, isLeg: Boolean) {
            val endX = startX + (Math.sin(angle) * length).toFloat()
            val endY = startY + (Math.cos(angle) * length).toFloat()
            
            // Upper Limb
            canvas.drawLine(startX, startY, endX, endY, blackStroke)
            
            // Lower Limb
            val bend = if(isLeg) if(angle > 0) -0.2 else 0.2 else if (angle > 0) -1.2 else 1.2 // Auto bend logic simplifed
            val footX = endX + (Math.sin(angle + bend) * length).toFloat()
            val footY = endY + (Math.cos(angle + bend) * length).toFloat()
            
            canvas.drawLine(endX, endY, footX, footY, blackStroke)
            
            // Ninja Wraps (Sketchy white lines on limbs)
            val wrapPaint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = 4f
                alpha = 180
            }
            if (isLeg) {
                // Leg wraps
                canvas.drawLine(endX - 5, endY + 10, endX + 5, endY + 15, wrapPaint)
                canvas.drawLine(endX - 5, endY + 20, endX + 5, endY + 25, wrapPaint)
            } else {
                // Arm wraps
                canvas.drawLine(endX - 5, endY + 5, endX + 5, endY + 10, wrapPaint)
            }

            // Red Shoes/Hands
            if (isLeg) {
                canvas.drawOval(footX - 10, footY - 4, footX + 14, footY + 4, redFill)
            }
        }

        // Draw Right Side (Behind)
        blackStroke.color = Color.DKGRAY // Depth
        drawSketchyLimb(hipX, hipY, rLegAngle, legLen, true)
        drawSketchyLimb(headX, shoulderY, rArmAngle, armLen, false)
        
        // Draw Left Side (Front)
        blackStroke.color = Color.BLACK
        drawSketchyLimb(hipX, hipY, lLegAngle, legLen, true)
        drawSketchyLimb(headX, shoulderY, lArmAngle, armLen, false)
        
        drawSword(canvas, headX, neckLean, shoulderY)
        drawEffects(canvas)
    }

    private fun drawSword(canvas: Canvas, headX: Float, neckLean: Float, shoulderY: Float) {
        val swordPaint = Paint().apply {
            color = Color.rgb(200, 200, 200) // Silver/Steel
            style = Paint.Style.STROKE
            strokeWidth = 6f
            strokeCap = Paint.Cap.BUTT
            isAntiAlias = true
        }
        val handlePaint = Paint().apply {
            color = Color.rgb(180, 0, 0) // Ninja Red
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        if (isAttacking) {
            // SWINGING ANIMATION
            val swingProgress = 1f - (attackTimer.toFloat() / attackDuration.toFloat())
            val swingAngle = -20f + (swingProgress * 160f)
            
            canvas.save()
            canvas.rotate(swingAngle, x + width, y + height / 2)
            
            // Blade
            canvas.drawLine(x + width, y + height / 2, x + width + 100, y + height / 2, swordPaint)
            // Handle
            canvas.drawRect(x + width - 20, y + height / 2 - 5, x + width, y + height / 2 + 5, handlePaint)
            // Sketchy edge
            swordPaint.color = Color.BLACK
            swordPaint.strokeWidth = 2f
            canvas.drawLine(x + width, y + height / 2 + 3, x + width + 100, y + height / 2 + 3, swordPaint)
            
            canvas.restore()
        } else {
            // SHEATHED ON BACK
            canvas.save()
            canvas.rotate(-45f, headX - 20, shoulderY + 20)
            
            // Sheath
            swordPaint.color = Color.BLACK
            swordPaint.strokeWidth = 10f
            canvas.drawLine(headX - 60, shoulderY + 20, headX + 10, shoulderY + 20, swordPaint)
            
            // Handle sticking out
            canvas.drawRect(headX + 10, shoulderY + 12, headX + 35, shoulderY + 28, handlePaint)
            
            canvas.restore()
        }
    }

    private fun drawShadows(canvas: Canvas) {
        if (state != PlayerState.DASHING && shadows.isEmpty()) return
        
        val shadowPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 10f
            isAntiAlias = true
        }
        
        for (i in shadows.indices) {
            val s = shadows[i]
            val alpha = (i + 1) * 30 // Faded
            shadowPaint.alpha = alpha
            
            // Draw a simplified silhouette at shadow position
            canvas.save()
            canvas.translate(s.x - x, s.y - y)
            // Draw head
            canvas.drawCircle(s.x + width/2 + (if(s.state == PlayerState.RUNNING) 50f else 20f), s.y + 28, 28f, shadowPaint)
            // Draw torso simplified
            canvas.drawLine(s.x + width/2 + 30f, s.y + 50f, s.x + width/2, s.y + 100f, shadowPaint)
            canvas.restore()
        }
    }

    fun jump() {
        if (state == PlayerState.RUNNING || state == PlayerState.SLIDING || state == PlayerState.DASHING) {
            state = PlayerState.JUMPING
            velocityY = jumpStrength
            isInvincible = false // Cancel dash invincibility if jumping out?
        }
    }

    fun slide() {
        if (state == PlayerState.RUNNING) {
            state = PlayerState.SLIDING
        }
    }

    fun stopSlide() {
        if (state == PlayerState.SLIDING) {
            state = PlayerState.RUNNING
            y = groundY // fix pos
        }
    }
    
    fun dash() {
        if (state != PlayerState.DASHING) {
            state = PlayerState.DASHING
            isInvincible = true
            dashTimer = dashDuration
            slashTimer = 15 // Trigger dash-specific slash visual
            paint.color = Color.YELLOW // Visual cue
            strike() // Also strike when dashing!
        }
    }
    
    fun strike() {
        if (!isAttacking) {
            isAttacking = true
            attackTimer = attackDuration
        }
    }

    fun activatePowerUp(type: PowerUpType, durationFrames: Int) {
        activePowerUps[type] = durationFrames
    }

    // Helper to draw visuals (call this from draw)
    fun drawEffects(canvas: Canvas) {
        val time = System.currentTimeMillis()
        
        // 1. Shield Effect
        if (isShieldActive) {
            val shieldPaint = Paint().apply {
                color = Color.CYAN
                style = Paint.Style.STROKE
                strokeWidth = 8f
                alpha = (100 + Math.sin(time / 200.0) * 50).toInt() // Pulsing alpha
                isAntiAlias = true
            }
            canvas.drawCircle(x + width / 2, y + height / 2, width.toFloat() * 1.1f, shieldPaint)
            
            // Inner glow
            shieldPaint.style = Paint.Style.FILL
            shieldPaint.alpha = 30
            canvas.drawCircle(x + width / 2, y + height / 2, width.toFloat() * 1.1f, shieldPaint)
        }

        // 2. Magnet Effect
        if (isMagnetActive) {
            val magnetPaint = Paint().apply {
                color = Color.YELLOW
                style = Paint.Style.STROKE
                strokeWidth = 4f
                alpha = (150 + Math.sin(time / 100.0) * 100).toInt() // Fast pulsing
                isAntiAlias = true
            }
            // Draw expanding rings
            val radius = width.toFloat() * (1.2f + (time % 1000 / 1000f))
            canvas.drawCircle(x + width / 2, y + height / 2, radius, magnetPaint)
        }

        // 3. Speed Effect (Trailing/Wind lines)
        if (isSpeedActive) {
            val speedPaint = Paint().apply {
                color = Color.WHITE
                strokeWidth = 4f
                alpha = 180
                isAntiAlias = true
            }
            // Draw "wind" lines behind player
            for (i in 0..2) {
                val lineX = x - 20 - (time % 500 / 500f) * 100 - (i * 40)
                val lineY = y + 40 + (i * 40)
                canvas.drawLine(lineX, lineY, lineX + 60, lineY, speedPaint)
            }
        }
        
        // 4. Slash Effect
        if (slashTimer > 0) {
            val slashPaint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = 15f
                strokeCap = Paint.Cap.ROUND
                alpha = (slashTimer * 17)
                isAntiAlias = true
            }
            val arcPath = android.graphics.Path()
            arcPath.moveTo(x + width + 20, y - 20)
            arcPath.quadTo(x + width + 100, y + height/2, x + width + 20, y + height + 20)
            canvas.drawPath(arcPath, slashPaint)
        }
    }
}
