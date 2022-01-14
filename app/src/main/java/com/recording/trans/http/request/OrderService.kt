package com.recording.trans.http.request

import com.recording.trans.bean.Order
import com.recording.trans.http.response.Response
import io.reactivex.Observable
import retrofit2.http.*

interface OrderService {

    @POST("orderList")
    @FormUrlEncoded
    fun getOrders(
        @Field("productId") productId: String,
        @Field("clientToken") token: String
    ): Observable<Response<List<Order>>>
}