package com.example.srtcayhan.hmsaccountkit

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import com.example.srtcayhan.hmsaccountkit.ads.AdActivity
import com.example.srtcayhan.hmsaccountkit.analytics.AnalyticsActivity
import com.example.srtcayhan.hmsaccountkit.databinding.ActivityMainBinding
import com.example.srtcayhan.hmsaccountkit.drive.DriveActivity
import com.example.srtcayhan.hmsaccountkit.identity.IdentityActivity
import com.example.srtcayhan.hmsaccountkit.location.LocationActivity
import com.example.srtcayhan.hmsaccountkit.map.MapActivity
import com.example.srtcayhan.hmsaccountkit.safetydetect.SafetyDetectActivity
import com.example.srtcayhan.hmsaccountkit.scan.customizedview.CustomizedViewActivity
import com.example.srtcayhan.hmsaccountkit.site.SiteActivity
import com.huawei.hmf.tasks.Task
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.account.AccountAuthManager
import com.huawei.hms.support.account.request.AccountAuthParams
import com.huawei.hms.support.account.request.AccountAuthParamsHelper
import com.huawei.hms.support.account.result.AuthAccount
import com.huawei.hms.support.account.service.AccountAuthService


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val TAG = "HuaweiIdActivity"

    private var logTextView: TextView? = null

    var authParams: AccountAuthParams? = null

    var mAuthManager: AccountAuthService? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.signInHwaButton.setOnClickListener { signInByHwId() }

        binding.silentSignInButton.setOnClickListener { silentSignInByHwId() }

        binding.authSignInButton.setOnClickListener { authModeSignIn() }

        binding.signOutButton.setOnClickListener { signOut() }

        binding.cancelAuthButton.setOnClickListener { cancelAuth() }

        binding.btnAnalytics.setOnClickListener {
            val intent = Intent(this, AnalyticsActivity::class.java)
            startActivity(intent)
        }

        binding.btnAd.setOnClickListener {
            val intent = Intent(this, AdActivity::class.java)
            startActivity(intent)
        }
        binding.btnLocation.setOnClickListener {
            val intent = Intent(this, LocationActivity::class.java)
            startActivity(intent)
        }

        binding.btnScan.setOnClickListener {
            val intent = Intent(this, CustomizedViewActivity::class.java)
            startActivity(intent)
        }

        binding.btnMap.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

        binding.btnSafety.setOnClickListener {
            val intent = Intent(this, SafetyDetectActivity::class.java)
            startActivity(intent)
        }
        binding.btnDrive.setOnClickListener {
            val intent = Intent(this, DriveActivity::class.java)
            startActivity(intent)
        }

        binding.btnSite.setOnClickListener {
            val intent = Intent(this, SiteActivity::class.java)
            startActivity(intent)
        }

        binding.btnIdentity.setOnClickListener {
            val intent = Intent(this, IdentityActivity::class.java)
            startActivity(intent)
        }

        logTextView = findViewById<View>(R.id.LogText) as TextView

        getToken()

    }


    private fun signInByHwId() {

        authParams =
            AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setIdToken()
                .setAccessToken()
                .createParams()

        mAuthManager = AccountAuthManager.getService(this@MainActivity, authParams)

        startActivityForResult(mAuthManager?.signInIntent, 8888)

    }

    private fun silentSignInByHwId() {
        val authParams: AccountAuthParams =
            AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setIdToken()
                .createParams()

        val service: AccountAuthService =
            AccountAuthManager.getService(this@MainActivity, authParams)

        val task : Task<AuthAccount> = service.silentSignIn()

        task.addOnSuccessListener { authAccount ->
            // Obtain the user's ID information.
            showLog("Silent sign in success")
        }

        task.addOnFailureListener { e ->
            // The sign-in failed. Your app can **getSignInIntent()**nIntent() method to explicitly display the authorization screen.
            if (e is ApiException) {
                showLog("sign failed status:" + e.statusCode)
            }
        }


    }

    private fun authModeSignIn(){
        val authParams : AccountAuthParams =  AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            .setAuthorizationCode()
            .createParams()

        val service : AccountAuthService = AccountAuthManager.getService(this@MainActivity, authParams)

        startActivityForResult(service.signInIntent, 8888)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Process the authorization result and obtain an ID to**AuthAccount**thAccount.

        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 8888) {
            val authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data)
            if (authAccountTask.isSuccessful) {
                // The sign-in is successful, and the user's ID information and ID token are obtained.
                val authAccount = authAccountTask.result
                Log.i(TAG, "idToken:" + authAccount.idToken)
                showLog("SignIn with Huawei Id Success \nName: ${authAccount.displayName} \nAccessToken:${authAccount.accessToken}  \n" +
                        "AuthorizationCode:${authAccount.authorizationCode}")


            } else {
                // The sign-in failed. No processing is required. Logs are recorded for fault locating.
                Log.e(
                    TAG,
                    "sign in failed : " + (authAccountTask.exception as ApiException).statusCode
                )
            }
        }
    }
    private fun signOut() {


        val signOutTask: Task<Void> = mAuthManager!!.signOut()
        signOutTask.addOnSuccessListener {
            Log.i(TAG, "signOut Success")
            showLog("signOut Success")
        }.addOnFailureListener {
            Log.e(TAG, "signOut fail")
            showLog("signOut fail")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showLog(log: String) {
        logTextView!!.text = "log:\n$log"
    }

    private fun cancelAuth() {
        mAuthManager?.cancelAuthorization()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Processing after a successful authorization revoking.
                showLog("Authorization Revoked")
            } else {
                // Handle the exception.
                val exception = task.exception
                if (exception is ApiException) {
                    val statusCode = exception.statusCode
                    showLog("Revoking Authorization Canceled")
                }
            }
        }
    }

    private fun getToken() {
        showLog("getToken:begin")
        object : Thread() {
            override fun run() {
                try {
                    // read from agconnect-services.json
                    val appId = "104854133"
                    val token = HmsInstanceId.getInstance(this@MainActivity).getToken(appId, "HCM")
                    Log.i(TAG, "get token:$token")
                    if (!TextUtils.isEmpty(token)) {
                        sendRegTokenToServer(token)
                    }
                    runOnUiThread {
                        showLog("get token:$token")
                    }

                } catch (e: ApiException) {
                    Log.e(TAG, "get token failed, $e")
                    runOnUiThread {
                        showLog("get token failed, $e")
                    }

                }
            }
        }.start()
    }

    private fun sendRegTokenToServer(token: String?) {
        Log.i(TAG, "sending token to server. token:$token")
    }

}