package com.example.roko

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val logoBackground = view.findViewById<ImageView>(R.id.ivLogoBackground)
        val logo = view.findViewById<ImageView>(R.id.ivLogo)
        val appName = view.findViewById<TextView>(R.id.tvAppName)

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
            // Navigate to main screen after animations
            fadeIn.doOnEnd {
                // Setup main content and remove splash fragment
                (activity as? MainActivity)?.apply {
                    setupMainContent()
                    supportFragmentManager.beginTransaction()
                        .remove(this@SplashFragment)
                        .commit()
                }
            }
            start()
        }
    }
} 