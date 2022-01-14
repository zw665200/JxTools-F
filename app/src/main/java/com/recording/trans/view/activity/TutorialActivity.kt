package com.recording.trans.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import com.recording.trans.R
import com.recording.trans.view.base.BaseActivity

class TutorialActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var webview: WebView
    private val url1 = "http://www.quanjingke.com/index.php?m=vryun&c=panohtmlout&a=imUse&jqid=2037"
    private val url2 = "http://down.ql-recovery.com/step2.html"
    private var step = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun setLayout(): Int {
        return R.layout.a_tutorial
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        back.setOnClickListener { finish() }

        webview = findViewById(R.id.webview)
    }

    override fun initData() {
        step = intent.getIntExtra("step", 1)
        when (step) {
            1 -> webview.loadUrl(url1)
            2 -> webview.loadUrl(url1)
        }

        initWebview()
    }

    @SuppressLint("setJavaScriptEnabled")
    private fun initWebview() {
        webview.settings.apply {
            javaScriptEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
        }
        webview.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
//                view?.loadUrl(url)
                return true
            }
        }
    }


}