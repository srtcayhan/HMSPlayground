package com.example.srtcayhan.hmsaccountkit.safetydetect

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.srtcayhan.hmsaccountkit.R
import kotlinx.android.synthetic.main.activity_safety_detect.*

class SafetyDetectActivity : AppCompatActivity(), View.OnClickListener {

    private var fManager: FragmentManager? = null

    private var fragmentAppsCheck: Fragment? = null
    private var fragmentSysIntegrityCheck: Fragment? = null
    private var fragmentUrlCheck: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_safety_detect)
        fManager = supportFragmentManager
        bindViews()
        txt_sysintegrity.performClick()
    }


    private fun bindViews() {
        txt_sysintegrity.setOnClickListener(this)
        txt_appscheck.setOnClickListener(this)
        txt_urlcheck.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val fragTransaction = supportFragmentManager.beginTransaction()
        hideAllFragment(fragTransaction)
        when (v.id) {
            R.id.txt_appscheck -> {
                setSelected()
                txt_appscheck.isSelected = true
                txt_topbar.text = getString(R.string.title_activity_apps_check)
                if (fragmentAppsCheck == null) {
                    fragmentAppsCheck = SafetyDetectAppsCheckAPIFragment()
                    fragmentAppsCheck?.let { fragTransaction.add(R.id.ly_content, it) }
                } else {
                    fragmentAppsCheck?.let { fragTransaction.show(it) }
                }
            }
            R.id.txt_sysintegrity -> {
                setSelected()
                txt_sysintegrity.isSelected = true
                txt_topbar.text = getString(R.string.title_activity_sys_integrity)
                if (fragmentSysIntegrityCheck == null) {
                    fragmentSysIntegrityCheck = SafetyDetectSysIntegrityAPIFragment()
                    fragmentSysIntegrityCheck?.let { fragTransaction.add(R.id.ly_content, it) }
                } else {
                    fragmentSysIntegrityCheck?.let { fragTransaction.show(it) }
                }
            }
            R.id.txt_urlcheck -> {
                setSelected()
                txt_urlcheck.isSelected = true
                txt_topbar.text = getString(R.string.title_url_check_entry)
                if (fragmentUrlCheck == null) {
                    fragmentUrlCheck = SafetyDetectUrlCheckAPIFragment()
                    fragmentUrlCheck?.let { fragTransaction.add(R.id.ly_content, it) }
                } else {
                    fragmentUrlCheck?.let { fragTransaction.show(it) }
                }
            }
        }
        fragTransaction.commit()
    }

    private fun setSelected() {
        txt_appscheck.isSelected = false
        txt_sysintegrity.isSelected = false
        txt_urlcheck.isSelected = false

    }

    private fun hideAllFragment(fragmentTransaction: FragmentTransaction) {
        if (fragmentAppsCheck != null) {
            fragmentAppsCheck?.let { fragmentTransaction.hide(it) }
        }
        if (fragmentSysIntegrityCheck != null) {
            fragmentSysIntegrityCheck?.let { fragmentTransaction.hide(it) }
        }
        if (fragmentUrlCheck != null) {
            fragmentUrlCheck?.let { fragmentTransaction.hide(it) }
        }

    }

    companion object {
        const val TAG = "SafetyDetectActivity"
    }
}