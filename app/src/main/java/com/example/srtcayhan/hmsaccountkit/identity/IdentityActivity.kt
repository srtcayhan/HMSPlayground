package com.example.srtcayhan.hmsaccountkit.identity

import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.util.Log
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.example.srtcayhan.hmsaccountkit.R
import com.example.srtcayhan.hmsaccountkit.databinding.ActivityIdentityBinding
import com.huawei.hmf.tasks.OnFailureListener
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.hmf.tasks.Task
import com.huawei.hms.identity.Address
import com.huawei.hms.identity.entity.GetUserAddressResult
import com.huawei.hms.identity.entity.UserAddress
import com.huawei.hms.identity.entity.UserAddressRequest
import com.huawei.hms.support.api.client.Status


class IdentityActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIdentityBinding

    companion object {
        private const val TAG = "identitycodelab"
        private const val GET_ADDRESS = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identity)

        binding = ActivityIdentityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.queryUserAddress.setOnClickListener {
            getUserAddress()
        }
    }

    private fun getUserAddress() {
        val req = UserAddressRequest()
        val task: Task<GetUserAddressResult> = Address.getAddressClient(this).getUserAddress(req)
        task.addOnSuccessListener(OnSuccessListener<GetUserAddressResult> { result ->
            Log.i(TAG, "onSuccess result code:" + result.returnCode)
            try {
                startActivityForResult(result)
            } catch (e: SendIntentException) {
                e.printStackTrace()
            }
        }).addOnFailureListener(OnFailureListener { e ->
            Log.i(
                TAG,
                "on Failed result code:" + e.message
            )
        })
    }

    @Throws(SendIntentException::class)
    private fun startActivityForResult(result: GetUserAddressResult) {
        val status: Status = result.status
        if (result.returnCode == 0 && status.hasResolution()) {
            Log.i(TAG, "the result had resolution.")
            status.startResolutionForResult(this, GET_ADDRESS)
        } else {
            Log.i(TAG, "the response is wrong, the return code is " + result.returnCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //ToDo：override the onActivityResult
        Log.i(TAG, "onActivityResult requestCode $requestCode resultCode $resultCode")
        when (requestCode) {
            GET_ADDRESS -> when (resultCode) {
                RESULT_OK -> {
                    val userAddress = UserAddress.parseIntent(data)
                    if (userAddress != null) {
                        val sb = StringBuilder()
                        sb.append("name:" + userAddress.name + ",")
                        sb.append("city:" + userAddress.administrativeArea + ",")
                        sb.append("area:" + userAddress.locality + ",")
                        sb.append("address:" + userAddress.addressLine1 + userAddress.addressLine2 + ",")
                        sb.append("phone:" + userAddress.phoneNumber)
                        Log.i(TAG, "user address is $sb")
                        binding.userAddress.text = (sb.toString())
                    } else {
                        binding.userAddress.text = ("Failed to get user address.")
                    }
                }
                RESULT_CANCELED -> {
                }
                else -> Log.i(TAG, "result is wrong, result code is $resultCode")
            }
            else -> {
            }
        }
    }

}