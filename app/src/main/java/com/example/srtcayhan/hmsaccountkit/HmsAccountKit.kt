package com.example.srtcayhan.hmsaccountkit

import android.app.Application
import com.huawei.hms.ads.HwAds

class HmsAccountKit : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize the Ads SDK.
        HwAds.init(this)
    }
}