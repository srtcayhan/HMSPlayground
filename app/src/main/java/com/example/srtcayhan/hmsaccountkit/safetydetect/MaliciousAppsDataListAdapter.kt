package com.example.srtcayhan.hmsaccountkit.safetydetect

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater.from
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.srtcayhan.hmsaccountkit.R
import com.huawei.hms.support.api.entity.safetydetect.MaliciousAppsData
import com.huawei.hms.support.api.safetydetect.AppsCheckConstants
import kotlinx.android.synthetic.main.item_list_app.view.*


class MaliciousAppsDataListAdapter(data: List<MaliciousAppsData>, context: Context) :
    BaseAdapter() {

    private val maliciousAppsData: MutableList<MaliciousAppsData> = ArrayList()
    private val context: Context
    override fun getCount(): Int {
        return maliciousAppsData.size
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val appsDataView: View = from(context).inflate(R.layout.item_list_app, parent, false)
        appsDataView.apply {
            val oneMaliciousAppsData = maliciousAppsData[position]
            txt_aName.text = oneMaliciousAppsData.apkPackageName
            txt_aCategory.text = getCategory(oneMaliciousAppsData.apkCategory)
            return appsDataView
        }
    }

    private fun getCategory(apkCategory: Int): String {
        return when (apkCategory) {
            AppsCheckConstants.VIRUS_LEVEL_RISK -> context.getString(R.string.app_type_risk)
            AppsCheckConstants.VIRUS_LEVEL_VIRUS -> context.getString(R.string.app_type_virus)
            else -> context.getString(R.string.app_type_virus)
        }
    }

    init {
        maliciousAppsData.addAll(data)
        this.context = context
    }
}
