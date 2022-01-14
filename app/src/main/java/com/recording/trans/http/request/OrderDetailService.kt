package com.recording.trans.http.request

import com.recording.trans.bean.OrderDetail
import com.recording.trans.http.response.Response
import io.reactivex.Observable
import retrofit2.http.*

interface OrderDetailService {

    @POST("orderDetail")
    @FormUrlEncoded
    fun getOrderDetail(@Field("orderSn") orderSn: String, @Field("clientToken") token: String): Observable<Response<OrderDetail>>
}