package com.example.srtcayhan.hmsaccountkit.fido

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Handler
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.srtcayhan.hmsaccountkit.R
import com.example.srtcayhan.hmsaccountkit.databinding.ActivityFidoBinding
import com.huawei.hms.support.api.fido.bioauthn.*
import java.util.concurrent.Executors

class FidoActivity : AppCompatActivity() {


    private lateinit var binding: ActivityFidoBinding
    private var resultTextView: TextView? = null

    private var bioAuthnPrompt: BioAuthnPrompt? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fido)
        resultTextView = findViewById(R.id.resultTextView)

        binding = ActivityFidoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnTextFingerAuthWithoutCrpObj.setOnClickListener { FingerAuthWithoutCrpObj() }

        binding.btnTextFaceAuthWithCrpObj.setOnClickListener {
            createBioAuthFace()
        }
    }

    @SuppressLint("SetTextI18n")
    fun FingerAuthWithoutCrpObj(){

        bioAuthnPrompt = createBioAuthnPrompt()
        val bioAuthnManager = BioAuthnManager(this)
        val errorCode = bioAuthnManager.canAuth()
        if (errorCode != 0) {
            resultTextView!!.text = ""
            showResult("Can not authenticate. errorCode=$errorCode")
        }

        val builder = BioAuthnPrompt.PromptInfo.Builder().setTitle("This is the title.")
            .setSubtitle("This is the subtitle")
            .setDescription("This is the description")

        builder.setDeviceCredentialAllowed(true)

        val info = builder.build()
        resultTextView!!.text = "Start fingerprint authentication without CryptoObject.\nAuthenticating......\n"
        bioAuthnPrompt!!.auth(info)

    }

    fun createBioAuthFace()  {
        //P40 Lite is not support this function.
        val permissionCheck = ContextCompat.checkSelfPermission(this@FidoActivity, Manifest.permission.CAMERA)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            showResult("The camera permission is not enabled. Please enable it.")

            ActivityCompat.requestPermissions(this@FidoActivity, arrayOf(Manifest.permission.CAMERA), 1)
            return
        }

        val callback = object : BioAuthnCallback() {
            override fun onAuthError(errMsgId: Int, errString: CharSequence) {
                showResult(
                    "Authentication error. errorCode=" + errMsgId + ",errorMessage=" + errString
                            + if (errMsgId == 1012) " The camera permission may not be enabled." else ""
                )
            }

            override fun onAuthHelp(helpMsgId: Int, helpString: CharSequence) {
                resultTextView!!
                    .append("Authentication help. helpMsgId=$helpMsgId,helpString=$helpString\n")
            }

            override fun onAuthSucceeded(result: BioAuthnResult) {
                showResult("Authentication succeeded.")
            }

            override fun onAuthFailed() {
                showResult("Authentication failed.")
            }
        }
        val cancellationSignal = CancellationSignal()
        val faceManager = FaceManager(this)
        val flags = 0
        val handler: Handler? = null
        val crypto: CryptoObject? = null
        faceManager.auth(crypto, cancellationSignal, flags, callback, handler)

    }

    fun createBioAuthnPrompt(): BioAuthnPrompt {
        // Callback.
        val callback = object : BioAuthnCallback() {
            override fun onAuthError(errMsgId: Int, errString: CharSequence) {
                showResult("Authentication error. errorCode=$errMsgId,errorMessage=$errString")
            }

            override fun onAuthSucceeded(result: BioAuthnResult) {
                if (result.cryptoObject != null) {
                    showResult("Authentication succeeded. CryptoObject=" + result.cryptoObject!!)
                } else {
                    showResult("Authentication succeeded. CryptoObject=null")
                }
            }

            override fun onAuthFailed() {
                showResult("Authentication failed.")
            }
        }
        return BioAuthnPrompt(this, ContextCompat.getMainExecutor(this), callback)


    }

    fun showResult(msg: String) {
        val builder = AlertDialog.Builder(this@FidoActivity)
        builder.setTitle("Authentication Result")
        builder.setMessage(msg)
        builder.setPositiveButton("OK", null)
        builder.show()
        resultTextView!!.append(msg + "\n")
    }


}