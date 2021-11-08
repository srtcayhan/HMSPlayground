package com.example.srtcayhan.hmsaccountkit.scan.defaultview

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.example.srtcayhan.hmsaccountkit.R
import com.example.srtcayhan.hmsaccountkit.databinding.ActivityDefaultViewBinding
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions

class DefaultViewActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDefaultViewBinding

    private val DEFINED_CODE = 222
    private val REQUEST_CODE_SCAN = 0X01
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_default_view)

        binding = ActivityDefaultViewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.defaultView.setOnClickListener {
            newDefaultViewBtnClick()
        }
    }

    private fun newDefaultViewBtnClick() {
        // Replace DEFAULT_VIEW with the code that you customize for receiving the permission verification result.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            this.requestPermissions(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE),
                DEFINED_CODE)
        }
    }
    override
    fun onRequestPermissionsResult(requestCode:Int, permissions:Array<out String>, grantResults:IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissions == null || grantResults.size< 2 || grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
            return
        }
        if (requestCode == DEFINED_CODE) {
            // Display the scanning UI in Default View mode.

            ScanUtil.startScan(this@DefaultViewActivity, REQUEST_CODE_SCAN, HmsScanAnalyzerOptions.Creator().setHmsScanTypes(
                HmsScan.ALL_SCAN_TYPE).create())
        }
    }


    override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent?) {
        // Process the result after the scanning is complete.
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data == null) {
            return
        }
        // Use ScanUtil.RESULT as the key value to obtain the return value of HmsScan from data returned by the onActivityResult method.
        else if (requestCode == REQUEST_CODE_SCAN) {
            when (val obj: Parcelable? = data.getParcelableExtra(ScanUtil.RESULT)) {
                is HmsScan -> {
                    if (!TextUtils.isEmpty(obj.getOriginalValue())) {
                        Toast.makeText(this, obj.getOriginalValue(), Toast.LENGTH_SHORT).show()
                    }
                    return
                }
            }
        }
    }


}