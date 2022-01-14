package com.recording.trans.http.loader

import com.recording.trans.bean.PayStatus
import com.recording.trans.controller.RetrofitServiceManager
import com.recording.trans.http.response.Response
import io.reactivex.Observable

object SinglePayStatusLoader {

    fun getPayStatus(serviceId: Int, token: String): Observable<Response<List<PayStatus>>> {
        return RetrofitServiceManager.getInstance().singlePayStatus.getPayStatus(serviceId, token)
    }
}