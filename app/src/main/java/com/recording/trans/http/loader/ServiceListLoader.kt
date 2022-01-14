package com.recording.trans.http.loader

import com.recording.trans.bean.Price
import com.recording.trans.controller.RetrofitServiceManager
import com.recording.trans.http.response.Response
import io.reactivex.Observable

object ServiceListLoader {

    fun getServiceList(): Observable<Response<List<Price>>> {
        return RetrofitServiceManager.getInstance().price.getServiceList()
    }
}