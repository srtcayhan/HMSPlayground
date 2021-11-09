package com.example.srtcayhan.hmsaccountkit

import android.app.Application
import com.huawei.hms.ads.HwAds
import com.huawei.hms.maps.MapsInitializer

class HmsAccountKit : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize the Ads SDK.
        HwAds.init(this)
        MapsInitializer.setApiKey("CwEAAAAAJo5BV02u3Cu2mxr18WIZKXpQEUF7jSI1oLZOHqvSlmKg1Mg0k8we0IiGORkM0hrIhBauG2vMz6Hw25PNoETSsNzqIYA=")
    }
}