package com.example.srtcayhan.hmsaccountkit.ads

import android.os.Bundle
import com.example.srtcayhan.hmsaccountkit.R
import com.huawei.hms.ads.HwAds
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.AudioFocusType
import com.huawei.hms.ads.splash.SplashAdDisplayListener
import com.huawei.hms.ads.splash.SplashView
import com.huawei.hms.ads.splash.SplashView.SplashAdLoadListener

class SplashActivity : Activity() {
    /**
     * Pause flag.
     * On the splash ad screen:
     * Set this parameter to true when exiting the app to ensure that the app home screen is not displayed.
     * Set this parameter to false when returning to the splash ad screen from another screen to ensure that the app home screen can be displayed properly.
     */
    private var hasPaused = false

    // Callback handler used when the ad display timeout message is received.
    private val timeoutHandler = Handler(Handler.Callback {
        if (hasWindowFocus()) {
            jump()
        }
        false
    })
    private var splashView: SplashView? = null
    private val splashAdLoadListener: SplashAdLoadListener = object : SplashAdLoadListener() {
        override fun onAdLoaded() {
            // Call this method when an ad is successfully loaded.
            Log.i(TAG, "SplashAdLoadListener onAdLoaded.")
            Toast.makeText(
                this@SplashActivity,
                getString(R.string.status_load_ad_success),
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun onAdFailedToLoad(errorCode: Int) {
            // Call this method when an ad fails to be loaded.
            Log.i(TAG, "SplashAdLoadListener onAdFailedToLoad, errorCode: $errorCode")
            Toast.makeText(
                this@SplashActivity,
                getString(R.string.status_load_ad_fail) + errorCode,
                Toast.LENGTH_SHORT
            ).show()
            jump()
        }

        override fun onAdDismissed() {
            // Call this method when the ad display is complete.
            Log.i(TAG, "SplashAdLoadListener onAdDismissed.")
            Toast.makeText(
                this@SplashActivity,
                getString(R.string.status_ad_dismissed),
                Toast.LENGTH_SHORT
            ).show()
            jump()
        }
    }
    private val adDisplayListener: SplashAdDisplayListener = object : SplashAdDisplayListener() {
        override fun onAdShowed() {
            // Call this method when an ad is displayed.
            Log.i(TAG, "SplashAdDisplayListener onAdShowed.")
        }

        override fun onAdClick() {
            // Call this method when an ad is clicked.
            Log.i(TAG, "SplashAdDisplayListener onAdClick.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lock screen orientation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        setContentView(R.layout.activity_splash)

        // Initialize the HUAWEI Ads SDK.
        HwAds.init(this)
        loadAd()
    }

    private fun loadAd() {
        Log.i(TAG, "Start to load ad")
        val orientation = screenOrientation
        val adParam = AdParam.Builder().build()
        splashView = findViewById(R.id.splash_ad_view)
        splashView!!.setAdDisplayListener(adDisplayListener)
        val slotId: String
        // Set a default app launch image.
        slotId = if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            splashView!!.setSloganResId(R.drawable.default_slogan)
            getString(R.string.ad_id_splash)
        } else {
            splashView!!.setSloganResId(R.drawable.default_slogan_landscape)
            getString(R.string.ad_id_splash_landscape)
        }
        splashView!!.setLogo(findViewById(R.id.logo_area))

        // Set a logo image.
        splashView!!.setLogoResId(R.mipmap.ic_launcher)
        // Set logo description.
        splashView!!.setMediaNameResId(R.string.app_name)
        // Set the audio focus type for a video splash ad.
        splashView!!.setAudioFocusType(AudioFocusType.NOT_GAIN_AUDIO_FOCUS_WHEN_MUTE)
        splashView!!.load(slotId, orientation, adParam, splashAdLoadListener)
        Log.i(TAG, "End to load ad")

        // Remove the timeout message from the message queue.
        timeoutHandler.removeMessages(MSG_AD_TIMEOUT)
        // Send a delay message to ensure that the app home screen can be displayed when the ad display times out.
        timeoutHandler.sendEmptyMessageDelayed(MSG_AD_TIMEOUT, AD_TIMEOUT.toLong())
    }

    private val screenOrientation: Int
        private get() {
            val config = resources.configuration
            return if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }

    /**
     * Switch from the splash ad screen to the app home screen when the ad display is complete.
     */
    private fun jump() {
        Log.i(TAG, "Jump hasPaused: $hasPaused")
        if (!hasPaused) {
            hasPaused = true
            Log.i(TAG, "Jump into MainActivity")
            startActivity(Intent(this@SplashActivity, AdActivity::class.java))
            val mainHandler = Handler()
            mainHandler.postDelayed({ finish() }, 1000)
        }
    }

    /**
     * Set this parameter to true when exiting the app to ensure that the app home screen is not displayed.
     */
    override fun onStop() {
        Log.i(TAG, "SplashActivity onStop.")
        // Remove the timeout message from the message queue.
        timeoutHandler.removeMessages(MSG_AD_TIMEOUT)
        hasPaused = true
        super.onStop()
    }

    /**
     * Call this method when returning to the splash ad screen from another screen to access the app home screen.
     */
    override fun onRestart() {
        Log.i(TAG, "SplashActivity onRestart.")
        super.onRestart()
        hasPaused = false
        jump()
    }

    override fun onDestroy() {
        Log.i(TAG, "SplashActivity onDestroy.")
        super.onDestroy()
        if (splashView != null) {
            splashView!!.destroyView()
        }
    }

    override fun onPause() {
        Log.i(TAG, "SplashActivity onPause.")
        super.onPause()
        if (splashView != null) {
            splashView!!.pauseView()
        }
    }

    override fun onResume() {
        Log.i(TAG, "SplashActivity onResume.")
        super.onResume()
        if (splashView != null) {
            splashView!!.resumeView()
        }
    }

    companion object {
        private val TAG = SplashActivity::class.java.simpleName

        // Ad display timeout interval, in milliseconds.
        private const val AD_TIMEOUT = 5000

        // Ad display timeout message flag.
        private const val MSG_AD_TIMEOUT = 1001
    }
}