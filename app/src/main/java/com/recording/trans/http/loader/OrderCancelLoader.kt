package com.recording.trans.http.loader

import com.recording.trans.controller.Constant
import com.recording.trans.controller.RetrofitServiceManager
import com.recording.trans.http.response.Response
import io.reactivex.Observable

object OrderCancelLoader {

    fun orderCancel(orderSn: String): Observable<Response<String?>> {
        return RetrofitServiceManager.getInstance().orderCancel().orderCancel(orderSn, Constant.CLIENT_TOKEN)
    }
}