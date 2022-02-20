package com.example.csac.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import com.example.csac.createOverlayLayout
import com.example.csac.models.Clicker
import com.example.csac.toDP
import com.example.csac.toPixels

class ClickerView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private lateinit var clickerMenu: ClickerMenu
    private val borderPaint = Paint()
    private val centerPaint = Paint()

    init {
        borderPaint.color = Color.parseColor("#2256b5")
        borderPaint.strokeWidth = 15f
        borderPaint.style = Paint.Style.STROKE
        centerPaint.color = Color.parseColor("#2256b5")
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        // Measured properties can only be used after onMeasure finishes running
        val centerX = (measuredWidth / 2).toFloat()
        val centerY = (measuredHeight / 2).toFloat()
        canvas?.drawCircle(centerX, centerY, centerX - borderPaint.strokeWidth, borderPaint)
        canvas?.drawCircle(centerX, centerY, 15f, centerPaint)
    }

    fun onDestroy(windowManager: WindowManager) {
        if(this::clickerMenu.isInitialized && clickerMenu.hasWindowToken()) {
            clickerMenu.onDestroy()
        }
        windowManager.removeView(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun addListeners(windowManager: WindowManager, clicker: Clicker, layoutParams: WindowManager.LayoutParams,
                     clickerViews: MutableList<ClickerView>, overlayMenu: View, statusBarHeight: Int) {
        setOnClickListener {
            val drawing = addDrawing(windowManager, layoutParams.x.toFloat(), layoutParams.y.toFloat())
            clickerMenu = ClickerMenu(context, windowManager, clicker, drawing, getCenter(layoutParams), clickerViews, overlayMenu)

            // Hide other views
            clickerViews.forEach { clickerView -> clickerView.visibility = INVISIBLE }
            overlayMenu.visibility = INVISIBLE
        }
        setOnTouchListener(Draggable(windowManager, layoutParams, this, onActionUp={
            val center = getCenter(layoutParams)
            clicker.x = center[0]
            clicker.y = center[1] + statusBarHeight
        }))
    }

    private fun addDrawing(windowManager: WindowManager, clickerX: Float, clickerY: Float): ViewGroup {
        val drawing = FrameLayout(context, null)
        val clickerView = ClickerView(context, null)
        drawing.addView(clickerView)

        // Set clicker view dimensions and position
        clickerView.x = clickerX
        clickerView.y = clickerY
        clickerView.layoutParams.width = toPixels(60)
        clickerView.layoutParams.height = toPixels(60)

        // Make drawing window
        val displayMetrics = context.resources.displayMetrics
        val drawingLayout = createOverlayLayout(toDP(displayMetrics.widthPixels), toDP(displayMetrics.heightPixels), touchable=false)
        windowManager.addView(drawing, drawingLayout)

        return drawing
    }

    private fun getCenter(layoutParams: WindowManager.LayoutParams): Array<Float> {
        return arrayOf(
            (layoutParams.x + layoutParams.width / 2).toFloat(),
            (layoutParams.y + layoutParams.height / 2).toFloat()
        )
    }
}