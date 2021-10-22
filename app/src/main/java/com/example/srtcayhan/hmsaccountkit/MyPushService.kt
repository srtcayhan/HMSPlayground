package com.example.srtcayhan.hmsaccountkit

import android.os.Bundle
import android.util.Log
import com.huawei.hms.push.HmsMessageService

class MyPushService : HmsMessageService() {
    val TAG = "PushDemoLog"
     override fun onNewToken(token: String?, bundle: Bundle?) {
        Log.i(TAG, "have received refresh token:$token")
    }
}