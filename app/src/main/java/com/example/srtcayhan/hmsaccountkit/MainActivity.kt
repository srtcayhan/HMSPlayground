package com.example.srtcayhan.hmsaccountkit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.example.srtcayhan.hmsaccountkit.databinding.ActivityMainBinding
import com.huawei.hmf.tasks.Task
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.account.AccountAuthManager
import com.huawei.hms.support.account.request.AccountAuthParams
import com.huawei.hms.support.account.request.AccountAuthParamsHelper
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

        binding.silentSignInButton.setOnClickListener { silentSignIn() }

        binding.signOutButton.setOnClickListener { signOut() }

        binding.cancelAuthButton.setOnClickListener { cancelAuth() }

        logTextView = findViewById<View>(R.id.LogText) as TextView
    }


    private fun signInByHwId() {

        authParams =
            AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setIdToken()
                .setAccessToken()
                .createParams()

        mAuthManager =
            AccountAuthManager.getService(this@MainActivity, authParams)

        startActivityForResult(mAuthManager?.signInIntent, 8888)

    }

    private fun silentSignIn() {
        val authParams: AccountAuthParams =
            AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setIdToken()
                .createParams()

        val service: AccountAuthService =
            AccountAuthManager.getService(this@MainActivity, authParams)

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
                showLog(" signIn success ")
                showLog("AccessToken: " + authAccount.getAccessToken())

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
        signOutTask.addOnSuccessListener({
            Log.i(TAG, "signOut Success")
            showLog("signOut Success")
        }).addOnFailureListener {
            Log.e(TAG, "signOut fail")
            showLog("signOut fail")
        }
    }

    private fun showLog(log: String) {
        logTextView!!.text = "log:\n$log"
    }

    private fun cancelAuth() {
        mAuthManager?.cancelAuthorization()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Processing after a successful authorization revoking.
                Log.i(TAG, "onSuccess: ")
            } else {
                // Handle the exception.
                val exception = task.exception
                if (exception is ApiException) {
                    val statusCode = exception.statusCode
                    Log.i(TAG, "onFailure: $statusCode")
                }
            }
        }
    }

}