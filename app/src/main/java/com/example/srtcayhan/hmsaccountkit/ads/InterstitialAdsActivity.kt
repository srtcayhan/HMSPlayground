package com.example.srtcayhan.hmsaccountkit.ads

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import com.example.srtcayhan.hmsaccountkit.R
import com.huawei.hms.ads.AdListener
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.InterstitialAd

class InterstitialAdsActivity : AppCompatActivity() {

    private var interstitialAd: InterstitialAd? = null
    private var displayRadioGroup: RadioGroup? = null
    private var loadAdButton: Button? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interstitial_ads)
        interstitialAd = InterstitialAd(this)
        // "testb4znbuh3n2" is a dedicated test ad unit ID. Before releasing your app, replace the test ad unit ID with the formal one.
        interstitialAd!!.adId = "testb4znbuh3n2"

        displayRadioGroup = findViewById(R.id.display_radio_group)
        loadAdButton = findViewById(R.id.load_ad)
        loadAdButton!!.setOnClickListener(View.OnClickListener { loadInterstitialAd() })
        loadInterstitialAd()
    }

    private val adId: String
        private get() = if (displayRadioGroup!!.checkedRadioButtonId == R.id.display_image) {
            // The value of image_ad_id is teste9ih9j0rc3.
            getString(R.string.image_ad_id)
        } else {
            // The value of video_ad_id is testb4znbuh3n2.
            getString(R.string.video_ad_id)
        }

    private fun loadInterstitialAd() {
        // Load an interstitial ad.
        interstitialAd = InterstitialAd(this)
        // Sets an ad unit ID.
        interstitialAd!!.adId = adId
        interstitialAd!!.adListener = adListener
        val adParam = AdParam.Builder().build()
        interstitialAd!!.loadAd(adParam)

    }

    private fun showInterstitialAd() {
        // Display the ad.
        if (interstitialAd != null && interstitialAd!!.isLoaded) {
            interstitialAd!!.show(this)
        } else {
            Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show()
        }
    }


    private val adListener: AdListener = object : AdListener() {
        override fun onAdLoaded() {
            // Called when an ad is loaded successfully.
            super.onAdLoaded()
            Toast.makeText(this@InterstitialAdsActivity, "Ad loaded", Toast.LENGTH_SHORT).show()
            // Display an interstitial ad.
            showInterstitialAd()
        }
        override fun onAdFailed(errorCode: Int) {
            // Called when an ad fails to be loaded.
            Toast.makeText(this@InterstitialAdsActivity, "Ad load failed with error code: $errorCode",
                Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Ad load failed with error code: $errorCode")
        }
        override fun onAdClosed() {
            // Called when an ad is closed.
            super.onAdClosed()
            Log.d(TAG, "onAdClosed")
        }
        override fun onAdClicked() {
            // Called when an ad is clicked.
            super.onAdClicked()
            Log.d(TAG, "onAdClicked")
        }
        override fun onAdLeave() {
            // Called when an ad leaves an app.
            super.onAdLeave()
            Log.d(TAG, "onAdLeave")
        }
        override fun onAdOpened() {
            // Called when an ad is opened.
            super.onAdOpened()
            Log.d(TAG, "onAdOpened")
        }
        override fun onAdImpression() {
            // Called when an ad impression occurs.
            super.onAdImpression()
            Log.d(TAG, "onAdImpression")
        }
    }
}