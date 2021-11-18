package com.example.srtcayhan.hmsaccountkit.panorama

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.appcompat.app.AppCompatActivity
import com.example.srtcayhan.hmsaccountkit.R
import com.huawei.hms.panorama.Panorama
import com.huawei.hms.panorama.PanoramaInterface
import com.huawei.hms.panorama.PanoramaLocalApi
import kotlinx.android.synthetic.main.activity_local_interface.*

class LocalInterfaceActivity : AppCompatActivity(), View.OnClickListener, OnTouchListener {


    private lateinit var mLocalInterface: PanoramaInterface.PanoramaLocalInterface
    private var mChangeButtonCompass = false


    companion object {
        private const val TAG: String = "LocalInterfaceActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local_interface)
        initView()
    }

    private fun initView() {
        val intent = intent
        val uri = intent.data
        val type = intent.getIntExtra("PanoramaType", PanoramaInterface.IMAGE_TYPE_SPHERICAL)
        callLocalApi(uri, type)
    }

    private fun callLocalApi(uri: Uri?, type: Int) {
        mLocalInterface = Panorama.getInstance().getLocalInstance(this)
        mLocalInterface.init()
        if (mLocalInterface.init() == 0 && mLocalInterface.setImage(uri, type) == 0) {
            val view: View = mLocalInterface.view
            relativeLayout.addView(view)

            // update MotionEvent to the image.
            view.setOnTouchListener(this@LocalInterfaceActivity)
            changeButton.apply {
                bringToFront()
                setOnClickListener(this@LocalInterfaceActivity)

            }
        } else {
            Log.e(TAG, "local api error")
        }

    }


    /**
     * write to common function to return the image or the resource file
     */
    override fun onClick(v: View?) {
        if (v?.id == R.id.changeButton) {
            if (mChangeButtonCompass) {
                mChangeButtonCompass = false
                mLocalInterface.setControlMode(PanoramaInterface.CONTROL_TYPE_TOUCH)
                mLocalInterface.setImage(
                    returnResource(R.raw.pano),
                    PanoramaLocalApi.IMAGE_TYPE_SPHERICAL
                )
            } else {
                mChangeButtonCompass = true
                mLocalInterface.setControlMode(PanoramaInterface.CONTROL_TYPE_POSE)
                mLocalInterface.setImage(
                    returnResource(R.raw.pano2),
                    PanoramaLocalApi.IMAGE_TYPE_SPHERICAL
                )
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        mLocalInterface.let {
            mLocalInterface.updateTouchEvent(event)
        }
        return true
    }

    private fun returnResource(drawable: Int): Uri {
        return Uri.parse("android.resource://$packageName/$drawable")
    }

}