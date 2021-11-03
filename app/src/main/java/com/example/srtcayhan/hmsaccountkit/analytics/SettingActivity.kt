package com.example.srtcayhan.hmsaccountkit.analytics

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.example.srtcayhan.hmsaccountkit.R
import com.huawei.hms.analytics.HiAnalytics
import com.huawei.hms.analytics.HiAnalyticsInstance

// This activity stands for analytics kit

class SettingActivity : AppCompatActivity() {

    private lateinit var btnSave: Button
    private lateinit var editFavorSport: EditText
    private lateinit var strFavorSport: String

    // Define a variable for the Analytics Kit instance.
    private lateinit var instance: HiAnalyticsInstance


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        // Generate an Analytics Kit instance.
        instance = HiAnalytics.getInstance(this)

        btnSave = findViewById(R.id.save_setting_button)
        editFavorSport = findViewById(R.id.edit_favorite_sport)

        btnSave.setOnClickListener {
            strFavorSport = editFavorSport.text.toString().trim()

            // Set users' favorite sport using the setUserProfile API.
            instance.setUserProfile("favor_sport", strFavorSport)


        }
    }
}