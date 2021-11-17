package com.example.srtcayhan.hmsaccountkit.site

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.srtcayhan.hmsaccountkit.R
import com.huawei.hms.site.api.SearchResultListener
import com.huawei.hms.site.api.SearchService
import com.huawei.hms.site.api.SearchServiceFactory
import com.huawei.hms.site.api.model.*
import java.io.UnsupportedEncodingException
import java.net.URLEncoder


class SiteActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var searchService: SearchService
    private lateinit var resultTextView: TextView
    private lateinit var queryInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_site)

        try {
            searchService =
                SearchServiceFactory.create(this, URLEncoder.encode("CwEAAAAAJo5BV02u3Cu2mxr18WIZKXpQEUF7jSI1oLZOHqvSlmKg1Mg0k8we0IiGORkM0hrIhBauG2vMz6Hw25PNoETSsNzqIYA=", "utf-8"))
        } catch (e: UnsupportedEncodingException) {
            Log.e(TAG, "encode apikey error")
        }

        queryInput = findViewById(R.id.edit_text_text_search_query)
        resultTextView = findViewById(R.id.response_text_search)
    }

    fun search(view: View?) {
        val textSearchRequest = TextSearchRequest()
        textSearchRequest.query = queryInput.text.toString()
        textSearchRequest.hwPoiType = HwLocationType.TOWER
        searchService.textSearch(textSearchRequest, object :
            SearchResultListener<TextSearchResponse> {
            override fun onSearchResult(textSearchResponse: TextSearchResponse) {
                val response = StringBuilder("\n")
                response.append("success\n")
                var count = 1
                var addressDetail: AddressDetail
                for (site in textSearchResponse.sites) {
                    addressDetail = site.address
                    response.append(
                        String.format(
                            "[%s]  name: %s, formatAddress: %s, country: %s, countryCode: %s \r\n",
                            "" + count++, site.name, site.formatAddress,
                            if (addressDetail == null) "" else addressDetail.country,
                            if (addressDetail == null) "" else addressDetail.countryCode
                        )
                    )
                }
                Log.d(TAG, "search result is : $response")
                resultTextView.text = response.toString()
            }

            override fun onSearchError(searchStatus: SearchStatus) {
                Log.e(TAG, "onSearchError is: " + searchStatus.errorCode)
            }
        })
    }

}
