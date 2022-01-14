package com.recording.trans.http.request

import com.recording.trans.bean.Price
import com.recording.trans.controller.Constant
import com.recording.trans.http.response.Response
import io.reactivex.Observable
import retrofit2.http.*

interface ServiceListService {

    @GET("serverList/${Constant.PRODUCT_ID}")
    fun getServiceList(): Observable<Response<List<Price>>>
}