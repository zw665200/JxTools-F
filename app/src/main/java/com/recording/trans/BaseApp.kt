package com.recording.trans

import android.app.Application
import com.baidu.mobads.action.BaiduAction
import com.bun.miitmdid.core.JLibrary
import com.bytedance.sdk.openadsdk.TTAdConfig
import com.bytedance.sdk.openadsdk.TTAdConstant
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.hyphenate.chat.ChatClient
import com.hyphenate.helpdesk.easeui.UIProvider
import com.recording.trans.controller.Constant
import com.recording.trans.controller.RetrofitServiceManager
import com.recording.trans.utils.AppUtil
import com.recording.trans.utils.JLog
import com.recording.trans.utils.RomUtil
import com.tencent.bugly.Bugly
import com.tencent.mmkv.MMKV
import tech.oom.idealrecorder.IdealRecorder


class BaseApp : Application() {


    override fun onCreate() {
        super.onCreate()
        initData()
        initRom()
        initHttpRequest()
        initMMKV()
        initBaiduAction()
        initIM()
        initBugly()
        initRecorder()
//        initTTAd()
    }

    private fun initData() {
        if (AppUtil.isDebugger(this)) {
            Constant.isDebug = true
        }
    }

    /**
     * 读取设备信息
     *
     */
    private fun initRom() {
        val name = RomUtil.getName()
        JLog.i("name = $name")
        if (name != "") {
            Constant.ROM = name
        }
    }

    private fun initHttpRequest() {
        RetrofitServiceManager.getInstance().initRetrofitService()
    }


    /**
     * init mmkv
     */
    private fun initMMKV() {
        MMKV.initialize(this)
    }

    private fun initBaiduAction() {
        //OPPO用户不激活OCPC
        if (!RomUtil.isOppo() && Constant.OCPC) {
            JLibrary.InitEntry(this)
            BaiduAction.init(this, Constant.USER_ACTION_SET_ID, Constant.APP_SECRET_KEY)
            BaiduAction.setActivateInterval(this, 30)
            BaiduAction.setPrintLog(false)
        }
    }

    private fun initIM() {
        val option = ChatClient.Options()
        option.setAppkey(Constant.SDK_APP_KEY)
        option.setTenantId(Constant.SDK_APP_ID)

        if (!ChatClient.getInstance().init(this, option)) {
            return
        }

        UIProvider.getInstance().init(this)
    }

    private fun initBugly() {
        Bugly.init(applicationContext, Constant.BUGLY_APPID, false)
    }

    private fun initRecorder() {
        IdealRecorder.init(this)
    }

    private fun initTTAd() {
        TTAdSdk.getAdManager().requestPermissionIfNecessary(this)

        val config = TTAdConfig.Builder().appId("5237261")
            .useTextureView(true) //默认使用SurfaceView播放视频广告,当有SurfaceView冲突的场景，可以使用TextureView
            .appName("语音转文字专业版_android")
            .titleBarTheme(TTAdConstant.TITLE_BAR_THEME_DARK)//落地页主题
            .allowShowNotify(true) //是否允许sdk展示通知栏提示,若设置为false则会导致通知栏不显示下载进度
            .debug(false) //测试阶段打开，可以通过日志排查问题，上线时去除该调用
            .directDownloadNetworkType(TTAdConstant.NETWORK_STATE_WIFI) //允许直接下载的网络状态集合,没有设置的网络下点击下载apk会有二次确认弹窗，弹窗中会披露应用信息
            .supportMultiProcess(false) //是否支持多进程，true支持
            .asyncInit(true) //是否异步初始化sdk,设置为true可以减少SDK初始化耗时。3450版本开始废弃~~
            .build()

        TTAdSdk.init(this, config, object : TTAdSdk.InitCallback {
            override fun success() {
                JLog.i("ad init success")
            }

            override fun fail(p0: Int, p1: String?) {
            }
        })
    }


}