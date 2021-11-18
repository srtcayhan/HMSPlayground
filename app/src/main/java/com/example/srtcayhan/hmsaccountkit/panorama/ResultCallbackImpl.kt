package com.example.srtcayhan.hmsaccountkit.panorama

import android.content.Context
import com.huawei.hms.panorama.PanoramaInterface.ImageInfoResult
import com.huawei.hms.support.api.client.ResultCallback

class ResultCallbackImpl(context: Context) : ResultCallback<ImageInfoResult> {

    private var context: Context? = null

    init {
        this.context = context
    }


    override fun onResult(result: ImageInfoResult?) {
        result?.let {
            if (result.status.isSuccess) {
                val intent = result.imageDisplayIntent
                intent?.let {
                    context?.startActivity(intent)
                }
            }
        }

    }

}