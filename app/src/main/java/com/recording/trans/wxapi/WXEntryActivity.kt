package com.recording.trans.wxapi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.baidu.mobads.action.ActionParam
import com.baidu.mobads.action.ActionType
import com.baidu.mobads.action.BaiduAction
import com.recording.trans.R
import com.recording.trans.bean.UserInfo
import com.recording.trans.controller.Constant
import com.recording.trans.controller.IMManager
import com.recording.trans.controller.LogReportManager
import com.recording.trans.controller.PayManager
import com.recording.trans.http.loader.TokenLoader
import com.recording.trans.http.loader.UserInfoLoader
import com.recording.trans.http.response.ResponseTransformer
import com.recording.trans.http.schedulers.SchedulerProvider
import com.recording.trans.utils.JLog
import com.recording.trans.utils.RomUtil
import com.recording.trans.utils.ToastUtil
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.json.JSONObject

class WXEntryActivity : Activity(), CoroutineScope by MainScope(), IWXAPIEventHandler {
    private var api: IWXAPI? = null
    private var mmkv = MMKV.defaultMMKV()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        api = Constant.api
        if (api == null) {
            finish()
        }
        try {
            val intent = intent
            api!!.handleIntent(intent, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        api!!.handleIntent(intent, this)
    }

    override fun onReq(req: BaseReq) {
        when (req.type) {
            ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX -> {
            }
            ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX -> {
            }
            else -> {
            }
        }
        finish()
    }

    override fun onResp(resp: BaseResp) {
        when (resp.errCode) {
            BaseResp.ErrCode.ERR_OK -> {
                if (resp.type == ConstantsAPI.COMMAND_PAY_BY_WX) {
                    paySuccess()
                } else {
                    getAuthSuccess(resp)
                }
                R.string.errcode_success
            }
            BaseResp.ErrCode.ERR_USER_CANCEL -> R.string.errcode_cancel
            BaseResp.ErrCode.ERR_AUTH_DENIED -> R.string.errcode_deny
            BaseResp.ErrCode.ERR_UNSUPPORT -> R.string.errcode_unsupported
            else -> R.string.errcode_unknown
        }
        finish()
    }

    private fun paySuccess() {
        JLog.i("weChat pay success")
    }

    private fun getAuthSuccess(resp: BaseResp) {
        val code = (resp as SendAuth.Resp).code
        JLog.i("code = $code")
        getAccessToken(code)
    }

    private fun getAccessToken(code: String) {
        launch(Dispatchers.IO) {
            TokenLoader.getToken(this@WXEntryActivity)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    Constant.QUEST_TOKEN = it.questToken
                    getUserInfo(code)
                }, {
                    ToastUtil.show(this@WXEntryActivity, "获取验证失败，请重试")
                })
        }
    }

    private fun getUserInfo(code: String) {
        launch(Dispatchers.IO) {
            UserInfoLoader.getUser(Constant.QUEST_TOKEN, code)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    if (it.isNotEmpty()) {
                        Constant.CLIENT_TOKEN = it[0].client_token
                        Constant.USER_NAME = it[0].nickname
                        Constant.USER_ID = it[0].id.toString()
                        Constant.USER_ICON = it[0].avatar

                        val userInfo = UserInfo(
                            it[0].id,
                            it[0].nickname,
                            it[0].user_type,
                            it[0].addtime,
                            it[0].last_logintime,
                            it[0].login_ip,
                            it[0].popularize_id,
                            it[0].pop_name,
                            it[0].client_token,
                            it[0].city,
                            it[0].avatar
                        )

                        mmkv?.encode("userInfo", userInfo)

                        //active upload
                        if (!RomUtil.isOppo() && Constant.OCPC) {
                            BaiduAction.logAction(ActionType.REGISTER)
//                            val actionParam = JSONObject()
//                            actionParam.put(ActionParam.Key.PURCHASE_MONEY, 100)
//                            BaiduAction.logAction(ActionType.PURCHASE, actionParam)
                        }

                        //IM register
                        IMManager.register(Constant.USER_ID, {}, {})

                        //report
                        LogReportManager.logReport("登录", "登录成功", LogReportManager.LogType.LOGIN)

                        ToastUtil.showShort(this@WXEntryActivity, "登录成功")
                        if (Constant.mHandler != null) {
                            Constant.mHandler.sendEmptyMessage(0x1000)
                        }

                        if (Constant.mSecondHandler != null) {
                            Constant.mSecondHandler.sendEmptyMessage(0x1000)
                        }
                    }
                }, {
                    JLog.i("error = ${it.message}")
                })
        }
    }

}