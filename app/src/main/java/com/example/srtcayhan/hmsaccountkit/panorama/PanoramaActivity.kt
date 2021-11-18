package com.example.srtcayhan.hmsaccountkit.panorama

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.srtcayhan.hmsaccountkit.R
import com.huawei.hms.panorama.Panorama
import com.huawei.hms.panorama.PanoramaInterface
import kotlinx.android.synthetic.main.activity_drive.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_panorama.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_panorama.*

class PanoramaActivity : AppCompatActivity() {

    companion object {
        private const val TAG: String = "PanoramaActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panorama)
        initView()
    }


    /**
     * initialzing the view hear
     *
     * switch case will be a better implementation
     */
    private fun initView() {
        loadImageInfo.setOnClickListener {
            panoramaInterfaceLoadImageInfo()
        }
        loadImageInfoWithType.setOnClickListener {
            panoramaInterfaceLoadImageInfoWithType()
        }
        localInterface.setOnClickListener {
            panoramaInterfaceLocalInterface()
        }
        requestPermission()
    }


    /**
     * Panoroma kit with local interface
     *
     * getResource for loading the raw images
     */
    private fun panoramaInterfaceLocalInterface() {
        val intent = Intent(this, LocalInterfaceActivity::class.java)
        intent.apply {
            data = returnResource(R.raw.pano)
            putExtra("PanoramaType", PanoramaInterface.IMAGE_TYPE_SPHERICAL)
        }
        startActivity(intent)
    }

    /**
     * request permission for the application
     */
    private fun requestPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
        } else {
            Log.i(TAG, "permission ok")
        }
    }

    /**
     * Loading the panoroma Image Information
     */

    private fun panoramaInterfaceLoadImageInfo() {
        Panorama.getInstance().loadImageInfoWithPermission(this, returnResource(R.raw.pano))
            .setResultCallback(ResultCallbackImpl(this@PanoramaActivity))
    }

    /**
     * Loading the panoroma Image Information with information
     */

    private fun panoramaInterfaceLoadImageInfoWithType() {
        Panorama.getInstance()
            .loadImageInfoWithPermission(
                this,
                returnResource(R.raw.pano2),
                PanoramaInterface.IMAGE_TYPE_RING
            )
            .setResultCallback(ResultCallbackImpl(this@PanoramaActivity))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun returnResource(drawable: Int): Uri {
        return Uri.parse("android.resource://$packageName/$drawable")
    }
}
