package com.recording.trans.http.loader

import com.recording.trans.bean.WechatPayParam
import com.recording.trans.controller.Constant
import com.recording.trans.controller.RetrofitServiceManager
import com.recording.trans.http.response.Response
import com.recording.trans.utils.AppUtil
import io.reactivex.Observable

object WechatPayLoader {

    fun getOrderParam(serviceId: Int): Observable<Response<WechatPayParam>> {
        return RetrofitServiceManager.getInstance().wechatPayStatus.getOrderParam(
            serviceId,
            Constant.CLIENT_TOKEN,
            Constant.PRODUCT_ID,
            AppUtil.getChannelId()
        )
    }
}