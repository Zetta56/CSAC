package com.example.csac.activity


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.csac.R


class MainActivity : AppCompatActivity() {
    var overlayVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        outState.putBoolean("overlayVisible", overlayVisible)
//    }
//
//    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
//        super.onRestoreInstanceState(savedInstanceState)
//        overlayVisible = savedInstanceState.getBoolean("overlayVisible")
//    }
}