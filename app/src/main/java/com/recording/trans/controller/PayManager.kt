package com.recording.trans.controller

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.alipay.sdk.app.PayTask
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.tencent.mmkv.MMKV
import com.recording.trans.bean.*
import com.recording.trans.callback.PayCallback
import com.recording.trans.http.loader.*
import com.recording.trans.http.response.ResponseTransformer
import com.recording.trans.http.schedulers.SchedulerProvider
import com.recording.trans.utils.JLog
import com.recording.trans.utils.ToastUtil
import com.recording.trans.view.activity.LoginActivity
import kotlinx.coroutines.*
import kotlin.concurrent.thread

class PayManager private constructor() : CoroutineScope by MainScope() {

    companion object {

        @Volatile
        private var instance: PayManager? = null

        fun get(): PayManager {
            if (instance == null) {
                synchronized(PayManager::class) {
                    if (instance == null) {
                        instance = PayManager()
                    }
                }
            }

            return instance!!
        }
    }

    fun checkPayResult(activity: Activity, result: (Boolean) -> Unit) {
        if (Constant.CLIENT_TOKEN == "") {
            val mmkv = MMKV.defaultMMKV()
            val userInfo = mmkv?.decodeParcelable("userInfo", UserInfo::class.java)
            if (userInfo != null) {
                Constant.CLIENT_TOKEN = userInfo.client_token
            } else {
                result(false)
                return
            }
        }

        getPayStatus { result(it) }
    }


    /**
     * 检查套餐
     * @param context
     * @param result
     */
    fun checkPay(activity: Activity, result: (Boolean) -> Unit) {
        if (Constant.CLIENT_TOKEN == "") {
            val mmkv = MMKV.defaultMMKV()
            val userInfo = mmkv?.decodeParcelable("userInfo", UserInfo::class.java)
            if (userInfo != null) {
                Constant.CLIENT_TOKEN = userInfo.client_token
            } else {
                activity.startActivity(Intent(activity, LoginActivity::class.java))
                return
            }
        }

        getPayStatus { result(it) }
    }

    private fun getPayStatus(result: (Boolean) -> Unit) {
        launch(Dispatchers.IO) {
            OrderLoader.getOrders()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    if (!it.isNullOrEmpty()) {
                        result(true)
                    } else {
                        result(false)
                    }
                }, {
                    JLog.i("getPayStatus error")
                })
        }
    }


    fun getPayList(activity: Activity, result: (List<Order>) -> Unit) {
        if (Constant.TEST == 1) {
            return
        }

        if (Constant.CLIENT_TOKEN == "") {
            val mmkv = MMKV.defaultMMKV()
            val userInfo = mmkv?.decodeParcelable("userInfo", UserInfo::class.java)
            if (userInfo != null) {
                Constant.CLIENT_TOKEN = userInfo.client_token
            } else {
                activity.startActivity(Intent(activity, LoginActivity::class.java))
                return
            }
        }

        launch(Dispatchers.IO) {
            OrderLoader.getOrders()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    if (it != null) {
                        result(it)
                    }
                }, {})
        }
    }

    fun getPayList(result: (List<Order>) -> Unit) {

        if (Constant.CLIENT_TOKEN == "") {
            val mmkv = MMKV.defaultMMKV()
            val userInfo = mmkv?.decodeParcelable("userInfo", UserInfo::class.java)
            if (userInfo != null) {
                Constant.CLIENT_TOKEN = userInfo.client_token
                Constant.USER_ID = userInfo.id.toString()
                Constant.USER_NAME = userInfo.nickname
                Constant.USER_ICON = userInfo.avatar
            } else {
                return
            }
        }

        launch(Dispatchers.IO) {
            OrderLoader.getOrders()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    if (it != null) {
                        result(it)
                    }
                }, {})
        }
    }

    @SuppressLint("CheckResult")
    fun getServiceList(activity: Activity, result: (List<Price>) -> Unit) {
        ServiceListLoader.getServiceList()
            .compose(ResponseTransformer.handleResult())
            .compose(SchedulerProvider.getInstance().applySchedulers())
            .subscribe({
                if (it.isNotEmpty()) {
                    result(it)
                }
            }, {
                ToastUtil.show(activity, "获取服务列表失败")
            })
    }

    @SuppressLint("CheckResult")
    fun getPayStatus(context: Context, serviceCode: String, success: (PayStatus) -> Unit) {
        val service = MMKV.defaultMMKV()?.decodeParcelable(serviceCode, Price::class.java)
        if (service != null) {
            thread {
                PayStatusLoader.getPayStatus(service.id, Constant.CLIENT_TOKEN)
                    .compose(ResponseTransformer.handleResult())
                    .compose(SchedulerProvider.getInstance().applySchedulers())
                    .subscribe({
                        if (it.isEmpty()) return@subscribe
                        success(it[0])
                    }, {
                        ToastUtil.showShort(context, "获取支付状态失败")
                    })
            }
        } else {
            ToastUtil.show(context, "获取支付状态失败")
        }
    }


    /**
     * 支付宝支付
     */
    @SuppressLint("CheckResult")
    fun doAliPay(activity: Activity, serviceId: Int, isBack: Int, callback: PayCallback) {
        if (Constant.CLIENT_TOKEN == "") {
            val mmkv = MMKV.defaultMMKV()
            val userInfo = mmkv?.decodeParcelable("userInfo", UserInfo::class.java)
            if (userInfo != null) {
                Constant.CLIENT_TOKEN = userInfo.client_token
            } else {
                activity.startActivity(Intent(activity, LoginActivity::class.java))
                return
            }
        }

        thread {
            AliPayLoader.getOrderParam(serviceId, isBack)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    checkOrderStatus(activity, it, callback)
                }, {
                    ToastUtil.show(activity, "发起支付请求失败")
                })
        }
    }

    /**
     * 快付
     */
    @SuppressLint("CheckResult")
    fun doFastPay(activity: Activity, serviceId: Int, callback: PayCallback) {
        thread {
            FastPayParamLoader.getOrderParam(serviceId)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    checkFastPay(activity, it, callback)
                }, {
                    ToastUtil.show(activity, "发起支付请求失败")
                })
        }
    }

    /**
     * 微信支付
     */
    @SuppressLint("CheckResult")
    fun doWechatPay(activity: Activity, serviceId: Int, callback: PayCallback) {
        thread {
            WechatPayLoader.getOrderParam(serviceId)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    checkWechatPay(activity, it, callback)
                }, {
                    ToastUtil.show(activity, "发起支付请求失败")
                })
        }
    }

    private fun checkOrderStatus(activity: Activity, order: AlipayParam, callback: PayCallback) {
        launch(Dispatchers.IO) {
            JLog.i("param = ${order.body}")
            JLog.i("orderSn = ${order.orderSn}")

            val task = PayTask(activity)
            val result = task.payV2(order.body, true)
            val res = PayResult(result)
            val resultStatus = res.resultStatus

            if (resultStatus == "9000") {
                JLog.i("alipay success")

                callback.progress(order.orderSn)
                callback.success()

                OrderDetailLoader.getOrderStatus(order.orderSn)
                    .compose(ResponseTransformer.handleResult())
                    .compose(SchedulerProvider.getInstance().applySchedulers())
                    .subscribe({
                        if (it.order_sn != order.orderSn) {
                            return@subscribe
                        }

                        when (it.status) {
//                            "1" -> callback.success()
//                            "0" -> callback.failed("未支付")
//                            "2" -> callback.failed("退款中")
//                            "3" -> callback.failed("已退款")
//                            "4" -> callback.failed("已取消")
                        }
                    }, {
                        JLog.i("${it.message}")
                    })

            } else {
                //支付失败，也需要发起服务端校验
                JLog.i("alipay failed")

                callback.failed("已取消")

//                launch(Dispatchers.IO) {
//                    OrderCancelLoader.orderCancel(order.orderSn)
//                        .compose(ResponseTransformer.handleResult())
//                        .compose(SchedulerProvider.getInstance().applySchedulers())
//                        .subscribe({}, {
//                            JLog.i("${it.message}")
//                        })
//                }
            }
        }

    }

    private fun checkFastPay(activity: Activity, order: FastPayParam, callback: PayCallback) {
        callback.progress(order.orderSn)

        val page = order.body
//        JLog.i("cd = ${page.orderCd}")
//        JLog.i("sign = ${page.sign}")


        //alipay
//        val intent = Intent()
//        intent.setClass(activity, FastPayActivity::class.java)
//        intent.putExtra("title","支付")
//        intent.putExtra("page", page)
//        activity.startActivity(intent)

        val url = "alipayqr://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=$page"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        activity.startActivity(intent)

        //wechat pay
//        val sb = StringBuffer()
//        sb.append("orderCd=")
//        sb.append(page.orderCd)
//        sb.append("&")
//        sb.append("sign=")
//        sb.append(page.sign)
//        sb.append("&")
//        val api = WXAPIFactory.createWXAPI(activity, Constant.TENCENT_APP_ID)
//        val req = WXLaunchMiniProgram.Req()
//        req.userName = Constant.TENCENT_MINI_PROGRAM_APP_ID
//        req.path = "pages/index/index?$sb"
//        req.miniprogramType = WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE
//        api.sendReq(req)

    }

    private fun checkWechatPay(activity: Activity, order: WechatPayParam, callback: PayCallback) {
        callback.progress(order.orderSn)

        JLog.i("发起支付")
        //wechat pay
        val api = WXAPIFactory.createWXAPI(activity, Constant.TENCENT_APP_ID, false)
        api.registerApp(Constant.TENCENT_APP_ID)

        val request = PayReq()
        request.appId = Constant.TENCENT_APP_ID
        request.partnerId = Constant.TENCENT_PARTNER_ID
        request.prepayId = order.body
        request.packageValue = "Sign=WXPay"
        request.nonceStr = order.noncestr
        request.timeStamp = order.timestamp.toString()
        request.sign = order.sign
        api.sendReq(request)
    }

}