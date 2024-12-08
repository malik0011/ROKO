package com.example.roko

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.view.WindowCompat

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_splash)

        // Make the activity full screen
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Hide system bars
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        val logoBackground = findViewById<ImageView>(R.id.ivLogoBackground)
        val logo = findViewById<ImageView>(R.id.ivLogo)
        val appName = findViewById<TextView>(R.id.tvAppName)

        // Scale animation for the logo background
        val scaleX = ObjectAnimator.ofFloat(logoBackground, View.SCALE_X, 0f, 1f)
        val scaleY = ObjectAnimator.ofFloat(logoBackground, View.SCALE_Y, 0f, 1f)
        
        // Rotation animation for the logo
        val rotation = ObjectAnimator.ofFloat(logo, View.ROTATION, 0f, 360f)
        
        // Fade in animation for the app name
        val fadeIn = ObjectAnimator.ofFloat(appName, View.ALPHA, 0f, 1f)

        // Combine all animations
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
            doOnEnd {
                // Start rotation after scale
                rotation.apply {
                    duration = 1000
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
                // Start fade in with rotation
                fadeIn.apply {
                    duration = 500
                    startDelay = 500
                    start()
                }
            }
            // Start the next activity after animations
            fadeIn.doOnEnd {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }
            start()
        }
    }
} 