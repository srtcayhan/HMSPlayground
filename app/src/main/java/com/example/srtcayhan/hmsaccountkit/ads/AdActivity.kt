package com.example.srtcayhan.hmsaccountkit.ads

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.srtcayhan.hmsaccountkit.R
import com.example.srtcayhan.hmsaccountkit.databinding.ActivityAdBinding
import com.huawei.hms.ads.HwAds
import com.huawei.hms.ads.banner.BannerView
import com.huawei.hms.ads.BannerAdSize
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.reward.Reward
import com.huawei.hms.ads.reward.RewardAd
import com.huawei.hms.ads.reward.RewardAdLoadListener
import com.huawei.hms.ads.reward.RewardAdStatusListener


class AdActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAdBinding

    private var bannerView: BannerView? = null

    private var watchAdButton: Button? = null
    private var rewardAd: RewardAd? = null
    private var scoreView: TextView? = null
    private var score = 1
    private val defaultScore = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ad)

        binding = ActivityAdBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        // Initialize the HUAWEI Ads SDK.
        HwAds.init(this)



        var bannerView: BannerView? = findViewById(R.id.hw_banner_view)
        // Set the ad unit ID and ad dimensions. "testw6vs28auh3" is a dedicated test ad unit ID.
        bannerView!!.adId = "testw6vs28auh3"
        bannerView!!.bannerAdSize = BannerAdSize.BANNER_SIZE_360_57
        // Set the refresh interval to 30 seconds.
        bannerView!!.setBannerRefresh(30)
        // Create an ad request to load an ad.
        val adParam = AdParam.Builder().build()
        bannerView!!.loadAd(adParam)

        binding.btnNativeAd.setOnClickListener {
            val intent = Intent(this, NativeAdsActivity::class.java)
            startActivity(intent)
        }

        binding.btnRewardedAd.setOnClickListener {
            loadRewardAd()

            // Load the button for watching a rewarded ad.
            loadWatchVideoButton()

            // Load a score view.
            loadScoreView()
        }

        binding.btnInterstitialAd.setOnClickListener {
            val intent = Intent(this, InterstitialAdsActivity::class.java)
            startActivity(intent)
        }

        binding.btnSplashAd.setOnClickListener {
            val intent = Intent(this, SplashActivity::class.java)
            startActivity(intent)
        }

    }

    private fun createRewardAd() {
        // testx9dtjwj8hp indicates a test ad unit ID.
         rewardAd = RewardAd(this@AdActivity, "testx9dtjwj8hp")
    }

    private fun loadRewardAd() {
        if (rewardAd == null) {
            createRewardAd()
        }
        val rewardAdLoadListener: RewardAdLoadListener = object : RewardAdLoadListener() {
            override fun onRewardAdFailedToLoad(errorCode: Int) {
                Toast.makeText(this@AdActivity, "onRewardAdFailedToLoad errorCode is :$errorCode", Toast.LENGTH_SHORT).show()
            }

            override fun onRewardedLoaded() {
                Toast.makeText(this@AdActivity, "onRewardedLoaded", Toast.LENGTH_SHORT).show()
            }
        }
        rewardAd!!.loadAd(AdParam.Builder().build(), rewardAdLoadListener)
    }

    /**
     * Load the button for watching a rewarded ad.
     */
    private fun loadWatchVideoButton() {
        watchAdButton = findViewById(R.id.show_video_button)
        watchAdButton!!.setOnClickListener(View.OnClickListener { rewardAdShow() })
    }

    private fun loadScoreView() {
        scoreView = findViewById(R.id.coin_count_text)
        scoreView!!.setText("Score:$score")
    }

    /**
     * Display a rewarded ad.
     */
    private fun rewardAdShow() {
        if (rewardAd!!.isLoaded) {
            rewardAd!!.show(this@AdActivity, object : RewardAdStatusListener() {
                override fun onRewardAdClosed() {
                    loadRewardAd()
                }

                override fun onRewardAdFailedToShow(errorCode: Int) {
                    Toast.makeText(this@AdActivity, "onRewardAdFailedToShow errorCode is :$errorCode", Toast.LENGTH_SHORT).show()
                }

                override fun onRewardAdOpened() {
                    Toast.makeText(this@AdActivity, "onRewardAdOpened", Toast.LENGTH_SHORT).show()
                }

                override fun onRewarded(reward: Reward) {
                    // You are advised to grant a reward immediately and at the same time, check whether the reward takes effect on the server.
                    // If no reward information is configured, grant a reward based on the actual scenario.
                    val addScore = if (reward.amount == 0) defaultScore else reward.amount
                    Toast.makeText(this@AdActivity, "Watch video show finished, add $addScore scores", Toast.LENGTH_SHORT).show()
                    addScore(addScore)
                    loadRewardAd()
                }
            })
        }
    }

    private fun addScore(addScore: Int) {
        score += addScore
        scoreView!!.text = "Score:$score"
    }
}