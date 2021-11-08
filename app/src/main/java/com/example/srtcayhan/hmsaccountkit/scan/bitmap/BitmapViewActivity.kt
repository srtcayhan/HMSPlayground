package com.example.srtcayhan.hmsaccountkit.scan.bitmap

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.srtcayhan.hmsaccountkit.R
import com.example.srtcayhan.hmsaccountkit.databinding.ActivityBitmapViewBinding
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions

class BitmapViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBitmapViewBinding

    val BITMAP = 0x22;
    val REQUEST_CODE_PHOTO = 0x33;
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bitmap_view)

        binding = ActivityBitmapViewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnBitmap.setOnClickListener {
            newViewBtnClick()
        }
    }


    fun newViewBtnClick() {
        // BITMAP is used for receiving the permission verification result. You can change it as required.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                BITMAP
            )
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (permissions == null || grantResults == null || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            return
        }
        if (requestCode == BITMAP) {
            // Call the system album.
            val pickIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            this@BitmapViewActivity.startActivityForResult(pickIntent, REQUEST_CODE_PHOTO)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //receive result after your activity finished scanning
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data == null) {
            return
        }
        if (requestCode == REQUEST_CODE_PHOTO) {
            // Obtain the image path.
            val path = getImagePath(this@BitmapViewActivity, data)
            if (TextUtils.isEmpty(path)) {
                return
            }
            // Obtain the bitmap from the image path.
            val bitmap = ScanUtil.compressBitmap(this@BitmapViewActivity, path)
            // Call the decodeWithBitmap method to pass the bitmap.
            val result1 = ScanUtil.decodeWithBitmap(this@BitmapViewActivity, bitmap, HmsScanAnalyzerOptions.Creator().setHmsScanTypes(0).setPhotoMode(false).create())
            // Obtain the scanning result.
            if (result1 != null && result1.size > 0) {
                if (!TextUtils.isEmpty(result1[0].getOriginalValue())) {
                    Toast.makeText(this, result1[0].getOriginalValue(), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun getImagePath(context: Context, data: Intent): String? {
        var imagePath: String? = null
        val uri = data.data
        //get api version
        val currentapiVersion = Build.VERSION.SDK_INT
        if (currentapiVersion > Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                if ("com.android.providers.media.documents" == uri!!.authority) {
                    val id = docId.split(":").toTypedArray()[1]
                    val selection = MediaStore.Images.Media._ID + "=" + id
                    imagePath = getImagePath(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection)
                } else if ("com.android.providers.downloads.documents" == uri.authority) {
                    val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), docId.toLong())
                    imagePath = getImagePath(context, contentUri, null)
                } else {
                    Log.i(TAG, "getImagePath  uri.getAuthority():" + uri.authority)
                }
            } else if ("content".equals(uri!!.scheme, ignoreCase = true)) {
                imagePath = getImagePath(context, uri, null)
            } else {
                Log.i(TAG, "getImagePath  uri.getScheme():" + uri.scheme)
            }
        } else {
            imagePath = getImagePath(context, uri, null)
        }
        return imagePath
    }

    @SuppressLint("Range")
    private fun getImagePath(context: Context, uri: Uri?, selection: String?): String? {
        var path: String? = null
        val cursor = context.contentResolver.query(uri!!, null, selection, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path
    }


}