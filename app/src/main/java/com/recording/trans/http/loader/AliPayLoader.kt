package com.recording.trans.http.loader

import com.recording.trans.bean.AlipayParam
import com.recording.trans.controller.Constant
import com.recording.trans.controller.RetrofitServiceManager
import com.recording.trans.http.response.Response
import com.recording.trans.utils.AppUtil
import io.reactivex.Observable

object AliPayLoader {

    fun getOrderParam(serviceId: Int,isBack:Int): Observable<Response<AlipayParam>> {
        return RetrofitServiceManager.getInstance().aliPayParam.getOrderParam(
            serviceId,
            Constant.CLIENT_TOKEN,
            Constant.PRODUCT_ID,
            AppUtil.getChannelId(),
            isBack
        )
    }
}