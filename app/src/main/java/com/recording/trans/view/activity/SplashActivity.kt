package com.recording.trans.view.activity

import android.content.Intent
import android.os.*
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bytedance.sdk.openadsdk.*
import com.recording.trans.R
import com.recording.trans.bean.UserInfo
import com.recording.trans.controller.Constant
import com.recording.trans.controller.DBManager
import com.recording.trans.controller.PayManager
import com.recording.trans.http.loader.ConfigLoader
import com.recording.trans.http.loader.ServiceListLoader
import com.recording.trans.http.response.ResponseTransformer
import com.recording.trans.http.schedulers.SchedulerProvider
import com.recording.trans.utils.JLog
import com.recording.trans.utils.ToastUtil
import com.recording.trans.view.base.BaseActivity
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.d_pics.*
import kotlinx.coroutines.*

/**
@author ZW
@description:
@date : 2020/11/25 10:31
 */
class SplashActivity : BaseActivity() {
    private lateinit var textView: TextView
    private lateinit var splashBg: ImageView
    private lateinit var timer: CountDownTimer
    private var kv = MMKV.defaultMMKV()
    private var show = false

    override fun setLayout(): Int {
        return R.layout.activity_splash
    }

    override fun initView() {
        textView = findViewById(R.id.splash_start)
        splashBg = findViewById(R.id.splash_bg)

        if (intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT != 0) {
            finish()
            return
        }

        initTimer()
        clearDatabase()
        getConfig()
        getServiceList()
        checkPayStatus()
        getUserInfo()
    }


    override fun initData() {
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus && !show) {
            val value = kv?.decodeBool("service_agree")
            if (value == null || !value) {
                val intent = Intent(this, AgreementActivity::class.java)
                startActivityForResult(intent, 0x1)
                show = true
            } else {
                timer.start()
            }
        }
        super.onWindowFocusChanged(hasFocus)
    }

    private fun initTimer() {
        timer = object : CountDownTimer(2 * 1000L, 1000) {
            override fun onFinish() {
                jumpTo()
            }

            override fun onTick(millisUntilFinished: Long) {
            }
        }
    }


    private fun jumpTo() {
        val intent = Intent()
        intent.setClass(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun clearDatabase() {
        launch(Dispatchers.IO) {
            DBManager.deleteFiles(this@SplashActivity)
        }
    }


    private fun getConfig() {
        launch {
            ConfigLoader.getConfig()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    Constant.WEBSITE = it.offcialSite
                }, {
                })
        }
    }

    private fun getServiceList() {
        launch {
            ServiceListLoader.getServiceList()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    if (it.isNotEmpty()) {
                        for (child in it) {
                            //save service list
                            MMKV.defaultMMKV()?.encode(child.server_code + child.expire_type, child)
                        }
                    }
                }, {
                    ToastUtil.show(this@SplashActivity, "获取服务列表失败")
                })
        }
    }

    private fun checkPayStatus() {
        PayManager.get().getPayList {
            if (it.isNotEmpty()) {
                val builder = StringBuilder()
                for (order in it) {

                    if (order.server_code == Constant.VOICE) {
                        when (order.expire_type) {
                            "1" -> builder.append("年度会员 ")
                            "3" -> builder.append("月度会员 ")
                        }

                    }
                }

                val text = builder.toString()
                if (text != "") {
                    Constant.USER_VIP_STATUS = text
                } else {
                    Constant.USER_VIP_STATUS = getString(R.string.mine_level_after)
                }
            } else {
                Constant.USER_VIP_STATUS = getString(R.string.mine_level_after)
            }
        }
    }

    private fun getUserInfo() {
        val mmkv = MMKV.defaultMMKV()
        val userInfo = mmkv?.decodeParcelable("userInfo", UserInfo::class.java)
        if (userInfo != null) {
            Constant.CLIENT_TOKEN = userInfo.client_token
            Constant.USER_ID = userInfo.id.toString()
            Constant.USER_NAME = userInfo.nickname
            Constant.USER_ICON = userInfo.avatar
        }
    }

    private fun openAd() {
        val mTTAdNative = TTAdSdk.getAdManager().createAdNative(this)
        val adSlot = AdSlot.Builder().setCodeId("887627235").setImageAcceptedSize(1080, 1920)
            .setAdLoadType(TTAdLoadType.LOAD)
            .build()

        mTTAdNative.loadSplashAd(adSlot, object : TTAdNative.SplashAdListener {
            override fun onError(code: Int, message: String?) {
                JLog.i("error code = $code")
                jumpTo()
            }

            override fun onTimeout() {
                JLog.i("on timeout")
                jumpTo()
            }

            override fun onSplashAdLoad(ad: TTSplashAd?) {

                if (ad == null) {
                    JLog.i("ad is null")
                    return
                }

                JLog.i("ad is not null")

                val view = ad.splashView
                if (view != null) {
                    (splashBg.parent as ViewGroup).addView(view)
                }

                ad.setSplashInteractionListener(object : TTSplashAd.AdInteractionListener {
                    override fun onAdClicked(p0: View?, p1: Int) {
                        timer.cancel()
                    }

                    override fun onAdShow(p0: View?, p1: Int) {
                        if (p0 != null) {
                            JLog.i("ad is show")
                            timer.start()
                        }
                    }

                    override fun onAdSkip() {
                        timer.cancel()
                        jumpTo()
                    }

                    override fun onAdTimeOver() {
//                        jumpTo()
                    }
                })
            }
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x1) {
            if (resultCode == 0x1) {
                kv?.encode("service_agree", true)
                timer.start()
            }

            if (resultCode == 0x2) {
                kv?.encode("service_agree", false)
                timer.start()
            }
        }
    }

}