package com.example.csac.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.*
import com.example.csac.models.CircleParcel
import com.example.csac.R
import com.example.csac.databinding.CircleMenuBinding
import com.example.csac.databinding.OverlayMenuBinding

// To add another view, just add it with a new layoutParams and call windowManager.addView()
class OverlayMenu(
    private val context: Context,
    private val windowManager: WindowManager,
    private val circles: MutableList<CircleView>,
    private val autoClickIntent: Intent
) {

    private val binding = OverlayMenuBinding.inflate(LayoutInflater.from(context))
    private val layoutParams = OverlayService.createOverlayLayout(55, 165, Gravity.START)
    private var playing = false

    init {
        windowManager.addView(binding.root, layoutParams)
        binding.playButton.setOnClickListener { toggleClicker() }
        binding.plusButton.setOnClickListener { addCircle() }
        binding.minusButton.setOnClickListener { removeCircle() }
        addTouchListeners()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addTouchListeners() {
        val draggable = Draggable(windowManager, layoutParams, binding.root)
        binding.root.setOnTouchListener(draggable)
        binding.playButton.setOnTouchListener(draggable)
        binding.plusButton.setOnTouchListener(draggable)
        binding.minusButton.setOnTouchListener(draggable)
    }

    fun onDestroy() {
        windowManager.removeView(binding.root)
    }

    private fun toggleClicker() {
        playing = !playing
        if(playing) {
            circles.forEach { circle -> circle.visibility = View.INVISIBLE }
            binding.playButton.setImageResource(R.drawable.pause)

            val circleParcels = ArrayList(circles.map { circle -> CircleParcel(circle) })
            autoClickIntent.putParcelableArrayListExtra("circles", circleParcels)
            autoClickIntent.putExtra("enabled", true)
            context.startService(autoClickIntent)
        } else {
            circles.forEach { circle -> circle.visibility = View.VISIBLE }
            binding.playButton.setImageResource(R.drawable.play)
            autoClickIntent.putExtra("enabled", false)
            context.startService(autoClickIntent)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addCircle() {
        val circle = CircleView(context)
        val circleLayout = OverlayService.createOverlayLayout(60, 60, Gravity.NO_GRAVITY)
        circle.setOnTouchListener(Draggable(windowManager, circleLayout, circle))
        circle.setOnClickListener {
            binding.root.visibility = View.INVISIBLE
            val menuLayout = OverlayService.createOverlayLayout(55, 165, Gravity.START)
            val menuBinding = CircleMenuBinding.inflate(LayoutInflater.from(context))
            windowManager.addView(menuBinding.root, menuLayout)
        }
        windowManager.addView(circle, circleLayout)
        circles += circle
    }

    private fun removeCircle() {
        if(circles.size > 0) {
            windowManager.removeView(circles[circles.lastIndex])
            circles.removeAt(circles.lastIndex)
        }
    }
}