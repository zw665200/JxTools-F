package com.recording.trans.http.request

import com.recording.trans.bean.AlipayParam
import com.recording.trans.http.response.Response
import io.reactivex.Observable
import retrofit2.http.*

interface AliPayService {

    @POST("orderPay")
    @FormUrlEncoded
    fun getOrderParam(
        @Field("serviceId") serviceId: Int,
        @Field("clientToken") clientToken: String,
        @Field("productId") productId: String,
        @Field("channelCode") channelCode: String,
        @Field("isBack") isback: Int
    ): Observable<Response<AlipayParam>>
}