package com.recording.trans.http.loader

import com.recording.trans.bean.FastPayParam
import com.recording.trans.controller.Constant
import com.recording.trans.controller.RetrofitServiceManager
import com.recording.trans.http.response.Response
import com.recording.trans.utils.AppUtil
import io.reactivex.Observable

object FastPayParamLoader {

    fun getOrderParam(serviceId: Int): Observable<Response<FastPayParam>> {
        return RetrofitServiceManager.getInstance().fastPayParam.getOrderParam(
            serviceId,
            Constant.CLIENT_TOKEN,
            Constant.PRODUCT_ID,
            AppUtil.getChannelId()
        )
    }
}