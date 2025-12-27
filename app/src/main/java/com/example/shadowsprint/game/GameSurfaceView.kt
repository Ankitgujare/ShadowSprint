package com.example.shadowsprint.game

import android.content.Context
import android.graphics.Canvas
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameSurfaceView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    private var thread: GameThread? = null
    var gameManager: GameManager? = null

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

    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        // Legacy touch handling removed. Using JoyStick.
        return true 
    }
}
