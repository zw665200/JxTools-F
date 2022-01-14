package com.recording.trans.http.loader

import android.content.Context
import android.os.Build
import com.recording.trans.bean.GetToken
import com.recording.trans.bean.Token
import com.recording.trans.controller.Constant
import com.recording.trans.controller.RetrofitServiceManager
import com.recording.trans.http.response.Response
import com.recording.trans.utils.AES
import com.recording.trans.utils.AppUtil
import com.recording.trans.utils.DeviceUtil
import io.reactivex.Observable

object TokenLoader {

    fun getToken(context: Context): Observable<Response<Token>> {
        val brand = Build.BRAND
        val mode = Build.MODEL
        val deviceId = DeviceUtil.getDeviceId(context)
        val device = Build.DEVICE
        val time = System.currentTimeMillis()
        val questTime = AppUtil.timeStamp2Date(time.toString(), null)
        val questFrom = AppUtil.getChannelId()

        val questToken = AES.encrypt(questFrom, AppUtil.MD5Encode((time / 1000).toString()) + questFrom)
        val productId = Constant.PRODUCT_ID

        val token = GetToken(questTime, questToken, questFrom, device, deviceId, brand, mode, productId)

        return RetrofitServiceManager.getInstance().token.getToken(token)
    }
}