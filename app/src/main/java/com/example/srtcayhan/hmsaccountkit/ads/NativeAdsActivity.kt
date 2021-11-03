package com.example.srtcayhan.hmsaccountkit.ads

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.srtcayhan.hmsaccountkit.R
import com.example.srtcayhan.hmsaccountkit.databinding.ActivityAdBinding
import com.example.srtcayhan.hmsaccountkit.databinding.ActivityNativeAdsBinding
import com.huawei.hms.ads.AdListener
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.HwAds
import com.huawei.hms.ads.VideoOperator
import com.huawei.hms.ads.nativead.*

class NativeAdsActivity : AppCompatActivity() {

    private lateinit var binding : ActivityNativeAdsBinding
    private var small: RadioButton? = null
    private var video: RadioButton? = null
    private var loadBtn: Button? = null
    private var adScrollView: ScrollView? = null
    private var layoutId = 0
    private var globalNativeAd: NativeAd? = null

    private val videoLifecycleListener: VideoOperator.VideoLifecycleListener =
        object : VideoOperator.VideoLifecycleListener() {
            override fun onVideoStart() {
                updateStatus(getString(R.string.status_play_start), false)
            }

            override fun onVideoPlay() {
                updateStatus(getString(R.string.status_playing), false)
            }

            override fun onVideoEnd() {
                // If there is a video, load a new native ad only after video playback is complete.
                updateStatus(getString(R.string.status_play_end), true)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_native_ads)

        binding = ActivityNativeAdsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Initialize the HUAWEI Ads SDK.
        HwAds.init(this)
        small = binding.radioButtonSmall
        video = binding.radioButtonVideo
        loadBtn = binding.btnLoad
        adScrollView = binding.scrollViewAd
        loadBtn!!.setOnClickListener(View.OnClickListener { loadAd(adId) })
        loadAd(adId)
    }

    private val adId: String
        private get() {
            val adId: String
            layoutId = R.layout.native_video_template
            if (small!!.isChecked) {
                adId = getString(R.string.ad_id_native_small)
                layoutId = R.layout.native_small_template
            } else if (video!!.isChecked) {
                adId = getString(R.string.ad_id_native_video)
            } else {
                adId = getString(R.string.ad_id_native)
            }
            return adId
        }

    private fun loadAd(adId: String) {
        updateStatus(null, false)
        val builder = NativeAdLoader.Builder(this, adId)
        builder.setNativeAdLoadedListener { nativeAd ->
            // Call this method when an ad is successfully loaded.
            updateStatus(getString(R.string.status_load_ad_success), true)

            // Display native ad.
            showNativeAd(nativeAd)
            nativeAd.setDislikeAdListener { // Call this method when an ad is closed.
                updateStatus(getString(R.string.ad_is_closed), true)
            }
        }.setAdListener(object : AdListener() {
            override fun onAdFailed(errorCode: Int) {
                // Call this method when an ad fails to be loaded.
                updateStatus(getString(R.string.status_load_ad_fail) + errorCode, true)
            }
        })
        val adConfiguration = NativeAdConfiguration.Builder()
            .setChoicesPosition(NativeAdConfiguration.ChoicesPosition.BOTTOM_RIGHT) // Set custom attributes.
            .build()
        val nativeAdLoader = builder.setNativeAdOptions(adConfiguration).build()
        nativeAdLoader.loadAd(AdParam.Builder().build())
        updateStatus(getString(R.string.status_ad_loading), false)
    }

    private fun showNativeAd(nativeAd: NativeAd) {
        // Destroy the original native ad.
        if (null != globalNativeAd) {
            globalNativeAd!!.destroy()
        }
        globalNativeAd = nativeAd

        // Obtain NativeView.
        val nativeView = layoutInflater.inflate(layoutId, null) as NativeView

        // Register and populate a native ad material view.
        initNativeAdView(globalNativeAd, nativeView)

        // Add NativeView to the app UI.
        adScrollView!!.removeAllViews()
        adScrollView!!.addView(nativeView)
    }

    private fun initNativeAdView(nativeAd: NativeAd?, nativeView: NativeView) {
        // Register a native ad material view.
        nativeView.titleView = nativeView.findViewById(R.id.ad_title)
        nativeView.mediaView = nativeView.findViewById<View>(R.id.ad_media) as MediaView
        nativeView.adSourceView = nativeView.findViewById(R.id.ad_source)
        nativeView.callToActionView = nativeView.findViewById(R.id.ad_call_to_action)

        // Populate a native ad material view.
        (nativeView.titleView as TextView).text = nativeAd!!.title
        nativeView.mediaView.setMediaContent(nativeAd.mediaContent)
        if (null != nativeAd.adSource) {
            (nativeView.adSourceView as TextView).text = nativeAd.adSource
        }
        nativeView.adSourceView.visibility =
            if (null != nativeAd.adSource) View.VISIBLE else View.INVISIBLE
        if (null != nativeAd.callToAction) {
            (nativeView.callToActionView as Button).text = nativeAd.callToAction
        }
        nativeView.callToActionView.visibility =
            if (null != nativeAd.callToAction) View.VISIBLE else View.INVISIBLE

        // Obtain a video controller.
        val videoOperator = nativeAd.videoOperator

        // Check whether a native ad contains video materials.
        if (videoOperator.hasVideo()) {
            // Add a video lifecycle event listener.
            videoOperator.videoLifecycleListener = videoLifecycleListener
        }

        // Register a native ad object.
        nativeView.setNativeAd(nativeAd)
    }

    private fun updateStatus(text: String?, loadBtnEnabled: Boolean) {
        if (null != text) {
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
        }
        loadBtn!!.isEnabled = loadBtnEnabled
    }

    override fun onDestroy() {
        if (null != globalNativeAd) {
            globalNativeAd!!.destroy()
        }
        super.onDestroy()
    }


}