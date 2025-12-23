package com.example.shadowsprint.game

import android.content.Context
import android.graphics.Canvas
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameSurfaceView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    private var thread: GameThread? = null
    private var gameManager: GameManager? = null

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        gameManager = GameManager(context, width, height)
        thread = GameThread(getHolder(), this)
        thread?.running = true
        thread?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Handle screen resizing if necessary
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        var retry = true
        thread?.running = false
        while (retry) {
            try {
                thread?.join()
                retry = false
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    fun update() {
        gameManager?.update()
    }


    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        gameManager?.draw(canvas)
    }

    private var startY = 0f
    private var startX = 0f
    private var lastTapTime = 0L

    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        when (event.action) {
            android.view.MotionEvent.ACTION_DOWN -> {
                startY = event.y
                startX = event.x
                return true
            }
            android.view.MotionEvent.ACTION_UP -> {
                val endY = event.y
                val endX = event.x
                val deltaY = endY - startY
                val deltaX = endX - startX
                
                if (Math.abs(deltaY) > 100) {
                     if (deltaY > 0) {
                         // Swipe Down
                         gameManager?.onSwipeDown()
                     } else {
                         // Swipe Up
                         gameManager?.onSwipeUp()
                     }
                } else if (Math.abs(deltaX) > 100) {
                    if (deltaX > 0) {
                        // Swipe Right -> DASH
                        gameManager?.onSwipeRight()
                    }
                } else {
                    // Tap or Double Tap
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastTapTime < 300) {
                        // Double Tap -> Shoot
                        gameManager?.onDoubleTap()
                        lastTapTime = 0 // Reset
                    } else {
                        // Single Tap -> Jump
                        gameManager?.onTouch(event.action, event.x, event.y)
                        lastTapTime = currentTime
                    }
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
