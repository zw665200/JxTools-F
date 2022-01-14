package com.recording.trans.view.activity

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.*
import android.widget.ImageView
import com.recording.trans.R
import com.recording.trans.bean.Location
import com.recording.trans.view.base.BaseActivity

class PanoStreetActivity : BaseActivity() {
    private lateinit var webView: WebView
    private lateinit var finish: ImageView
    private var location: Location? = null

    override fun setLayout(): Int {
        return R.layout.a_panostreet
    }


    @SuppressLint("SetJavaScriptEnabled")
    override fun initView() {
        webView = findViewById(R.id.webview)
        finish = findViewById(R.id.close)

        finish.setOnClickListener { finish() }

        val setting = webView.settings
        setting.javaScriptEnabled = true

        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url
                if (url != null) {
                    view?.loadUrl(url.toString())
                }
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                openPano()
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                return super.onJsAlert(view, url, message, result)
            }
        }

        webView.loadUrl("file:///android_asset/pano.html")
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun initData() {
        location = intent.getParcelableExtra("location")
    }

    private fun openPano() {
        if (location != null) {
            webView.evaluateJavascript("openPano(${location!!.lat}, ${location!!.lng});", null)
        }
    }


    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        val parent = webView.parent as ViewGroup
        parent.removeView(webView)

        webView.stopLoading()
        webView.clearHistory()
        webView.removeAllViewsInLayout()
        webView.removeAllViews()
        webView.destroy()
        super.onDestroy()
    }
}