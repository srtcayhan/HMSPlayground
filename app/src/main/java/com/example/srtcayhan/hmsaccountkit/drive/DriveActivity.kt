package com.example.srtcayhan.hmsaccountkit.drive

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import com.example.srtcayhan.hmsaccountkit.R
import com.huawei.cloud.base.auth.DriveCredential
import com.huawei.cloud.base.auth.DriveCredential.AccessMethod
import com.huawei.cloud.base.http.FileContent
import com.huawei.cloud.base.util.StringUtils
import com.huawei.cloud.client.exception.DriveCode
import com.huawei.cloud.services.drive.Drive
import com.huawei.cloud.services.drive.DriveScopes
import com.huawei.cloud.services.drive.model.*
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.api.entity.auth.Scope
import com.huawei.hms.support.hwid.HuaweiIdAuthAPIManager
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import java.io.FileOutputStream
import java.util.*

class DriveActivity : AppCompatActivity(), View.OnClickListener {
    private var mCredential: DriveCredential? = null

    // huawei account AT
    private var accessToken: String? = null
    private var unionId: String? = null
    private lateinit var directoryCreated: File
    private var fileUploaded: File? = null
    private var fileSearched: File? = null
    private var mComment: Comment? = null
    private var mReply: Reply? = null
    private var isApplicationData: CheckBox? = null
    private var uploadFileName: EditText? = null
    private var searchFileName: EditText? = null
    private var commentText: EditText? = null
    private var replyText: EditText? = null
    private var queryResult: TextView? = null
    private var commentList: TextView? = null
    private var replyList: TextView? = null
    private var historyVersionList: TextView? = null

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_SIGN_IN_LOGIN = 1002
        private val PERMISSIONS_STORAGE = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )
        private val MIME_TYPE_MAP: MutableMap<String, String> = HashMap()

        init {
            MIME_TYPE_MAP[".doc"] = "application/msword"
            MIME_TYPE_MAP[".jpg"] = "image/jpeg"
            MIME_TYPE_MAP[".mp3"] = "audio/x-mpeg"
            MIME_TYPE_MAP[".mp4"] = "video/mp4"
            MIME_TYPE_MAP[".pdf"] = "application/pdf"
            MIME_TYPE_MAP[".png"] = "image/png"
            MIME_TYPE_MAP[".txt"] = "text/plain"
        }
    }

    private val refreshAT = AccessMethod {
        accessToken!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drive)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "DriveApplication"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(PERMISSIONS_STORAGE, 1)
        }
        uploadFileName = findViewById(R.id.uploadFileName)
        searchFileName = findViewById(R.id.searchFileName)
        isApplicationData = findViewById(R.id.isApplicationData)
        queryResult = findViewById(R.id.queryResult)
        commentList = findViewById(R.id.commentList)
        replyList = findViewById(R.id.replyList)
        commentText = findViewById(R.id.commentText)
        replyText = findViewById(R.id.replyText)
        historyVersionList = findViewById(R.id.historyVersionList)
        findViewById<View>(R.id.buttonLogin).setOnClickListener(this)
        findViewById<View>(R.id.buttonUploadFiles).setOnClickListener(this)
        findViewById<View>(R.id.buttonQueryFiles).setOnClickListener(this)
        findViewById<View>(R.id.buttonDownloadFiles).setOnClickListener(this)
        findViewById<View>(R.id.buttonCreateComment).setOnClickListener(this)
        findViewById<View>(R.id.buttonQueryComment).setOnClickListener(this)
        findViewById<View>(R.id.buttonCreateReply).setOnClickListener(this)
        findViewById<View>(R.id.buttonQueryReply).setOnClickListener(this)
        findViewById<View>(R.id.buttonQueryHistoryVersion).setOnClickListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(
            TAG,
            "onActivityResult, requestCode = $requestCode, resultCode = $resultCode"
        )
        if (requestCode == REQUEST_SIGN_IN_LOGIN) {
            val authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data)
            if (authHuaweiIdTask.isSuccessful) {
                val huaweiAccount = authHuaweiIdTask.result
                accessToken = huaweiAccount.accessToken
                unionId = huaweiAccount.unionId
                val returnCode = init(unionId, accessToken, refreshAT)
                if (DriveCode.SUCCESS == returnCode) {
                    showTips("login ok")
                } else if (DriveCode.SERVICE_URL_NOT_ENABLED == returnCode) {
                    showTips("drive is not enabled")
                } else {
                    showTips("login error")
                }
            } else {
                Log.d(
                    TAG,
                    "onActivityResult, signIn failed: " + (authHuaweiIdTask.exception as ApiException).statusCode
                )
                Toast.makeText(
                    applicationContext,
                    "onActivityResult, signIn failed.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showTips(toastText: String) {
        runOnUiThread {
            Toast.makeText(applicationContext, toastText, Toast.LENGTH_LONG).show()
            val textView = findViewById<TextView>(R.id.textView)
            textView.text = toastText
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.buttonLogin -> driveLogin()
            R.id.buttonUploadFiles -> uploadFiles()
            R.id.buttonQueryFiles -> queryFiles()
            R.id.buttonDownloadFiles -> downloadFiles()
            R.id.buttonCreateComment -> createComment()
            R.id.buttonQueryComment -> queryComment()
            R.id.buttonCreateReply -> createReply()
            R.id.buttonQueryReply -> queryReply()
            R.id.buttonQueryHistoryVersion -> queryHistoryVersion()
            else -> {
            }
        }
    }

    private fun driveLogin() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        val scopeList: MutableList<Scope> = ArrayList()
        scopeList.add(Scope(DriveScopes.SCOPE_DRIVE))
        scopeList.add(Scope(DriveScopes.SCOPE_DRIVE_READONLY))
        scopeList.add(Scope(DriveScopes.SCOPE_DRIVE_FILE))
        scopeList.add(Scope(DriveScopes.SCOPE_DRIVE_METADATA))
        scopeList.add(Scope(DriveScopes.SCOPE_DRIVE_METADATA_READONLY))
        scopeList.add(Scope(DriveScopes.SCOPE_DRIVE_APPDATA))
        scopeList.add(HuaweiIdAuthAPIManager.HUAWEIID_BASE_SCOPE)
        val authParams = HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            .setAccessToken()
            .setIdToken()
            .setScopeList(scopeList)
            .createParams()
        val client = HuaweiIdAuthManager.getService(this, authParams)
        startActivityForResult(client.signInIntent, REQUEST_SIGN_IN_LOGIN)
    }

    private fun uploadFiles() {
        Thread(Runnable {
            try {
                if (accessToken == null) {
                    showTips("please click 'Login'.")
                    return@Runnable
                }
                if (StringUtils.isNullOrEmpty(uploadFileName!!.text.toString())) {
                    showTips("please input upload file name above.")
                    return@Runnable
                }
                val fileObject = java.io.File("/sdcard/" + uploadFileName!!.text)
                if (!fileObject.exists()) {
                    showTips("the input file does not exit.")
                    return@Runnable
                }
                val drive = buildDrive()
                val appProperties: MutableMap<String, String> = HashMap()
                appProperties["appProperties"] = "property"
                // create somepath directory
                val file = File()
                file.setFileName("somepath" + System.currentTimeMillis())
                    .setMimeType("application/vnd.huawei-apps.folder").appSettings =
                    appProperties
                if (isApplicationData!!.isChecked) {
                    file.parentFolder = listOf("applicationData")
                }
                directoryCreated = drive.files().create(file).execute()
                // create test.jpg on cloud
                val mimeType = mimeType(fileObject)
                val content = File()
                    .setFileName(fileObject.name)
                    .setMimeType(mimeType)
                    .setParentFolder(listOf(directoryCreated.getId()))
                fileUploaded = drive.files()
                    .create(content, FileContent(mimeType, fileObject))
                    .setFields("*")
                    .execute()
                showTips("upload success")
            } catch (ex: Exception) {
                Log.d(TAG, "upload error $ex")
                showTips("upload error $ex")
            }
        }).start()
    }

    private fun queryFiles() {
        Thread(Runnable {
            try {
                if (accessToken == null) {
                    showTips("please click 'Login'.")
                    return@Runnable
                }
                if (StringUtils.isNullOrEmpty(searchFileName!!.text.toString())) {
                    showTips("please input file name above.")
                    return@Runnable
                }
                var containers = ""
                var queryFile =
                    "fileName = '" + searchFileName!!.text + "' and mimeType != 'application/vnd.huawei-apps.folder'"
                if (isApplicationData!!.isChecked) {
                    containers = "applicationData"
                    queryFile = "'applicationData' in parentFolder and $queryFile"
                }
                val drive = buildDrive()
                val request = drive.files().list()
                var files: FileList?
                while (true) {
                    files = request
                        .setQueryParam(queryFile)
                        .setPageSize(10)
                        .setOrderBy("fileName")
                        .setFields("category,nextCursor,files(id,fileName,size)")
                        .setContainers(containers)
                        .execute()
                    if (files == null || files.files.size > 0) {
                        break
                    }
                    if (!StringUtils.isNullOrEmpty(files.nextCursor)) {
                        request.cursor = files.nextCursor
                    } else {
                        break
                    }
                }
                var text = ""
                if (files != null && files.files.size > 0) {
                    fileSearched = files.files[0]
                    text = fileSearched.toString()
                } else {
                    text = "empty"
                }
                val finalText = text
                runOnUiThread { queryResult!!.text = finalText }
                showTips("query ok")
            } catch (ex: Exception) {
                Log.d(TAG, "query error $ex")
                showTips("query error $ex")
            }
        }).start()
    }

    private fun downloadFiles() {
        Thread(Runnable {
            try {
                if (accessToken == null) {
                    showTips("please click 'Login'.")
                    return@Runnable
                }
                if (fileSearched == null) {
                    showTips("please click 'QUERY FILE'.")
                    return@Runnable
                }
                val drive = buildDrive()
                val content = File()
                val request = drive.files()[fileSearched!!.id]
                content.setFileName(fileSearched!!.fileName).id = fileSearched!!.id
                val downloader = request.mediaHttpDownloader
                downloader.setContentRange(0, fileSearched!!.getSize() - 1)
                val filePath =
                    "/storage/emulated/0/Huawei/Drive/DownLoad/Demo_" + fileSearched!!.fileName
                request.executeContentAndDownloadTo(FileOutputStream(java.io.File(filePath)))
                showTips("download to $filePath")
            } catch (ex: Exception) {
                Log.d(TAG, "download error $ex")
                showTips("download error $ex")
            }
        }).start()
    }

    private fun createComment() {
        Thread(Runnable {
            try {
                if (accessToken == null) {
                    showTips("please click 'Login'.")
                    return@Runnable
                }
                if (fileSearched == null) {
                    showTips("please click 'QUERY FILE'.")
                    return@Runnable
                }
                if (StringUtils.isNullOrEmpty(commentText!!.text.toString())) {
                    showTips("please input comment above.")
                    return@Runnable
                }
                val drive = buildDrive()
                val comment = Comment()
                comment.description = commentText!!.text.toString()
                mComment = drive.comments()
                    .create(fileSearched!!.id, comment)
                    .setFields("*")
                    .execute()
                if (mComment != null && mComment!!.id != null) {
                    Log.i(TAG, "Add comment success")
                    showTips("Add comment success")
                } else {
                    Log.e(TAG, "Add comment failed")
                    showTips("Add comment failed")
                }
            } catch (ex: Exception) {
                Log.d(TAG, "Add comment error $ex")
                showTips("Add comment error")
            }
        }).start()
    }

    private fun queryComment() {
        Thread(Runnable {
            try {
                if (accessToken == null) {
                    showTips("please click 'Login'.")
                    return@Runnable
                }
                if (fileSearched == null) {
                    showTips("please click 'QUERY FILE'.")
                    return@Runnable
                }
                val drive = buildDrive()
                val response = drive.comments()
                    .list(fileSearched!!.id)
                    .setFields("comments(id,description,replies(description))")
                    .execute()
                val text = response.comments.toString()
                runOnUiThread { commentList!!.text = text }
            } catch (ex: Exception) {
                Log.d(TAG, "query comment error $ex")
                showTips("query comment error")
            }
        }).start()
    }

    private fun createReply() {
        Thread(Runnable {
            try {
                if (accessToken == null) {
                    showTips("please click 'Login'.")
                    return@Runnable
                }
                if (fileSearched == null) {
                    showTips("please click 'QUERY FILE'.")
                    return@Runnable
                }
                if (mComment == null) {
                    showTips("please click 'COMMENT THE FILE'.")
                    return@Runnable
                }
                if (StringUtils.isNullOrEmpty(replyText!!.text.toString())) {
                    showTips("please input comment above.")
                    return@Runnable
                }
                val drive = buildDrive()
                val reply = Reply()
                reply.description = replyText!!.text.toString()
                mReply = drive.replies()
                    .create(fileSearched!!.id, mComment!!.id, reply)
                    .setFields("*")
                    .execute()
                if (mReply != null && mReply!!.id != null) {
                    Log.i(TAG, "Add reply success")
                    showTips("Add reply success")
                } else {
                    Log.e(TAG, "Add reply failed")
                    showTips("Add reply failed")
                }
            } catch (ex: Exception) {
                Log.d(TAG, "Add reply error $ex")
                showTips("Add reply error")
            }
        }).start()
    }

    private fun queryReply() {
        Thread(Runnable {
            try {
                if (accessToken == null) {
                    showTips("please click 'Login'.")
                    return@Runnable
                }
                if (fileSearched == null) {
                    showTips("please click 'QUERY FILE'.")
                    return@Runnable
                }
                if (mComment == null) {
                    showTips("please click 'COMMENT THE FILE'.")
                    return@Runnable
                }
                val drive = buildDrive()
                val response = drive.replies()
                    .list(fileSearched!!.id, mComment!!.id)
                    .setFields("replies(id,description)")
                    .execute()
                val text = response.replies.toString()
                runOnUiThread { replyList!!.text = text }
            } catch (ex: Exception) {
                Log.d(TAG, "query reply error $ex")
                showTips("query reply error")
            }
        }).start()
    }

    private fun queryHistoryVersion() {
        Thread(Runnable {
            try {
                if (accessToken == null) {
                    showTips("please click 'Login'.")
                    return@Runnable
                }
                if (fileSearched == null) {
                    showTips("please click 'QUERY FILE'.")
                    return@Runnable
                }
                val drive = buildDrive()
                val response = drive.historyVersions()
                    .list(fileSearched!!.id)
                    .setFields("historyVersions(id,sha256)")
                    .execute()
                val text = response.getHistoryVersions().toString()
                runOnUiThread { historyVersionList!!.text = text }
            } catch (ex: Exception) {
                Log.d(TAG, "query historyVersion", ex)
                showTips("query historyVersion error")
            }
        }).start()
    }

    private fun buildDrive(): Drive {
        return Drive.Builder(mCredential, this).build()
    }

    private fun mimeType(file: java.io.File?): String? {
        if (file != null && file.exists() && file.name.contains(".")) {
            val fileName = file.name
            val suffix = fileName.substring(fileName.lastIndexOf("."))
            if (MIME_TYPE_MAP.keys.contains(suffix)) {
                return MIME_TYPE_MAP[suffix]
            }
        }
        return "*/*"
    }

    /**
     * 通过上下文context和华为账号信息（unionId，countrycode，accessToken）初始化drive。
     * 当accessToken失效时，注册一个AccessMethod去获取一个新的accessToken。
     *
     * @param unionID   unionID from HwID
     * @param at        access token
     * @param refreshAT a callback to refresh AT
     */
    fun init(unionID: String?, at: String?, refreshAT: AccessMethod?): Int {
        if (StringUtils.isNullOrEmpty(unionID) || StringUtils.isNullOrEmpty(at)) {
            return DriveCode.ERROR
        }
        val builder = DriveCredential.Builder(unionID, refreshAT)
        mCredential = builder.build().setAccessToken(at)
        return DriveCode.SUCCESS
    }
}