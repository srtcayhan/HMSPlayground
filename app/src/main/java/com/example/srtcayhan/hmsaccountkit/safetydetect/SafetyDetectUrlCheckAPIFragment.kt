package com.example.srtcayhan.hmsaccountkit.safetydetect

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.srtcayhan.hmsaccountkit.R
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.api.entity.safetydetect.UrlCheckThreat
import com.huawei.hms.support.api.safetydetect.SafetyDetect
import com.huawei.hms.support.api.safetydetect.SafetyDetectClient
import com.huawei.hms.support.api.safetydetect.SafetyDetectStatusCodes
import kotlinx.android.synthetic.main.fg_urlcheck.*

class SafetyDetectUrlCheckAPIFragment : Fragment(),
    AdapterView.OnItemSelectedListener, View.OnClickListener {

    private var client: SafetyDetectClient? = null

    companion object {
        val TAG: String = SafetyDetectUrlCheckAPIFragment::class.java.simpleName
        private const val APP_ID = "104854133"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        client = SafetyDetect.getClient(activity)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fg_urlcheck, container, false)
    }

    override fun onResume() {
        super.onResume()
        client?.initUrlCheck()
    }

    override fun onPause() {
        super.onPause()
        client?.shutdownUrlCheck()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fg_call_url_btn.setOnClickListener(this)
        fg_url_spinner.onItemSelectedListener = this
        val adapter = activity?.applicationContext?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.url_array,
                android.R.layout.simple_spinner_item
            )
        }
        adapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fg_url_spinner.adapter = adapter
    }

    override fun onClick(view: View) {
        if (view.id == R.id.fg_call_url_btn) {
            callUrlCheckApi()
        }
    }

    override fun onItemSelected(
        adapterView: AdapterView<*>,
        view: View,
        pos: Int,
        id: Long
    ) {
        val url = adapterView.getItemAtPosition(pos) as String
        fg_call_urlCheck_text.setText(url)
        fg_call_urlResult.setText("")
    }

    override fun onNothingSelected(adapterView: AdapterView<*>?) {}

    private fun callUrlCheckApi() {
        Log.i(TAG, "Start call URL check api")
        val realUrl = fg_call_urlCheck_text.text.toString().trim()
        client?.urlCheck(
            realUrl,
            APP_ID,  // Specify url threat type
            UrlCheckThreat.MALWARE,
            UrlCheckThreat.PHISHING
        )?.addOnSuccessListener {

            /**
             * Called after successfully communicating with the SafetyDetect API.
             * The #onSuccess callback receives an
             * [com.huawei.hms.support.api.entity.safetydetect.UrlCheckResponse] that contains a
             * list of UrlCheckThreat that contains the threat type of the Url.
             */
            // Indicates communication with the service was successful.
            // Identify any detected threats.
            // Call getUrlCheckResponse method of UrlCheckResponse then you can get List<UrlCheckThreat> .
            // If List<UrlCheckThreat> is empty , that means no threats found , else that means threats found.
            val list = it.urlCheckResponse
            fg_call_urlResult.setText(
                if (list.isEmpty()) {
                    // No threats found.
                    "No threats found."
                } else {
                    // Threats found!
                    "Threats found!"
                }
            )
        }?.addOnFailureListener {
            /**
             * Called when an error occurred when communicating with the SafetyDetect API.
             */
            // An error with the Huawei Mobile Service API contains some additional details.
            val errorMsg: String? = if (it is ApiException) {
                "Error: ${SafetyDetectStatusCodes.getStatusCodeString(it.statusCode)}: ${it.message}"
                // You can use the apiException.getStatusCode() method to get the status code.
                // Note: If the status code is SafetyDetectStatusCodes.CHECK_WITHOUT_INIT, you need to call initUrlCheck().
            } else {
                // Unknown type of error has occurred.
                it.message
            }
            if (errorMsg != null) {
                Log.d(TAG, errorMsg)
            }
            Toast.makeText(activity?.applicationContext, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }

}